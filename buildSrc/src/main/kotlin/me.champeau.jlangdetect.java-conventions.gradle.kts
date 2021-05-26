plugins {
    `java-library`
    id("me.champeau.publishing")
}

group = "me.champeau.jlangdetect"
version = "0.6"

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.test {
    useTestNG()
}
