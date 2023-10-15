@file:SuppressLint("TestManifestGradleConfiguration")

import android.annotation.SuppressLint
import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("pacemaker-application")
    id("org.jetbrains.compose")
    kotlin("native.cocoapods")
}

pacemaker {
    ios()
    android()
}

extensions.configure(ApplicationExtension::class) {
    namespace = "io.sellmair.pacemaker"
}

kotlin {
    sourceSets.commonMain.get().dependencies {
        api(project(":models"))
        api(project(":utils"))
        api(project(":bluetooth"))

        /* COMPOSE */
        implementation(compose.ui)
        implementation(compose.foundation)
        implementation(compose.runtime)

        implementation(compose.material3)
        implementation(compose.materialIconsExtended)

        /* Utils */
        implementation(Dependencies.coroutinesCore)
        implementation(Dependencies.okio)
        implementation("org.jetbrains.kotlinx:atomicfu:0.21.0")
    }

    sourceSets.androidMain.get().dependencies {
        /* androidx */
        implementation("androidx.activity:activity-compose:1.8.0")
        implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")
        implementation(compose.preview)

        /* Polar SDK and dependencies */
        implementation("com.github.polarofficial:polar-ble-sdk:4.0.0")
        implementation("io.reactivex.rxjava3:rxjava:3.1.6")
        implementation("io.reactivex.rxjava3:rxandroid:3.0.0")

        /* kotlinx */
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.7.3")

    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    sourceSets.invokeWhenCreated("androidDebug") {
        dependencies {
            implementation("androidx.compose.ui:ui-tooling:1.5.3")
            implementation("androidx.compose.ui:ui-test-manifest:1.5.3")
        }
    }

    sourceSets.getByName("androidInstrumentedTest").dependencies {
        implementation("androidx.compose.ui:ui-test-junit4:1.5.3")
    }

    cocoapods {
        version = "2023.1"
        name = "PM"
        podfile = project.file("../iosApp/Podfile")

        framework {
            homepage = "https://github.com/sellmair/pacemaker"
            summary = "Application Framework"
            baseName = "PM"
            isStatic = true
        }
    }
}
