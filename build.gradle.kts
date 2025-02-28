// Root-level build file
buildscript {
    dependencies {
        classpath(libs.google.services) // Use the version catalog
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.googleGmsGoogleServices) apply false
}