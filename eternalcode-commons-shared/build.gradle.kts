plugins {
    `commons-java-17`
    `commons-publish`
    `commons-repositories`
    `commons-java-unit-test`
}

tasks.test {
    useJUnitPlatform()
}
