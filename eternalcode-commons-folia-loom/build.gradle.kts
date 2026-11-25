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
    
    compileOnly("dev.folia:folia-api:26.3.build.28-stable")
    
    api("org.jetbrains:annotations:26.1.0")
}
