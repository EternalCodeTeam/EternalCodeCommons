plugins {
    id("java-library")
}

dependencies {
    testImplementation("org.mockito:mockito-core:5.19.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.4")
    testImplementation("org.assertj:assertj-core:3.27.5")
    testImplementation("org.awaitility:awaitility:4.3.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

sourceSets.test {
    java.setSrcDirs(listOf("test"))
    resources.setSrcDirs(emptyList<String>())
}
