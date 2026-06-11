import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kover)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            freeCompilerArgs += listOf("-Xbinary=bundleId=com.lmartin.qrguardian.shared")
        }
    }

    androidLibrary {
        namespace = "com.lmartin.qrguardian.shared"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.mlkit.barcode.scanning)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.compose.uiToolingPreview)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.ktor.client.core)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.client.mock)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

kover {
    reports {
        filters {
            excludes {
                // Compose Multiplatform generated resources are not app logic and are not meaningfully testable.
                classes(
                    "qrguardian.shared.generated.resources.*",
                    "components.resources.library.generated.resources.*",
                )
                // Presentation layers are mostly UI/state rendering and are not the target for coverage tracking here.
                packages(
                    "com.lmartin.qrguardian.presentation.app.*",
                    "com.lmartin.qrguardian.presentation.camera.*",
                    "com.lmartin.qrguardian.presentation.components.*",
                    "com.lmartin.qrguardian.presentation.intro.*",
                    "com.lmartin.qrguardian.presentation.permissions.*",
                    "com.lmartin.qrguardian.presentation.result.*",
                    "com.lmartin.qrguardian.presentation.theme.*",
                )
                classes(
                    "com.lmartin.qrguardian.AppKt*",
                )
            }
        }
    }
}
