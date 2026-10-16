plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.washgo.agent"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.washgo.agent"
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
    val composeBom = platform("androidx.compose:compose-bom:2024.09.02")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
