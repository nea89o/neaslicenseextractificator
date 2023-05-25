package moe.nea.licenseextractificator

import java.io.Serializable

sealed class LicenseExtra : Serializable {

    data class Matcher(val matcher: LicenseMatcher) : LicenseExtra()

    data class ModuleExtra(val group: String, val module: String, val licensing: ProjectLicensing) : LicenseExtra()

    data class SoloExtra(val licensing: ProjectLicensing) : LicenseExtra()

}
