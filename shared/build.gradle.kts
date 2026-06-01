plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    kotlin("plugin.compose")
    id("com.android.library")
    id("app.cash.sqldelight")
    id("org.jetbrains.compose")
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
                }
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

                // SQLDelight
                implementation("app.cash.sqldelight:runtime:2.0.2")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")

                // Ktor Client
                implementation("io.ktor:ktor-client-core:3.0.1")
                implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")

                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                // SQLDelight Android Driver
                implementation("app.cash.sqldelight:android-driver:2.0.2")

                // Ktor Android Client
                implementation("io.ktor:ktor-client-okhttp:3.0.1")
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                // SQLDelight Native Driver
                implementation("app.cash.sqldelight:native-driver:2.0.2")

                // Ktor iOS Client
                implementation("io.ktor:ktor-client-darwin:3.0.1")
            }
        }
    }
}

android {
    namespace = "fr.unilim.stodolist.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }
}

sqldelight {
    databases {
        create("StodolistDatabase") {
            packageName.set("fr.unilim.stodolist.database")
        }
    }
}
