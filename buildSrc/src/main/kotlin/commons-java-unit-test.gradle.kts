plugins {
    id("java-library")
}

dependencies {
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:6.1.0")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.awaitility:awaitility:4.3.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

sourceSets.test {
    java.setSrcDirs(listOf("test"))
    resources.setSrcDirs(emptyList<String>())
}
