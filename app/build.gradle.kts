plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.example.dodojob"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.dodojob"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${project.findProperty("SUPABASE_URL") ?: System.getenv("SUPABASE_URL") ?: ""}\""
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${project.findProperty("SUPABASE_ANON_KEY") ?: System.getenv("SUPABASE_ANON_KEY") ?: ""}\""
        )

    }

    buildFeatures {
        compose = true
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

}
kotlin {
    jvmToolchain(17)                                   // ⬅️ 17
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.compose)

    /* ── Compose: BOM 1회 + 모듈은 버전 없이 alias ── */
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.foundation)

    /* 아이콘: extended만 넣으면 core까지 포함됨 */
    implementation(libs.androidx.compose.material.icons)

    /* Navigation-Compose: 버전은 그대로 두거나 toml에 alias로 옮겨도 OK */
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.foundation)
    implementation(libs.transportation.consumer)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.compose.foundation.foundation)
    implementation(libs.ui.text)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.compose.foundation.foundation2)
    implementation(libs.androidx.animation.core)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    /* 테스트에서도 BOM 맞추기 */
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Supabase BOM
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.storage)

    // Ktor & JSON
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.serialization.json)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.lifecycle.viewmodel.compose)   // ← 필수
    implementation(libs.lifecycle.runtime.compose)    // (선택) collectAsStateWithLifecycle
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    implementation(libs.kotlinx.coroutines.android)

    implementation("com.naver.maps:map-sdk:3.22.1") // 최신 버전으로 교체 가능
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // 네이버 역지오코딩 REST 호출용
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.github.jan-tennert.supabase:storage-kt")




}

/*configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin" &&
            requested.name.startsWith("kotlin-stdlib")) {
            useVersion("2.1.20") // libs.versions.toml과 동일
            because("Avoid mixed Kotlin stdlib causing compiler ICE")
        }
    }
}*/