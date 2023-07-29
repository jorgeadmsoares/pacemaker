@file:Suppress("OPT_IN_USAGE")

import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family

plugins {
    kotlin("multiplatform")
    id("android-conventions")
}

kotlin {
    plugins.withType<AndroidBasePlugin>().configureEach {
        androidTarget()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()

    targetHierarchy.default {
        common {
            group("mobile") {
                group("ios")
                withAndroidTarget()
                withJvm()
            }
        }
    }

    sourceSets.all {
        languageSettings.optIn("kotlin.time.ExperimentalTime")
        languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
    }

    jvmToolchain(8)
}