plugins {
    `commons-java-17`
    `commons-publish`
    `commons-repositories`
    `commons-java-unit-test`
}


dependencies {
    api(project(":eternalcode-commons-shared"))
    api(project(":eternalcode-commons-bukkit"))

    compileOnly("dev.folia:folia-api:26.3.build.26-stable")

    api("org.jetbrains:annotations:26.1.0")
}

tasks.test {
    useJUnitPlatform()
}
