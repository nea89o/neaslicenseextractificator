package moe.nea.licenseextractificator.builder

import moe.nea.licenseextractificator.ProjectLicensing

class DeveloperBuilder(var name: String) {

    var email: String? = null
    var webPresence: String? = null

    fun build(): ProjectLicensing.Developer =
        ProjectLicensing.Developer(name, email, webPresence)

}
