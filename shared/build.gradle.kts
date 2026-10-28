import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
        useEsModules()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        useEsModules()
        binaries.executable()
    }

    android {
        namespace = "com.example.dictionary.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
            implementation("io.ktor:ktor-client-okhttp:3.0.0")
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
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

            // Modules 6 and 9: shared networking and JSON dependencies.
            implementation(libs.androidx.room.runtime)
            implementation("io.ktor:ktor-client-core:3.0.0")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
            implementation(libs.androidx.navigation.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
            implementation("io.ktor:ktor-client-darwin:3.0.0")
        }
        jvmTest.dependencies {
            implementation("io.ktor:ktor-client-mock:3.0.0")
        }
        jvmMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
            implementation("io.ktor:ktor-client-cio:3.0.0")
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
            implementation("io.ktor:ktor-client-js:3.0.0")
            implementation(project(":browserStorage"))
        }
        wasmJsMain.dependencies {
            implementation("io.ktor:ktor-client-js:3.0.0")
            implementation(project(":browserStorage"))
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    listOf("kspAndroid", "kspJvm", "kspIosArm64", "kspIosSimulatorArm64", "kspJs", "kspWasmJs").forEach {
        add(it, libs.androidx.room.compiler)
    }
}

room3 {
    schemaDirectory(layout.projectDirectory.dir("schemas"))
}
