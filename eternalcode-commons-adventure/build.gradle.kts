plugins {
    `commons-java-21`
    `commons-publish`
    `commons-repositories`
    `commons-java-unit-test`
}


dependencies {
    api(project(":eternalcode-commons-shared"))

    api("net.kyori:adventure-text-minimessage:5.1.0")
    api("net.kyori:adventure-text-serializer-legacy:5.0.1")
}

tasks.test {
    useJUnitPlatform()
}
