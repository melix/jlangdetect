plugins {
    id("me.champeau.jlangdetect.java-conventions")
}

dependencies {
    api(project(":jlangdetect"))
    implementation(project(":jlangdetect-europarl"))
    implementation(libs.slf4j)
    testImplementation(libs.testng)
    testImplementation(libs.janino)
    testRuntimeOnly(libs.logbackClassic)
}

description = "JLangDetect extras"
