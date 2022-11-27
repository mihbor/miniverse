plugins {
  id("org.jetbrains.kotlin.js")
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(npm("three", "^0.132.2", generateExternals = false))
}

kotlin {
  js(IR) {
    browser()
    binaries.executable()
  }
}