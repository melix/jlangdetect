plugins {
    id("me.champeau.jlangdetect.java-conventions")
}

dependencies {
    implementation(libs.slf4j)
    api(project(":jlangdetect"))
    testImplementation(libs.testng)
    testImplementation(libs.janino)
    testRuntimeOnly(libs.logbackClassic)
}

description = "JLangDetect Europarl"
