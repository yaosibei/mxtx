import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.util.Properties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.mindeye.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mindeye.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "2.0.0"

        manifestPlaceholders["AMAP_API_KEY"] = localProperties.getProperty("amap.api.key", "")
        buildConfigField("String", "XUNFEI_APP_ID", "\"${localProperties.getProperty("xunfei.appId", "")}\"")
        buildConfigField("String", "XUNFEI_API_KEY", "\"${localProperties.getProperty("xunfei.apiKey", "")}\"")
        buildConfigField("String", "XUNFEI_API_SECRET", "\"${localProperties.getProperty("xunfei.apiSecret", "")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        viewBinding = true
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    sourceSets {
        getByName("main") {
            // 统一使用标准 jniLibs 目录，避免与 app/libs 下的原始 SDK 文件重复打包。
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }
    
    applicationVariants.all {
        if (buildType.name == "debug") {
            outputs.all {
                val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
                (this as? BaseVariantOutputImpl)?.outputFileName = "mxtx-$name-$timestamp.apk"
            }
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation(files("libs/arm64-v8a/armeabi-v7a/Msc.jar"))

    // AndroidX 核心库
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Hilt 依赖注入
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Material Design Components
    implementation("com.google.android.material:material:1.11.0")

    // Room 数据库
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // 网络请求
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // ML Kit - 文字识别(中文)
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")
    // ML Kit - 图像标签
    implementation("com.google.mlkit:image-labeling:17.0.7")
    // ML Kit - 对象检测
    implementation("com.google.mlkit:object-detection:17.0.1")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    ksp("androidx.hilt:hilt-compiler:1.1.0")

    // 位置服务
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.amap.api:search:9.7.0")
    implementation("com.amap.api:navi-3dmap:10.0.700_3dmap10.0.700")

    // 图片加载
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:compiler:4.16.0")

    // 权限请求
    implementation("com.guolindev.permissionx:permissionx:1.7.0")

    // JSON 解析
    implementation("com.google.code.gson:gson:2.10.1")

    // 测试
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Compose 测试
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
