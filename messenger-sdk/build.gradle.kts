import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
    // Apollo codegen plugin is added in Phase 3 (subscriptions), once a reachable
    // schema is available. Phase 1/2 use raw HTTP + JSON, mirroring the iOS SDK.
    // alias(libs.plugins.apollo)
}

// Coordinates for the published artifact: io.github.munkhorgilb:messenger-sdk:<version>
group = "io.github.munkhorgilb"
version = "0.30.5"

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
}

// Publishing to Maven Central via the Central Portal (Sonatype), plus `publishToMavenLocal`
// for local testing. The vanniktech plugin builds the sources + javadoc jars, signs every
// artifact, and uploads the bundle. Consumers: `implementation("io.github.munkhorgilb:messenger-sdk:0.30.5")`.
//
// Credentials are read from ~/.gradle/gradle.properties (NEVER commit them) — see PUBLISHING.md:
//   mavenCentralUsername / mavenCentralPassword  (Central Portal user token)
//   signingInMemoryKey / signingInMemoryKeyPassword  (or signing.keyId/.password/.secretKeyRingFile)
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(group.toString(), "messenger-sdk", version.toString())

    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = true,
        )
    )

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
        developers {
            developer {
                id.set("Munkhorgilb")
                name.set("Munkh-orgil")
                url.set("https://github.com/Munkhorgilb")
            }
        }
        scm {
            url.set("https://github.com/Munkhorgilb/android-sdk")
            connection.set("scm:git:git://github.com/Munkhorgilb/android-sdk.git")
            developerConnection.set("scm:git:ssh://git@github.com/Munkhorgilb/android-sdk.git")
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
