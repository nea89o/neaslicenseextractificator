# Neas License Extractificator

> Extract licenses from Maven POMs (with local overrides)

## Applying to your project

`settings.gradle.kts`:

```kts
pluginManagement {
    repositories {
        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
        }
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "moe.nea.licenseextractificator" -> useModule("com.github.romangraef:neaslicenseextractificator:${requested.version}")
            }
        }
    }
}

```

`build.gradle.kts`:

```kts
plugins {
    id("moe.nea.licenseextractificator") version "<git hash>"
}
```

## Configuration

You can add license overrides or additional licenses in the `licensing` extension.
This can be used if a dependency doesn't specify their license in their Maven POM, or if you have been granted
a special license.

```kts
licensing {
    // Specify a license for specifiy modules
    module("org.jetbrains", "annotations") {
        // Specify a name of the project this license is for
        name = "Not Jetbrains Annotations"
        // Specify a description for the project
        description = "Definitely not Jetbrains annotations"

        // Specify a SPDX license
        spdxLicense.MIT()

        // You can specify dual (, triple, etc. ) licensing as well.
        license("My License", "https://example.com/mylicense")


        // Specify developer credits
        developer("Not The Jetbrains Team") {
            webPresence = "https://not.jetbrains.com"
        }
        developer("Some other person")
    }
    // Specify a license *without* an associated gradle dependency
    solo {
        name = "Some other dependency not captured by maven"
        description = "This other dependency is also licensed lol"
        spdxLicense.`BSD-3-Clause`()
        developer("Me") {
            webPresence = "https://nea89.moe"
        }
    }
}
```

The actual license task itself can also be configured:

```kts
val licenseTask = tasks.named("license", LicenseDiscoveryTask::class) {
    // Required: scan a configuration
    // If you don't scan a configuration, then no licenses can be found.
    scanConfiguration(project.configurations.compileClasspath.get())
    // Specify an output file
    outputFile.set(file("$buildDir/LICENSES.json"))
    // Specify a formatter. This is the JsonLicenseFormatter by default
    // Another formatter is TextLicenseFormatter
    licenseFormatter.set(JsonLicenseFormatter())
}

tasks.processResources {
    // Include the generated LICENSES.json in your java resources
    // They can now be accessed at runtime using SomeClass.class.getResourceAsStream("/LICENSES.json")
    // And from there your application can take over whatever processing it wants to do in order to display the licenses
    from(licenseTask)
}
```


 

