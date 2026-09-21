plugins {
    `commons-java-17`
    `commons-publish`
    `commons-repositories`
    `commons-java-unit-test`
}

dependencies {
    implementation("com.github.ben-manes.caffeine:caffeine:3.3.0")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.awaitility:awaitility:4.3.0")
}

tasks.test {
    useJUnitPlatform()
}
