buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
        classpath ("com.android.tools.build:gradle:7.1.3")

    }
    allprojects {
        repositories {
            mavenCentral()
            maven { url = uri("https://www.jitpack.io") }
            jcenter()
            google()

        }
    }
}


// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

}
