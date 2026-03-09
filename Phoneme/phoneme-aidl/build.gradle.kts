plugins {
    alias(libs.plugins.android.library)
    id("ivi.maven.publish")
}

android {
    namespace = "com.telenav.scoutivi.tts.phoneme.aidl"
    compileSdk = 34

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    buildFeatures.aidl = true
}

dependencies {

}

mavenPublishing {
    artifactId = "phoneme-aidl"
}