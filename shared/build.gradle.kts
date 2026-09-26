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
            implementation(libs.ktor.client.mock)
        }
        // In-memory SQLite for repository tests on the JVM (iOS uses the native driver).
        getByName("androidUnitTest").dependencies {
            implementation(libs.sqldelight.sqlite.driver)
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
                classes(
                    // HTTP client construction (platform engines, logging)
                    "org.gce.racehub.core.data.network.HttpClientFactory*",
                    // Platform SQLite driver factory
                    "org.gce.racehub.db.DatabaseDriverFactory*",
                    // Generated SQLDelight code
                    "org.gce.racehub.db.RaceHubDatabase*",
                    "org.gce.racehub.db.shared.*",
                    "org.gce.racehub.db.RaceEntity*",
                    "org.gce.racehub.db.DriverStandingEntity*",
                    "org.gce.racehub.db.ConstructorStandingEntity*",
                    "org.gce.racehub.db.TrendingThreadEntity*",
                    // DI wiring — pure configuration, no domain logic
                    "org.gce.racehub.di.*",
                    "org.gce.racehub.core.di.*",
                    "org.gce.racehub.auth.di.*",
                    "org.gce.racehub.race.di.*",
                    "org.gce.racehub.forum.di.*",
                    "org.gce.racehub.profile.di.*",
                    // Wire types — serialization, no domain logic. The auth DTOs are listed
                    // by name because DtoMappers.kt in the same package has logic.
                    "org.gce.racehub.core.data.GraphQL*",
                    "org.gce.racehub.auth.data.dto.LoginRequestDto*",
                    "org.gce.racehub.auth.data.dto.LoginResponseDto*",
                    "org.gce.racehub.auth.data.dto.LoginDataDto*",
                    "org.gce.racehub.auth.data.dto.UserResponseDto*",
                    "org.gce.racehub.auth.data.dto.LogoutResponseDto*",
                    "org.gce.racehub.auth.data.dto.SignUpRequestDto*",
                    "org.gce.racehub.auth.data.dto.Otp*Dto*",
                    "org.gce.racehub.auth.data.dto.PasswordReset*Dto*",
                    "org.gce.racehub.race.data.dto.*",
                    "org.gce.racehub.forum.data.dto.*",
                    "org.gce.racehub.profile.data.dto.*",
                    // Theme constants
                    "org.gce.racehub.theme.*",
                    // Interfaces (no executable code)
                    "org.gce.racehub.core.domain.session.SessionStorage",
                    "org.gce.racehub.auth.domain.repository.AuthRepository",
                    "org.gce.racehub.race.domain.repository.RaceRepository",
                    "org.gce.racehub.forum.domain.repository.ForumRepository",
                    "org.gce.racehub.profile.domain.repository.ProfileRepository",
                    // Android platform implementation (not exercised by JVM unit tests)
                    "org.gce.racehub.core.data.storage.AndroidSessionStorage"
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

    // JVM unit tests exercise code that logs via android.util.Log (logError);
    // return defaults instead of throwing "not mocked".
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}
