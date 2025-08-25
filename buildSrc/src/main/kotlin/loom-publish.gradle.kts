plugins {
    `java-library`
    `maven-publish`
}

group = "dev.vanqure.loom"
version = "1.0.0-SNAPSHOT"

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    repositories {
        mavenLocal()
    }
}

fun RepositoryHandler.maven(name: String, url: String, username: String, password: String) {
    val isSnapshot = version.toString().endsWith("-SNAPSHOT")
    this.maven {
        this.name = if (isSnapshot) "${name}Snapshots" else "${name}Releases"
        this.url = if (isSnapshot) uri("$url/snapshots") else uri("$url/releases")
        this.credentials {
            this.username = System.getenv(username)
            this.password = System.getenv(password)
        }
    }
}

interface LoomPublishExtension {
    var artifactId: String
}

val extension = extensions.create<LoomPublishExtension>("loomPublish")

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                artifactId = extension.artifactId
                from(project.components["java"])
            }
        }
    }
}