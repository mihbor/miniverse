plugins {
  id("org.jetbrains.kotlin.js")
}

repositories {
  mavenCentral()
}

dependencies {
  api(project(":threejs_kt"))
  implementation(npm("three-mesh-ui", "6.5.2"))
}

kotlin {
  js(IR) {
    browser()
    binaries.executable()
  }
}