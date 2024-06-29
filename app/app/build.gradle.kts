plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.qrcheckin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.qrcheckin"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }


}


dependencies {
    // Circular profile view

    //implementation(files ("C:/Users/armaa/AppData/Local/Android/Sdk/platforms/android-34/android.jar"))
    implementation("androidx.cardview:cardview:1.0.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity:1.7.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.squareup.retrofit2:retrofit:2.3.0")
    implementation("com.squareup.retrofit2:converter-gson:2.3.0")
    // QR Generator: https://github.com/androidmads/QRGenerator
    implementation("com.github.androidmads:QRGenerator:1.0.1")

    // OpenStreetMap
    implementation("org.osmdroid:osmdroid-android:6.1.14")

    implementation ("com.google.android.gms:play-services-location:18.0.0")


    // Glide library for image processing
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("androidx.datastore:datastore-core-android:1.1.0-beta02")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation ("androidx.test.espresso:espresso-intents:3.4.0")


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.6.1")
    testImplementation("org.mockito:mockito-core:3.3.3")
    testImplementation("org.robolectric:robolectric:4.6.1")
    testImplementation("org.mockito:mockito-inline:3.3.3")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("com.google.firebase:firebase-firestore:24.10.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.test.uiautomator:uiautomator:2.2.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}