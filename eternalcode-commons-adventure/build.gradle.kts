plugins {
    `commons-java-17`
    `commons-publish`
    `commons-repositories`
    `commons-java-unit-test`
}


dependencies {
    api(project(":eternalcode-commons-shared"))

    api("net.kyori:adventure-text-minimessage:4.25.0")
    api("net.kyori:adventure-text-serializer-legacy:4.24.0")
}

tasks.test {
    useJUnitPlatform()
}
