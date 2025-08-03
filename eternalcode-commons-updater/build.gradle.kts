plugins {
    `commons-java-17`
    `commons-publish`
    `commons-repositories`
}

dependencies {
    api("org.json:json:20240303")
    api(project(":eternalcode-commons-shared"))
}
