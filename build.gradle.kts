import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.spotless)
}

val ktlintEditorConfigOverrides = mapOf(
    "ktlint_standard_function-naming" to "disabled",
    "ktlint_standard_filename" to "disabled",
    "ktlint_standard_max-line-length" to "disabled",
)

configure<SpotlessExtension> {
    kotlin {
        target("**/*.kt")
        targetExclude(
            "**/build/**",
            "**/.gradle/**",
            "**/.kotlin/**",
            "**/.konan/**",
            "**/generated/**",
            "**/DerivedData/**",
            "**/Pods/**",
            "**/*.xcworkspace/**",
            "**/*.xcodeproj/**",
        )
        ktlint().editorConfigOverride(ktlintEditorConfigOverrides)
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude(
            "**/build/**",
            "**/.gradle/**",
            "**/.kotlin/**",
            "**/.konan/**",
            "**/generated/**",
            "**/DerivedData/**",
            "**/Pods/**",
            "**/*.xcworkspace/**",
            "**/*.xcodeproj/**",
        )
        ktlint().editorConfigOverride(ktlintEditorConfigOverrides)
        trimTrailingWhitespace()
        endWithNewline()
    }
}
