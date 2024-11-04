plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.gms)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.dagger.hilt.android)
}

val appName = "nomdujeu"
val gdxVersion = "1.12.1"
val roboVMVersion = "2.3.12"
val box2DLightsVersion = "1.5"
val ashleyVersion = "1.7.3"
val aiVersion = "1.8.2"
val ktxVersion = "1.11.0-rc2"
val fleksVersion = "1.6-JVM"

android {
    namespace = "fr.eseo.ld.android.cp.nomdujeu"
    compileSdk = 34

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
        kotlinCompilerExtensionVersion = "1.5.3"
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
    implementation(libs.kotlinx.coroutines.play.services)
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
//    Ktor : don't work when set in libs.version and installed here
    implementation("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-cio:3.0.0")
    implementation("io.ktor:ktor-client-websockets:3.0.0")
    // JSON serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

//    LibGDX
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")
    add("natives", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    add("natives", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    add("natives", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    add("natives", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")
    add("natives", "com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-armeabi-v7a")
    add("natives", "com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-arm64-v8a")
    add("natives", "com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-x86")
    add("natives", "com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-x86_64")
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-box2d:$gdxVersion")

    // Ktx extensions of LibGDX
    implementation("io.github.libktx:ktx-actors:$ktxVersion")
    implementation("io.github.libktx:ktx-app:$ktxVersion")
    implementation("io.github.libktx:ktx-assets:$ktxVersion")
    implementation("io.github.libktx:ktx-box2d:$ktxVersion")
    implementation("io.github.libktx:ktx-collections:$ktxVersion")
    implementation("io.github.libktx:ktx-graphics:$ktxVersion")
    implementation("io.github.libktx:ktx-log:$ktxVersion")
    implementation("io.github.libktx:ktx-math:$ktxVersion")
    implementation("io.github.libktx:ktx-scene2d:$ktxVersion")
    implementation("io.github.libktx:ktx-style:$ktxVersion")
    implementation("io.github.libktx:ktx-tiled:$ktxVersion")

    // FLEKS : A fast, lightweight, entity component system library written in Kotlin
    implementation("io.github.quillraven.fleks:Fleks:$fleksVersion")

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

