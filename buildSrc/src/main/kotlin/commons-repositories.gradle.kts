plugins {
    `java-library`
}

repositories {
    mavenCentral()

    maven("https://papermc.io/repo/repository/maven-public/") // paper, adventure, velocity
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // spigot
}
