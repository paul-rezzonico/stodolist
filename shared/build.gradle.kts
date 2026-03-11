plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("app.cash.sqldelight")
    id("org.jetbrains.compose")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

                // SQLDelight
                implementation("app.cash.sqldelight:runtime:2.0.1")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")

                // Ktor Client
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

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
                implementation("app.cash.sqldelight:android-driver:2.0.1")

                // Ktor Android Client
                implementation("io.ktor:ktor-client-android:2.3.7")
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
                implementation("app.cash.sqldelight:native-driver:2.0.1")

                // Ktor iOS Client
                implementation("io.ktor:ktor-client-darwin:2.3.7")
            }
        }
    }
}

android {
    namespace = "fr.unilim.stodolist.shared"
    compileSdk = 34

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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

sqldelight {
    databases {
        create("StodolistDatabase") {
            packageName.set("fr.unilim.stodolist.database")
        }
    }
}
