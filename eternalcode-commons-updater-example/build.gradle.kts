plugins {
    `commons-java-17`
    `commons-repositories`
    application
}

application {
    mainClass.set("com.eternalcode.commons.updater.example.ExampleChecker")
}

dependencies {
    implementation(project(":eternalcode-commons-updater"))
}
