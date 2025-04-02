plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    packaging {
        resources {
            excludes += "META-INF/gfprobe-provider.xml"
            excludes += "META-INF/NOTICE.md"  // Exclude NOTICE.md to prevent conflicts
            excludes += "META-INF/LICENSE.md" // (Optional) In case you see similar LICENSE errors
        }
    }

    namespace = "com.example.cw"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cw"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("org.eclipse.angus:jakarta.mail:2.0.2")

    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")

    implementation("org.mindrot:jbcrypt:0.4")


}