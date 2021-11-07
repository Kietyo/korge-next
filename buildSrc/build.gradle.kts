plugins {
    `kotlin-dsl`
    publishing
    `maven-publish`
    signing
    //kotlin("multiplatform")
    //kotlin("gradle-plugin")
}
//val kotlinVersion = "1.5.31"
val kotlinVersion = "1.6.0-RC2"
val androidToolsBuildGradleVersion = "4.2.0"

dependencies {
    implementation("com.android.tools.build:gradle:$androidToolsBuildGradleVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}


repositories {
    mavenLocal()
    mavenCentral()
    google()
}
