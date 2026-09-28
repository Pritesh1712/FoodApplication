plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.foodapplication"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.foodapplication"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    
    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    
    // Glide for Image Loading
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    
    // Socket.IO for Real-time
    implementation(libs.socket.io.client) {
        exclude(group = "org.json", module = "json")
    }
    
    // SwipeRefreshLayout
    implementation(libs.swiperefreshlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}