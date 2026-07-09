plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    // ESTA É A CORREÇÃO MÁGICA: Configura o Java 11 pro Android sem quebrar o iOS
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
    }

    sourceSets {
        androidMain.dependencies {
            // Usando o atalho inteligente do plugin para o Preview no Android
            implementation(compose.preview)

            // Motores Android
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
        }

        commonMain.dependencies {
            // Trocamos de "libs.compose..." para "compose..." (O atalho oficial da JetBrains)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Jetpack ViewModel
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Injeção de Dependência
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Banco de Dados (Room KMP)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)

            // Rede (Ktorfit / Ktor)
            implementation(libs.ktorfit.lib)
            implementation("io.ktor:ktor-client-core:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-client-content-negotiation:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-serialization-kotlinx-json:${libs.versions.ktor.get()}")
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}