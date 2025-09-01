plugins {
    `commons-java-17`
    `commons-publish`
    `commons-repositories`
    `commons-java-unit-test`
}

tasks.test {
    useJUnitPlatform()
}


dependencies {
    api("com.google.code.gson:gson:2.13.1")
    api(project(":eternalcode-commons-shared"))

    api("org.jetbrains:annotations:26.0.2-1")
}
