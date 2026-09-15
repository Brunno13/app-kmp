import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    // Gerador de código (Necessário para o Room e Ktorfit)
    alias(libs.plugins.ksp)

    alias(libs.plugins.kotlinx.serialization)

    alias(libs.plugins.ktorfit)

    id("jacoco")
}

kotlin {
    jvmToolchain(17)

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "com.brunno.appkmp.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withAndroidTestOnJvmBuilder {
            compilationName = "hostTest"
            defaultSourceSetName = "androidHostTest"
            sourceSetTreeName = "test"
        }.configure {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            // Usado atalho do plugin para o Preview no Android
            implementation(compose.preview)

            // Motores Android
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)

            implementation(libs.androidxSecurityCrypto)
            implementation(libs.androidx.biometric)
        }

        commonMain.dependencies {
            // UI (Compose)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Jetpack ViewModel & Navigation
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.jetbrains.navigation.compose)

            // Injeção de Dependência
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.koin.compose.viewmodel)

            // Banco de Dados (Room KMP)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)

            // Rede (Ktorfit / Ktor)
            implementation(libs.ktorfit.lib)
            implementation("io.ktor:ktor-client-core:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-client-content-negotiation:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-serialization-kotlinx-json:${libs.versions.ktor.get()}")
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.multiplatformSettings)

            implementation(compose.materialIconsExtended)

            implementation(libs.peekaboo.image.picker)
        }

        iosMain.dependencies {
            // Motores iOS
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("com.russhwolf:multiplatform-settings-test:1.1.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
            implementation(libs.ktor.client.mock)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)

    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
}

ksp {
    arg("room.generateKotlin", "true")
}

extensions.configure<JacocoPluginExtension> {
    toolVersion = "0.8.14"
}

tasks.register<JacocoReport>("jacocoAndroidHostTestReport") {
    group = "verification"
    description = "Generates JaCoCo coverage report for Android host tests."

    dependsOn("testAndroidHostTest")

    executionData(
        layout.buildDirectory.file(
            "jacoco/testAndroidHostTest.exec"
        )
    )

    classDirectories.setFrom(
        layout.buildDirectory.dir(
            "classes/kotlin/android/main"
        )
    )

    sourceDirectories.setFrom(
        files(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin"
        )
    )

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(true)
    }
}

tasks.register<JacocoReport>("jacocoBusinessCoverageReport") {
    group = "verification"
    description = "Generates JaCoCo coverage report for business logic."

    dependsOn("testAndroidHostTest")

    executionData(
        layout.buildDirectory.file(
            "jacoco/testAndroidHostTest.exec"
        )
    )

    classDirectories.setFrom(
        fileTree("build/classes/kotlin/android/main") {
            include(
                "com/brunno/appkmp/data/repository/**",
                "com/brunno/appkmp/data/remote/ApiErrorHandlerKt*",
                "com/brunno/appkmp/presentation/viewmodels/**",
                "com/brunno/appkmp/domain/error/**",
                "com/brunno/appkmp/domain/enums/**"
            )
        }
    )

    sourceDirectories.setFrom(
        files(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin"
        )
    )

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(true)
    }
}