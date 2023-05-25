package moe.nea.licenseextractificator

import moe.nea.licenseextractificator.builder.LicenseBuilder
import java.io.Serializable

fun interface LicenseMatcher : Serializable {
    data class Context(val group: String, val module: String, val version: String) {
        internal var license: ProjectLicensing? = null
        fun useLicense(licensing: ProjectLicensing) {
            license = licensing
        }

        fun useLicense(block: LicenseBuilder.() -> Unit) {
            license = LicenseBuilder().also(block).build()
        }
    }

    fun testMatch(context: Context): ProjectLicensing? {
        context.match()
        return context.license
    }

    fun Context.match()
}