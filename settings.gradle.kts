rootProject.name = "miniverse"

include("threejs_kt")
include("three-mesh-ui_kt")

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}
