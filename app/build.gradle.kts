plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.naver.maps.map.sleep"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.naver.maps.map.sleep"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

//csv 파일 읽어오기
    implementation ("com.opencsv:opencsv:5.6")
//mobile dynamic map sdk 적용
    implementation("com.naver.maps:map-sdk:3.19.1")
//FusedLocationSource 의존성 추가
    implementation("com.google.android.gms:play-services-location:21.0.1")
//FadingTextView 의존성 추가(텍스트 변경)
    implementation ("com.github.rosenpin:fading-text-view:3.3")
//viewpager2 의존성 추가
    implementation("androidx.viewpager2:viewpager2:1.1.0")
//google vision api
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")

//google vision api 관련 의존성 업데이트
    implementation ("com.google.android.gms:play-services-vision-common:19.1.3")
    implementation ("com.google.android.gms:play-services-vision:20.1.3")
//ML KIT face Deteciton
    implementation ("com.google.mlkit:face-detection:16.1.5")
//카메라 관리를 위한 dexter
    implementation ("com.karumi:dexter:6.2.3")
//MultiDex 지원
    implementation ("androidx.multidex:multidex:2.0.1")

//direction 5 적용
    //implementation("com.squareup.retrofit2:retrofit:2.9.0")
    //implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //implementation ("com.google.code.gson:gson:2.8.9")
    //implementation ("com.naver.maps:naver-map-direction:1.5.0")
//http라이브러리 적용
    //implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}