package moe.nea.licenseextractificator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.support.serviceOf

class LicensePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<LicenseExtension>("licensing")
        val task = target.tasks.create("license", LicenseDiscoveryTask::class.java, extension)
        task.licenseFormatter.convention(JsonLicenseFormatter())
        task.outputFile.convention(target.layout.buildDirectory.file("LICENSES.json"))
    }
}