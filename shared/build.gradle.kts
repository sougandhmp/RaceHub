import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kover)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kmpNativeCoroutines)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    val xcf = XCFramework("Shared")
    listOf(
        iosArm64(),
        iosSimulatorArm64()
        // No iosX64: Kotlin deprecated the x64 Apple targets and AndroidX
        // lifecycle-viewmodel 2.11 no longer publishes them (Intel-Mac simulator only).
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            xcf.add(this)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    // KMP-NativeCoroutines generates @ObjCName-annotated Swift accessors.
    sourceSets.all {
        languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.koinViewmodelCore)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.androidx.security.crypto)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

sqldelight {
    databases {
        create("RaceHubDatabase") {
            packageName.set("org.gce.racehub.db")
        }
    }
}

kover {
    reports {
        filters {
            excludes {
                // Network clients and service classes (require live HTTP)
                classes(
                    "org.gce.racehub.auth.data.network.*",
                    "org.gce.racehub.auth.data.repository.AuthRepositoryNetworkImpl*",
                    "org.gce.racehub.race.data.repository.HomeRepositoryNetworkImpl*",
                    // Database infrastructure (requires platform SQLite driver)
                    "org.gce.racehub.db.DatabaseDriverFactory*",
                    "org.gce.racehub.db.LocalDataSource*",
                    // Generated SQLDelight code
                    "org.gce.racehub.db.RaceHubDatabase*",
                    "org.gce.racehub.db.shared.*",
                    // DI wiring — pure configuration, no domain logic
                    "org.gce.racehub.di.*",
                    "org.gce.racehub.auth.di.*",
                    "org.gce.racehub.race.di.*",
                    // DTO data classes — serialization, no domain logic
                    "org.gce.racehub.auth.data.dto.LoginRequestDto*",
                    "org.gce.racehub.auth.data.dto.LoginResponseDto*",
                    "org.gce.racehub.auth.data.dto.LoginDataDto*",
                    "org.gce.racehub.auth.data.dto.UserResponseDto*",
                    "org.gce.racehub.auth.data.dto.LogoutResponseDto*",
                    "org.gce.racehub.race.data.dto.*",
                    // Theme constants
                    "org.gce.racehub.theme.*",
                    // Interfaces (no executable code)
                    "org.gce.racehub.auth.domain.repository.AuthRepository",
                    "org.gce.racehub.auth.data.storage.SessionStorage",
                    "org.gce.racehub.race.domain.repository.HomeRepository",
                    // Android platform implementation (not exercised by JVM unit tests)
                    "org.gce.racehub.auth.data.storage.AndroidSessionStorage",
                    // Generated SQLDelight entity data classes
                    "org.gce.racehub.db.RaceEntity*",
                    "org.gce.racehub.db.DriverStandingEntity*",
                    "org.gce.racehub.db.ConstructorStandingEntity*",
                    "org.gce.racehub.db.TrendingThreadEntity*"
                )
            }
        }
    }
}

android {
    namespace = "org.gce.racehub.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
