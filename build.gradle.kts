plugins {
  java
}

group = "com.tallymc"
version = "1.1.2"

repositories {
  mavenCentral()
  maven {
    url = uri("https://repo.papermc.io/repository/maven-public/")
  }
}

dependencies {
  compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
}

dependencyLocking {
  lockAllConfigurations()
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.processResources {
  val props = mapOf("version" to project.version)
  filteringCharset = "UTF-8"
  inputs.property("version", project.version)
  filesMatching("paper-plugin.yml") {
    expand(props)
  }
}

tasks.jar {
  archiveFileName.set("TallyMC-${project.version}.jar")
}
