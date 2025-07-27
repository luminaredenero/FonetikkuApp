// Top-level build file where you can add configuration options common to all sub-projects/modules.

// TAMBAHKAN BLOK INI UNTUK MEMBERITAHU LOKASI DOWNLOAD LIBRARY
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}
