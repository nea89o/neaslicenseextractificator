import moe.nea.licenseextractificator.LicenseDiscoveryTask
import moe.nea.licenseextractificator.TextLicenseFormatter

plugins {
    id("moe.nea.licenseextractificator") version "0.0.1"
    java
}
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
}

licensing {
    module("org.jetbrains", "annotations") {
        name = "Not Jetbrains Annotations"
        description = "Definitely not Jetbrains annotations"
        spdxLicense.MIT()
        spdxLicense.`GPL-3-0-or-later`()

        license("My License", "https://example.com/mylicense")

        developer("Not The Jetbrains Team") {
            webPresence = "https://not.jetbrains.com"
        }
        developer("Some other person")
    }
    match {
        if (group.startsWith("com."))
            useLicense {
                name = "Gson"
                description =
                    "Gson is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java object."
                spdxLicense.`Apache-2-0`()
                developer("Google People") {
                    webPresence = "https://google.com"
                }
            }
    }
    solo {
        name = "Some other dependency not captured by maven"
        description = "This other dependency is also licensed"
        spdxLicense.`BSD-3-Clause`()
        developer("Me") {
            webPresence = "https://nea89.moe"
        }
    }
}

val licenseTask = tasks.named("license", LicenseDiscoveryTask::class) {
    scanConfiguration(project.configurations.compileClasspath.get())
    outputFile.set(file("$buildDir/LICENSES.txt"))
    licenseFormatter.set(TextLicenseFormatter())
}

tasks.processResources {
    from(licenseTask)
}