plugins {
  id("org.jetbrains.kotlin.js")
}

repositories {
  mavenCentral()
}

dependencies {
  api(project(":threejs_kt"))
  implementation(npm("three-mesh-ui", "4.6.0"))
}

kotlin {
  js(IR) {
    browser()
    binaries.executable()
  }
}