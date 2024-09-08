plugins {
    `commons-java-17`
    `commons-publish`
    `commons-repositories`
    `commons-java-unit-test`
}


dependencies {
    api(project(":eternalcode-commons-shared"))

    compileOnlyApi("dev.folia:folia-api:1.20.1-R0.1-SNAPSHOT")

    api("org.jetbrains:annotations:24.1.0")
}

tasks.test {
    useJUnitPlatform()
}
