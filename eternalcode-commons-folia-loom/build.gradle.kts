plugins {
    `commons-java-21`
    `commons-publish`
    `commons-repositories`
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src/main/java"))
    }
}

dependencies {
    api(project(":eternalcode-commons-loom"))
    
    compileOnly("dev.folia:folia-api:1.20.4-R0.1-SNAPSHOT")
    
    api("org.jetbrains:annotations:26.0.2-1")
}
