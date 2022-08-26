package moe.nea.licenseextractificator.builder

import moe.nea.licenseextractificator.ProjectLicensing

class LicenseBuilder {
    val licenses: MutableList<ProjectLicensing.License> = mutableListOf()
    var webPresence: String? = null
    var name: String? = null
    var description: String? = null
    val developers: MutableList<ProjectLicensing.Developer> = mutableListOf()

    fun developer(name: String, init: DeveloperBuilder.() -> Unit = {}) {
        developers.add(DeveloperBuilder(name).also(init).build())
    }

    fun license(name: String, url: String) {
        licenses.add(ProjectLicensing.License(name, url))
    }

    fun build(): ProjectLicensing {
        return ProjectLicensing(
            licenses,
            webPresence,
            name ?: error("Must need specify a projectName"),
            description,
            developers
        )
    }

    val spdxLicense get() = DefaultLicenses(this)

}