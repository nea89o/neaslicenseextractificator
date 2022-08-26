package moe.nea.licenseextractificator

import java.io.File
import java.io.Serializable

class TextLicenseFormatter : LicenseFormatter, Serializable {
    override fun formatLicense(licenses: List<ProjectLicensing>, outputFile: File) {
        var isFirst = true
        outputFile.printWriter().use { writer ->
            for (project in licenses) {
                if (isFirst)
                    writer.println("--------------------------")
                isFirst = false
                writer.println(project.projectName)
                writer.println()
                writer.println(project.projectDescription)
                writer.println()
                for (license in project.licenses) {
                    writer.println(license.licenseName + " - " + license.licenseUrl)
                }
                writer.println()
                for (developer in project.developers) {
                    writer.print(developer.name)
                    if (developer.webPresence != null || developer.email != null) {
                        writer.print(" - ")
                    }
                    if (developer.webPresence != null)
                        writer.print(developer.webPresence)
                    if (developer.email != null) {
                        if (developer.webPresence != null) {
                            writer.printf(" (%s)", developer.email)
                        } else {
                            writer.print(developer.email)
                        }
                    }

                    writer.println()
                }
                writer.println("--------------------------")
            }

        }
    }
}