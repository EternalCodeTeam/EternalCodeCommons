plugins {
    `commons-java-21`
    `commons-publish`
    `commons-repositories`
    `commons-java-unit-test`
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src/main/java"))
    }
    test {
        java.setSrcDirs(listOf("test"))
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
}

tasks.test {
    useJUnitPlatform()
}
