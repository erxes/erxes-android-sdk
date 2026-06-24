plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
    // Apollo codegen plugin is added in Phase 3 (subscriptions), once a reachable
    // schema is available. Phase 1/2 use raw HTTP + JSON, mirroring the iOS SDK.
    // alias(libs.plugins.apollo)
}

// Coordinates for the published artifact: com.erxes:messenger-sdk:<version>
group = "com.erxes"
version = "0.30.1"

android {
    namespace = "com.erxes.messenger"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    // Expose a "release" variant for publishing, with a matching sources jar.
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

// Publish the release AAR to Maven (local or remote). `./gradlew :messenger-sdk:publishToMavenLocal`
// installs it into ~/.m2 so a consumer app with `mavenLocal()` can `implementation("com.erxes:messenger-sdk:0.30.1")`.
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = "messenger-sdk"
            version = project.version.toString()

            afterEvaluate { from(components["release"]) }

            pom {
                name.set("erxes Android Messenger SDK")
                description.set("Native Android SDK for the erxes customer messenger (chat, tickets, knowledge base).")
                url.set("https://github.com/Munkhorgilb/android-sdk")
                licenses {
                    license {
                        name.set("AGPL-3.0")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.txt")
                    }
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.okhttp)
    implementation(libs.apollo.runtime)
    implementation(libs.coil.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
