plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.listapp"
    compileSdk = 34


    buildFeatures {
        viewBinding = true
        buildConfig = true

    }

    defaultConfig {
        applicationId = "com.example.listapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.metadata.extractor)


    implementation(libs.play.services.location)

    implementation(libs.play.services.maps)
    implementation(libs.activity)
    implementation(libs.material)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation (libs.okhttp)
    implementation (libs.converter.scalars)


    implementation(libs.gson)
    implementation(libs.github.glide)

    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)


    implementation(libs.room.ktx)

    implementation(libs.room.rxjava2)

    implementation(libs.room.rxjava3)

    implementation(libs.room.guava)

    testImplementation(libs.room.testing)

    implementation(libs.room.paging)

    implementation(libs.navigation.fragment)
    implementation(libs.navigation.common)
    implementation(libs.github.glide)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}