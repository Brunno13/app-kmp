plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    // Gerador de código (Necessário para o Room e Ktorfit)
    alias(libs.plugins.ksp)

    alias(libs.plugins.kotlinx.serialization)

    alias(libs.plugins.ktorfit)
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

            // Banco de Dados (Room KMP)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)

            // Rede (Ktorfit / Ktor)
            implementation(libs.ktorfit.lib)
            implementation("io.ktor:ktor-client-core:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-client-content-negotiation:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-serialization-kotlinx-json:${libs.versions.ktor.get()}")
            implementation(libs.kotlinx.serialization.json)
        }

        iosMain.dependencies {
            // Motores iOS
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

dependencies {
    // Ferramentas de UI do Android
    androidRuntimeClasspath(libs.compose.uiTooling)

    // Acionando os geradores de código do Room para cada plataforma (KSP)
    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
}

// Força o Room a gerar código em Kotlin (Obrigatório para KMP)
ksp {
    arg("room.generateKotlin", "true")
}