import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun localProperty(name: String): String = localProperties.getProperty(name).orEmpty()

fun String.escapeForBuildConfig(): String {
    return replace("\\", "\\\\").replace("\"", "\\\"")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.ktor.client.okhttp)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.lmartin.qrguardian"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.lmartin.qrguardian"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "GOOGLE_SAFE_BROWSING_API_KEY",
            "\"${localProperty("GOOGLE_SAFE_BROWSING_API_KEY").escapeForBuildConfig()}\""
        )
        buildConfigField(
            "String",
            "URLHAUS_API_KEY",
            "\"${localProperty("URLHAUS_API_KEY").escapeForBuildConfig()}\""
        )
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
