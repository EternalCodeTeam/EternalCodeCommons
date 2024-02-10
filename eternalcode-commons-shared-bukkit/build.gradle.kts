plugins {
    `commons-java-17`
    `commons-publish`
    `commons-repositories`
    `commons-java-unit-test`
}


dependencies {
    api(project(":eternalcode-commons-shared"))
    api("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    api("net.kyori:adventure-api:4.15.0")
}

tasks.test {
    useJUnitPlatform()
}
