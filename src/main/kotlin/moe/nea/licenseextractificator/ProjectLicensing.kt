package moe.nea.licenseextractificator

import java.io.Serializable

/**
 * Simplified schema of the maven licensing blobs
 */
data class ProjectLicensing(
    val licenses: List<License>,
    val webPresence: String?,
    val projectName: String,
    val projectDescription: String?,
    val developers: List<Developer>,
) : Serializable {

    data class License(
        val licenseName: String,
        val licenseUrl: String?,
    ) : Serializable

    data class Developer(
        val name: String,
        val email: String?,
        val webPresence: String?,
    ) : Serializable
}