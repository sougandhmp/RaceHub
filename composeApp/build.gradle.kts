import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

/** The API base URL for [buildType], from `racehub.apiBaseUrl.<buildType>` in gradle.properties. */
fun apiBaseUrl(buildType: String): String =
    providers.gradleProperty("racehub.apiBaseUrl.$buildType").orNull
        ?: error("Set racehub.apiBaseUrl.$buildType in gradle.properties")

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
            implementation(libs.koin.androidx.compose)
            implementation(libs.koinViewmodelCompose)
            implementation(libs.koinViewmodelCore)
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.kotlinx.serialization.json)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.compose.material.icons.extended)
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        // Compose UI tests run on the JVM under Robolectric, next to the other unit tests.
        androidUnitTest.dependencies {
            implementation(libs.androidx.compose.ui.test.junit4)
            implementation(libs.robolectric)
        }
    }
}

android {
    namespace = "org.gce.racehub"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.gce.racehub"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = project.findProperty("buildNumber")?.toString()?.toIntOrNull() ?: 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl("debug")}\"")
        }
        getByName("release") {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl("release")}\"")
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        // Robolectric reaches into FileDescriptor internals, which JDK 21 no longer exports.
        unitTests.all { it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED") }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    // Registers the empty activity that Compose UI tests host content in.
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

composeCompiler {
    // Declares the :shared domain models (and read-only List) stable so composables
    // taking them can skip recomposition. See the file for the immutability contract.
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose_stability.conf"))
}
