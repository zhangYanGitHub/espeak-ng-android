pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

rootProject.name = "scoutivi-app-tts"
include(":Phoneme:espeak-ng")
include(":Phoneme:espeak-server")
include(":Phoneme:phoneme-aidl")
include(":Phoneme:phoneme-sdk")
include(":Speech:speech-service")
include(":Speech:sample")
