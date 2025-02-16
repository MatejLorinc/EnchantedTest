plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
}

group = "com.matejlorinc"
version = "1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}

paperweight {
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    jar {
        archiveBaseName.set("EnchantedTest")
        archiveClassifier.set("")
        archiveVersion.set("")
    }

    shadowJar {
        dependsOn(jar)
        configurations = listOf(project.configurations.getByName("shadow"))
        mergeServiceFiles()
        from("LICENSE") {
            rename { "${it}_${project.base.archivesName.get()}" }
        }
    }
}