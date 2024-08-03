

plugins {
    id("org.jetbrains.kotlin.jvm") version System.getenv("kotlin_version")
    application
}

application{
    mainClass = "me.naotiki.MainKt"
}

group = "me.naotiki"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}