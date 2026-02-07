plugins {
    `commons-java-17`
    `commons-publish`
    `commons-repositories`
    `commons-java-unit-test`
}

dependencies {
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.3")
}

tasks.test {
    useJUnitPlatform()
}
