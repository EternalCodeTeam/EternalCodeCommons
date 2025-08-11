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
    api("org.json:json:20240303")
    api(project(":eternalcode-commons-shared"))

    api("org.jetbrains:annotations:24.1.0")
}
