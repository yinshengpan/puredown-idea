plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.puredown"
version = "1.0.0"

repositories {
    mavenCentral()
}

intellij {
    localPath.set("/Applications/Android Studio.app/Contents")
}

dependencies {
    // Include all extra jars in lib if any
    compileOnly(fileTree(mapOf("dir" to "/Applications/Android Studio.app/Contents/lib", "include" to listOf("*.jar"))))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }
    patchPluginXml {
        sinceBuild.set("233")
        untilBuild.set("")
    }
    buildSearchableOptions {
        enabled = false
    }
    runPluginVerifier {
        localPaths.set(listOf(
            file("/Applications/IntelliJ IDEA.app/Contents"),
            file("/Applications/Android Studio.app/Contents")
        ))
        ideVersions.set(listOf(
            "IU-2023.3.8",
            "IU-2024.1.7",
            "IU-2024.2.6",
            "IU-2024.3.5"
        ))
    }
}
