plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val appVersion: String = providers.gradleProperty("appVersion").getOrElse("1.4.2")
val signingStoreFile = providers.gradleProperty("signingStoreFile")
val hasStableSigning = signingStoreFile.isPresent

android {
    namespace = "com.xq.phonecheck"
    compileSdk = 34

    signingConfigs {
        if (hasStableSigning) {
            create("stable") {
                storeFile = file(signingStoreFile.get())
                storePassword = providers.gradleProperty("signingStorePassword").getOrElse("")
                keyAlias = providers.gradleProperty("signingKeyAlias").getOrElse("")
                keyPassword = providers.gradleProperty("signingKeyPassword").getOrElse("")
            }
        }
    }

    defaultConfig {
        applicationId = "com.xq.phonecheck"
        minSdk = 26
        targetSdk = 34
        versionCode = 7
        versionName = appVersion
    }

    buildTypes {
        debug {
            if (hasStableSigning) {
                signingConfig = signingConfigs.getByName("stable")
            }
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasStableSigning) {
                signingConfig = signingConfigs.getByName("stable")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    applicationVariants.all {
        val variant = this
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "phone-check-${variant.versionName}-${variant.name}.apk"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    testImplementation("junit:junit:4.13.2")
}
