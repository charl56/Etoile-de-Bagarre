plugins {
    kotlin("plugin.serialization") version "1.9.22"
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.gms.google.services)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.dagger.hilt.android)
}

val appName = "nomdujeu"
val gdxVersion = "1.12.1"

android {
    namespace = "fr.eseo.ld.android.cp.nomdujeu"
    compileSdk = 35

    defaultConfig {
        applicationId = "fr.eseo.ld.android.cp.nomdujeu"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }



    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
            jniLibs.srcDirs("libs")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.9"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

configurations {
    create("natives")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.database.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)
    // Firebase
    implementation(libs.hilt.android)
    ksp(libs.hilt.ksp)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.config)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.play.services.auth)
    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.network.tls)
    // LibGDX
    implementation(libs.gdx)
    implementation(libs.gdx.backend.android)
    implementation(libs.gdx.box2d)
    implementation(libs.gdx.ai)
    // Add native extensions
    add("natives", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    add("natives", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    add("natives", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    add("natives", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")
    add("natives", "com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-armeabi-v7a")
    add("natives", "com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-arm64-v8a")
    add("natives", "com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-x86")
    add("natives", "com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-x86_64")
    // Ktx extensions of LibGDX
    implementation(libs.libktx.ktx.actors)
    implementation(libs.libktx.ktx.app)
    implementation(libs.libktx.ktx.assets)
    implementation(libs.libktx.ktx.box2d)
    implementation(libs.libktx.ktx.collections)
    implementation(libs.libktx.ktx.graphics)
    implementation(libs.libktx.ktx.log)
    implementation(libs.libktx.ktx.math)
    implementation(libs.libktx.ktx.scene2d)
    implementation(libs.libktx.ktx.style)
    implementation(libs.libktx.ktx.tiled)
    // FLEKS : A fast, lightweight, entity component system library written in Kotlin
    implementation(libs.io.github.quillraven.fleks)
}

// To be sure native libs are copied before start application
tasks.named("preBuild").configure {
    dependsOn("copyAndroidNatives")
}


tasks.register("copyAndroidNatives") {
    doFirst {
        val libsDir = file("libs")
        libsDir.mkdirs()

        // Chemin vers les bibliothèques natives
        val nativeLibsDir = file("$layout.buildDirectory/libs")
        nativeLibsDir.mkdirs()

        val armeabiV7aDir = file("libs/armeabi-v7a").apply { mkdirs() }
        val arm64V8aDir = file("libs/arm64-v8a").apply { mkdirs() }
        val x86Dir = file("libs/x86").apply { mkdirs() }
        val x8664Dir = file("libs/x86_64").apply { mkdirs() }

        configurations["natives"].resolvedConfiguration.files.forEach { jar ->
            val outputDir = when {
                jar.name.endsWith("natives-armeabi-v7a.jar") -> armeabiV7aDir
                jar.name.endsWith("natives-arm64-v8a.jar") -> arm64V8aDir
                jar.name.endsWith("natives-x86.jar") -> x86Dir
                jar.name.endsWith("natives-x86_64.jar") -> x8664Dir
                else -> null
            }

            outputDir?.let {
                copy {
                    from(zipTree(jar))
                    into(it)
                    include("*.so")
                }
            }
        }
    }
}

