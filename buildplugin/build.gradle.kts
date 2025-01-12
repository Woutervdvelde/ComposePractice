plugins {
    `kotlin-dsl`
}

//repositories {
//    mavenCentral()
//}

gradlePlugin {
    plugins {
        create("BuildPlugin") {
            id = "com.example.buildplugin"
            implementationClass = "com.example.buildplugin.BuildPlugin"
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}