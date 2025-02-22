// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0-rc01" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false
}

buildscript {
    // 省略
    dependencies {
        // 省略
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.46.1") // 追加

    }
}