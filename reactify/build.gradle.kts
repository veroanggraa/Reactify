plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech.maven.publish)
}

group = "io.github.veroanggraa"
version = "0.1.0-alpha01"

kotlin {
    explicitApi()

    android {
        namespace = "com.veroanggra.reactify.gooey"
        compileSdk = 37
        minSdk = 24

        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.graphics)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        getByName("androidDeviceTest").dependencies {
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.androidx.compose.ui.test.junit4)
            implementation(libs.androidx.junit)
            implementation(libs.androidx.espresso.core)
            implementation(libs.androidx.compose.ui.test.manifest)
        }
    }
}

// AGP's `com.android.kotlin.multiplatform.library` plugin doesn't expose `androidTest.sources.assets`
// yet, so the Compose Multiplatform resources plugin can't wire this task's `outputDirectory` and Gradle
// refuses to run it ("Value not set"). There are no real Compose resources in this module either way, so
// the task is a no-op; it just needs a valid output directory to satisfy Gradle's property validation.
// The task type itself is internal to the compose-gradle-plugin module, hence the reflection.
tasks.matching { it.name == "copyAndroidDeviceTestComposeResourcesToAndroidAssets" }.configureEach {
    val outputDirectory = javaClass.getMethod("getOutputDirectory").invoke(this)
        as org.gradle.api.file.DirectoryProperty
    outputDirectory.set(layout.buildDirectory.dir("intermediates/compose-resources/$name"))
}

mavenPublishing {
    coordinates(group.toString(), "reactify", version.toString())

    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set("Reactify")
        description.set(
            "Metaball-style \"gooey\" UI for Compose Multiplatform: shapes that merge, stretch " +
                "necks, and pinch apart as items animate from a source into a target.",
        )
        url.set("https://github.com/veroanggraa/Reactify")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("veroanggraa")
                name.set("Veronica")
                url.set("https://github.com/veroanggraa")
            }
        }

        scm {
            url.set("https://github.com/veroanggraa/Reactify")
            connection.set("scm:git:git://github.com/veroanggraa/Reactify.git")
            developerConnection.set("scm:git:ssh://git@github.com/veroanggraa/Reactify.git")
        }
    }
}
