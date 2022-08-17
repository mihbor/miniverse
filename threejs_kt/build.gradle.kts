plugins {
  id("org.jetbrains.kotlin.js")
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(npm("three", "^0.120.0", generateExternals = false))
}

kotlin {
  js(IR) {
    browser()
    binaries.executable()
  }
}