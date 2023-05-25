plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.0.0"
}

group = "moe.nea"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.9.1")
    testImplementation(kotlin("test"))
}

pluginBundle {
    website = "https://github.com/romangraef/neaslicenseextractificator"
    vcsUrl = "https://github.com/romangraef/neaslicenseextractificator"
    description = "Automatically extract licenses from maven pom (or local declarations) into a JSON or text file to be read by your application"
    tags = listOf("licensing")
}

gradlePlugin {
    plugins {
        create("licenseextractificator") {
            id = "moe.nea.licenseextractificator"
            implementationClass = "moe.nea.licenseextractificator.LicensePlugin"
        }
    }
}

