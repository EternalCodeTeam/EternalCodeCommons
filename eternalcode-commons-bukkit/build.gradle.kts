plugins {
    `commons-java-17`
    `commons-publish`
    `commons-repositories`
    `commons-java-unit-test`
}


dependencies {
    api(project(":eternalcode-commons-shared"))

    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    testImplementation("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")

    api("org.jetbrains:annotations:25.0.0")
}

tasks.test {
    useJUnitPlatform()
}
