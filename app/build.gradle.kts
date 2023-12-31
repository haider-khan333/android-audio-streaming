plugins {
    id("com.android.application")
}

android {
    namespace = "ai.issm.audiostreaming"
    compileSdk = 33

    defaultConfig {
        applicationId = "ai.issm.audiostreaming"
        minSdk = 24
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // okhttp3 implementation
    implementation("com.squareup.okhttp3:okhttp:4.9.2")

//    implementation ("org.java-websocket:Java-WebSocket:1.5.2")

    // RxJava3 implementation
    implementation("io.reactivex.rxjava3:rxjava:3.0.0")
    // RxAndroid implementation
    implementation("io.reactivex.rxjava3:rxandroid:3.0.0")



    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}