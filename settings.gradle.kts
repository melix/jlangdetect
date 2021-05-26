rootProject.name = "jlangdetect-parent"

enableFeaturePreview("VERSION_CATALOGS")

include(":jlangdetect-europarl")
include(":jlangdetect")
include(":jlangdetect-extra")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}