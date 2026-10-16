plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.washgo.user"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.washgo.user"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val mapsApiKey = (project.findProperty("MAPS_API_KEY") as? String)
            ?: System.getenv("MAPS_API_KEY")
            ?: ""
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")

        val maplibreStyleUrl = (project.findProperty("MAPLIBRE_STYLE_URL") as? String)
            ?: System.getenv("MAPLIBRE_STYLE_URL")
            ?: "https://demotiles.maplibre.org/style.json"
        buildConfigField("String", "MAPLIBRE_STYLE_URL", "\"$maplibreStyleUrl\"")
    }

    buildTypes {
        debug {
            val stagingUrl = (project.findProperty("STAGING_BASE_URL") as? String)
                ?: (project.findProperty("DEBUG_BASE_URL") as? String)
                ?: "https://wash-go-api-hsvt.onrender.com/"
            val normalizedStagingUrl = if (stagingUrl.endsWith("/")) stagingUrl else "$stagingUrl/"
            buildConfigField("String", "BASE_URL", "\"$normalizedStagingUrl\"")
        }
        release {
            val prodUrl = (project.findProperty("PROD_BASE_URL") as? String)
                ?: (project.findProperty("BASE_URL") as? String)
                ?: "https://wash-go-api.onrender.com/"
            val normalizedProdUrl = if (prodUrl.endsWith("/")) prodUrl else "$prodUrl/"
            buildConfigField("String", "BASE_URL", "\"$normalizedProdUrl\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(libs.maplibre.android)

    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
