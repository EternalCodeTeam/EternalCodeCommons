plugins {
    `java-library`
}

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/") // paper, adventure, velocity
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // spigot
}
