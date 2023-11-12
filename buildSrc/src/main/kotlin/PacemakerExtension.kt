@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import app.cash.sqldelight.gradle.SqlDelightExtension
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSourceSetConvention
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class PacemakerExtension(
    private val project: Project,
    private val androidPluginId: String
) {

    private val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

    fun jvm() {
        kotlin {
            jvm()
            sourceSets.jvmTest.dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }

    fun ios() {
        kotlin.iosX64()
        kotlin.iosArm64()
        kotlin.iosSimulatorArm64()
    }

    fun macos() {
        kotlin.macosArm64()
        kotlin.macosX64()
    }

    fun watchos() {
        kotlin.watchosArm64()
        kotlin.watchosSimulatorArm64()
    }

    fun android() {
        project.plugins.apply(androidPluginId)
        kotlin.androidTarget()
        project.extensions.configure(BaseExtension::class) {
            compileSdkVersion(34)
            namespace = "io.sellmair.${project.name.replace("-", ".")}"
            defaultConfig {
                minSdk = 31
            }

            if (this is ApplicationExtension) {
                packaging {
                    resources {
                        /* https://github.com/Kotlin/kotlinx-atomicfu/pull/344 */
                        excludes.add("META-INF/versions/9/previous-compilation-data.bin")
                    }
                }
            }
        }

        kotlin.apply {
            sourceSets.androidMain.dependencies {
                implementation(Dependencies.coroutinesAndroid)
            }
        }
    }

    val sourceSets = SourceSetsDsl()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    inner class SourceSetsDsl {
        fun useNonAndroid() {
            kotlin.applyDefaultHierarchyTemplate {
                common {
                    group("nonAndroid") {
                        withCompilations { true }
                        excludeCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
                    }
                }
            }
        }

        fun useJvmAndAndroid() {
            kotlin.applyDefaultHierarchyTemplate {
                common {
                    group("jvmAndAndroid") {
                        withAndroidTarget()
                        withJvm()
                    }
                }
            }
        }
    }


    val features = FeaturesDsl()

    fun features(configure: FeaturesDsl.() -> Unit) {
        features.configure()
    }

    inner class FeaturesDsl {
        fun useSqlDelight(configure: SqlDelightExtension.() -> Unit) {
            project.plugins.apply("app.cash.sqldelight")
            kotlin {
                sourceSets.commonMain.dependencies {
                    implementation("app.cash.sqldelight:coroutines-extensions:2.0.0")
                }

                sourceSets.androidMain.dependencies {
                    implementation("app.cash.sqldelight:android-driver:2.0.0")
                }

                sourceSets.getByName("androidUnitTest").dependencies {
                    implementation("app.cash.sqldelight:sqlite-driver:2.0.0")
                }

                sourceSets.nativeMain.dependencies {
                    implementation("app.cash.sqldelight:native-driver:2.0.0")
                }


            }

            project.extensions.configure(SqlDelightExtension::class.java) {
                this.configure()
            }
        }

        fun useAtomicFu() {
            project.plugins.apply("kotlinx-atomicfu")
            kotlin {
                sourceSets.commonMain.dependencies {
                    implementation("org.jetbrains.kotlinx:atomicfu:0.22.0")
                }
            }
        }
    }

    operator fun <T> T.invoke(configure: T.() -> Unit) {
        configure(this)
    }
}

val NamedDomainObjectContainer<KotlinSourceSet>.androidUnitTest by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.androidInstrumentedTest by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.phoneMain by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.phoneTest by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.iosAndMacMain by KotlinSourceSetConvention
val NamedDomainObjectContainer<KotlinSourceSet>.iosAndMacTest by KotlinSourceSetConvention