plugins {
    `java-library`
    `maven-publish`
}

group = "com.eternalcode"
version = "1.3.4"

java {
    withSourcesJar()
}

publishing {
    repositories {
        mavenLocal()

        val mavenUsername = System.getenv("ETERNAL_CODE_MAVEN_USERNAME")
        val mavenPassword = System.getenv("ETERNAL_CODE_MAVEN_PASSWORD")

        if (!mavenUsername.isNullOrBlank() && !mavenPassword.isNullOrBlank()) {
            maven {
                url = uri("https://repo.eternalcode.pl/releases")

                if (version.toString().endsWith("-SNAPSHOT")) {
                    url = uri("https://repo.eternalcode.pl/snapshots")
                }

                credentials {
                    username = mavenUsername
                    password = mavenPassword
                }
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

