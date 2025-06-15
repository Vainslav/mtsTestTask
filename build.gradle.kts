plugins {
    id("java-gradle-plugin")
    id("maven-publish")
    kotlin("jvm") version "2.0.21"
}

group = "pumpkingseeds"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("bracketCounter") {
            id = "pumpkingseeds.brackets-counter"
            implementationClass = "pumpkingseeds.BracketsCounterPlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = "pumpkingseeds"
            artifactId = "brackets-counter"
            version = "1.0.0"

            pom {
                packaging = "pom"
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}