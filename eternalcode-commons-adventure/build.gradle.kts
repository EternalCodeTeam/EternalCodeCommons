plugins {
    `commons-java-21`
    `commons-publish`
    `commons-repositories`
    `commons-java-unit-test`
}


dependencies {
    api(project(":eternalcode-commons-shared"))

    // For best version support we keep the old version of adventure.
    api("net.kyori:adventure-text-minimessage:4.12.0")
    api("net.kyori:adventure-text-serializer-legacy:4.12.0")
}

tasks.test {
    useJUnitPlatform()
}
