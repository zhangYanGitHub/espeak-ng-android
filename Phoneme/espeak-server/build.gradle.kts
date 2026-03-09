plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val major = findProperty("major") ?: "1"
val minor = findProperty("minor") ?: "0"
val versionNameProp = findProperty("versionName")?.toString()
val appVersionName = versionNameProp ?: "${major}.${minor}.${findProperty("buildNumber") ?: "0"}"
val versionParts = appVersionName.split(".")

val majorInt = versionParts.getOrNull(0)?.toInt() ?: 0
val minorInt = versionParts.getOrNull(1)?.toInt() ?: 0
val patchInt = versionParts.getOrNull(2)?.toInt() ?: 0

val appVersionCode =
    majorInt * 1_000_000 +
        minorInt * 1_000 +
        patchInt

println("appVersionName $appVersionName")
println("appVersionCode $appVersionCode")
android {
    namespace = "com.telenav.scoutivi.espeak"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.espeak.tts.server"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = appVersionCode.toInt()
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(project(":Phoneme:espeak-ng"))
    implementation(project(":Phoneme:phoneme-aidl"))
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

}