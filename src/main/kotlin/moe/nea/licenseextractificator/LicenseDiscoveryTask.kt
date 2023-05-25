package moe.nea.licenseextractificator

import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.UnresolvedArtifactResult
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import javax.inject.Inject

abstract class LicenseDiscoveryTask @Inject constructor(@Nested  val extension: LicenseExtension) : DefaultTask() {

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val scannedDependencies: ListProperty<ComponentArtifactIdentifier>

    @get:Input
    abstract val licenseFormatter: Property<LicenseFormatter>


    @get:Internal
    lateinit var resolvedLicenses: Map<ComponentIdentifier, ProjectLicensing>


    fun scanConfiguration(configuration: Configuration) {
        scannedDependencies.addAll(configuration.incoming
            .artifacts.resolvedArtifacts
            .map { it.map { it.id } })
    }


    @TaskAction
    fun discoverLicenses() {
        val componentIdentifiers = scannedDependencies.get().toSet()
        val resolvedLicenses = mutableMapOf<ComponentIdentifier, ProjectLicensing>()
        val extras = extension.extras.get()
        val overrides = extras.filterIsInstance<LicenseExtra.ModuleExtra>()
        val dynamicOverrides = extras.filterIsInstance<LicenseExtra.Matcher>()
        outer@for (comp in componentIdentifiers) {
            val componentIdentifier = comp.componentIdentifier
            if (componentIdentifier is ModuleComponentIdentifier) {
                val potentialOverrides =
                    overrides.filter { it.module == componentIdentifier.module && it.group == componentIdentifier.group }
                when (potentialOverrides.size) {
                    1 -> {
                        logger.info("Using provided module override for ${comp.displayName}")
                        resolvedLicenses[comp.componentIdentifier] = potentialOverrides[0].licensing
                        continue
                    }

                    2 -> {
                        logger.warn("Found two potential manually supplied module overrides for ${comp.displayName}. Using only the first.")
                        resolvedLicenses[comp.componentIdentifier] = potentialOverrides[0].licensing
                        continue
                    }
                }

                for ((matcher) in dynamicOverrides) {
                    val context = LicenseMatcher.Context(
                        componentIdentifier.group,
                        componentIdentifier.module,
                        componentIdentifier.version
                    )
                    val licensing = matcher.testMatch(context) ?: continue
                    resolvedLicenses[comp.componentIdentifier] = licensing
                    continue@outer
                }
            }
            val resolutionResult =
                project.dependencies.createArtifactResolutionQuery()
                    .forComponents(comp.componentIdentifier)
                    .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
                    .execute()
            for (component in resolutionResult.resolvedComponents) {
                for (arti in component.getArtifacts(MavenPomArtifact::class.java)) {
                    if (arti is ResolvedArtifactResult) {
                        if (comp.componentIdentifier in resolvedLicenses) {
                            logger.warn("Found two POMs for ${comp.displayName}. Will only keep first one found and discard additional information (discarding ${arti.id})")
                            continue
                        }
                        val parsedPom = parsePom(arti)
                        if (parsedPom != null)
                            resolvedLicenses[comp.componentIdentifier] = parsedPom
                    } else if (arti is UnresolvedArtifactResult) {
                        logger.warn("Could not resolve POM artifact for ${comp.displayName}", arti.failure)
                    } else {
                        logger.error("ArtifactResolutionQuery returned unknown type: ${arti::javaClass}")
                    }
                }
            }

            if (comp.componentIdentifier !in resolvedLicenses) {
                logger.error("Could not find License information for ${comp.componentIdentifier}")
            }
        }
        this.resolvedLicenses = resolvedLicenses
        val allResolvedLicenses =
            extras.filterIsInstance<LicenseExtra.SoloExtra>().map { it.licensing } + resolvedLicenses.values
        outputFile.get().asFile.parentFile.mkdirs()
        licenseFormatter.get().formatLicense(allResolvedLicenses, outputFile.get().asFile)
    }

    private fun parsePom(arti: ResolvedArtifactResult): ProjectLicensing? {
        val pom = XmlParser().parse(arti.file)
        return ProjectLicensing(
            findLicenses(arti, pom) ?: return null,
            getString(pom["url"]),
            getString(pom["name"]) ?: return warn(arti, "Found POM without name"),
            getString(pom["description"]),
            findDevelopers(arti, pom) ?: return null,
        )
    }

    private fun findDevelopers(arti: ResolvedArtifactResult, pom: Node): List<ProjectLicensing.Developer>? {
        val developers = pom["developers"] as? NodeList ?: return emptyList()
        return developers
            .filterIsInstance<Node>().flatMap { it.children() }
            .filterIsInstance<Node>().map {
                val name = getString(it["name"])
                val url = getString(it["url"]) ?: getString(it["organizationUrl"])
                val email = getString(it["email"])
                ProjectLicensing.Developer(name ?: return warn(arti, "Found developer without name"), email, url)
            }
    }

    private fun findLicenses(arti: ResolvedArtifactResult, pom: Node): List<ProjectLicensing.License>? {
        val licenses = pom["licenses"] as? NodeList ?: return null
        return licenses
            .filterIsInstance<Node>().flatMap { it.children() }
            .filterIsInstance<Node>().map {
                val name = getString(it["name"])
                val url = getString(it["url"])
                ProjectLicensing.License(name ?: return warn(arti, "Found license without name"), url)
            }
    }

    private fun <T> warn(arti: ResolvedArtifactResult, text: String): T? {
        logger.error("${arti.id}: $text")
        return null
    }

    private fun getString(nodeList: Any?): String? = when (nodeList) {
        is NodeList -> getString(nodeList.singleOrNull())
        is Node -> nodeList.localText().singleOrNull()
        is String -> nodeList
        else -> null
    }

}