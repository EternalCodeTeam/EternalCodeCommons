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
    compileOnlyApi("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
}
