package moe.nea.licenseextractificator

import moe.nea.licenseextractificator.builder.LicenseBuilder
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input

abstract class LicenseExtension {
    @get:Input
    abstract val extras: ListProperty<LicenseExtra>

    /**
     * Add a license override for a module. This will cause the given license to be used instead of the license
     * provided by the maven POM, but will only include this license if this module is dependent on.
     */
    fun specifyModuleLicense(group: String, module: String, licensing: ProjectLicensing) {
        extras.add(LicenseExtra.ModuleExtra(group, module, licensing))
    }

    /**
     * @see specifyModuleLicense
     */
    fun module(group: String, module: String, init: LicenseBuilder.() -> Unit) {
        specifyModuleLicense(group, module, LicenseBuilder().also(init).build())
    }

    /**
     * Like [specifyModuleLicense], but allows for manually matching each artifact.
     * These dynamic matchers are always weaker than [specifyModuleLicense], and are executed in order, with the first
     * declared one taking priority.
     */
    fun specifyLicenseIf(block: LicenseMatcher) {
        extras.add(LicenseExtra.Matcher(block))
    }

    /**
     * @see specifyLicenseIf
     */
    fun match(block: LicenseMatcher) {
        specifyLicenseIf(block)
    }

    /**
     * Add a license to each license task. These solo licenses will always be included.
     */
    fun specifySoloLicense(license: ProjectLicensing) {
        extras.add(LicenseExtra.SoloExtra(license))
    }

    /**
     * @see specifySoloLicense
     */
    fun extraLicense(init: LicenseBuilder.() -> Unit) {
        specifySoloLicense(LicenseBuilder().also(init).build())
    }

    /**
     * See [specifySoloLicense]
     */
    fun solo(init: LicenseBuilder.() -> Unit) {
        specifySoloLicense(LicenseBuilder().also(init).build())
    }

}