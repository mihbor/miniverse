plugins {
  kotlin("multiplatform") version "1.6.10"
  kotlin("plugin.serialization") version "1.6.10"
  id("org.jetbrains.compose") version "1.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  google()
}

kotlin {
  js(IR) {
    browser()
    binaries.executable()
  }
  sourceSets {
    val jsMain by getting {
      dependencies {
        implementation(compose.web.core)
        implementation(compose.runtime)
        
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
  
        implementation("com.ionspin.kotlin:bignum:0.3.3")
        implementation("com.ionspin.kotlin:bignum-serialization-kotlinx:0.3.3")
        api(project(":threejs_kt"))
        api(project(":three-mesh-ui_kt"))
      }
    }
  }
}

tasks.register<Zip>("minidappDistribution") {
  dependsOn("jsBrowserDistribution")
  archiveFileName.set("${project.name}.mds.zip")
  destinationDirectory.set(layout.buildDirectory.dir("minidapp"))
  from(layout.buildDirectory.dir("distributions"))
}
