plugins {
    `commons-java-17`
    `commons-publish`
    `commons-repositories`
    `commons-java-unit-test`
}


dependencies {
    api(project(":eternalcode-commons-shared"))
    api(project(":eternalcode-commons-bukkit"))

    compileOnly("dev.folia:folia-api:1.20.1-R0.1-SNAPSHOT")

    api("org.jetbrains:annotations:25.0.0")
}

tasks.test {
    useJUnitPlatform()
}
