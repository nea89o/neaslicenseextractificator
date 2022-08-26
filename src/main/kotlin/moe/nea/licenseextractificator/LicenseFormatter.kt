package moe.nea.licenseextractificator

import java.io.File

fun interface LicenseFormatter {
    fun formatLicense(licenses: List<ProjectLicensing>, outputFile: File)
}