plugins {
    id("java-library")
}

dependencies {
    testImplementation("org.mockito:mockito-core:5.20.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.14.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.14.0")
    testImplementation("org.assertj:assertj-core:3.27.6")
    testImplementation("org.awaitility:awaitility:4.3.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.14.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

sourceSets.test {
    java.setSrcDirs(listOf("test"))
    resources.setSrcDirs(emptyList<String>())
}
