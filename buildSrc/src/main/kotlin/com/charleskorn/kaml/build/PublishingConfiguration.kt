package com.charleskorn.kaml.build

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

fun Project.configurePublishing() {
    apply<MavenPublishPlugin>()

    createJarTasks()

    val snapshotsRepositoryName = "mavenSnapshots"
    val releasesRepositoryName = "mavenReleases"
    val usernameEnvironmentVariableName = "OSSRH_USERNAME"
    val passwordEnvironmentVariableName = "OSSRH_PASSWORD"
    val repoUsername = System.getenv(usernameEnvironmentVariableName)
    val repoPassword = System.getenv(passwordEnvironmentVariableName)

    configure<PublishingExtension> {
        publications {
            register<MavenPublication>("mavenJava") {
                from(components.getByName("java"))
                artifact(tasks.getByName("sourcesJar"))
                artifact(tasks.getByName("javadocJar"))

                pom {
                    name.set("kaml")
                    description.set("YAML support for kotlinx.serialization")
                    url.set("https://github.com/charleskorn/kaml")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("charleskorn")
                            name.set("Charles Korn")
                            email.set("me@charleskorn.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/charleskorn/kaml.git")
                        developerConnection.set("scm:git:ssh://github.com:charleskorn/kaml.git")
                        url.set("http://github.com/charleskorn/kaml")
                    }
                }
            }
        }

        repositories {
            maven {
                name = snapshotsRepositoryName
                url = uri("https://oss.sonatype.org/content/repositories/snapshots")

                credentials {
                    username = repoUsername
                    password = repoPassword
                }
            }

            maven {
                name = releasesRepositoryName
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")

                credentials {
                    username = repoUsername
                    password = repoPassword
                }
            }
        }
    }

    val validateReleaseTask = tasks.register("validateRelease") {
        doFirst {
            if (version.toString().contains("-")) {
                throw RuntimeException("Attempting to publish a release of an untagged commit.")
            }
        }
    }

    val validateCredentialsTask = tasks.register("validateMavenRepositoryCredentials") {
        doFirst {
            if (repoUsername.isNullOrBlank()) {
                throw RuntimeException("Environment variable '$usernameEnvironmentVariableName' not set.")
            }

            if (repoPassword.isNullOrBlank()) {
                throw RuntimeException("Environment variable '$passwordEnvironmentVariableName' not set.")
            }
        }
    }

    tasks.named("publishMavenJavaPublicationTo${snapshotsRepositoryName.capitalize()}Repository").configure {
        dependsOn(validateCredentialsTask)
    }

    tasks.named("publishMavenJavaPublicationTo${releasesRepositoryName.capitalize()}Repository").configure {
        dependsOn(validateCredentialsTask)
        dependsOn(validateReleaseTask)
    }
}

private fun Project.createJarTasks() {
    val sourcesJarTask = tasks.register<Jar>("sourcesJar") {
        from(sourceSets.get("main").allSource)
        classifier = "sources"
    }

    val javadocJarTask = tasks.register<Jar>("javadocJar") {
        from(tasks.named("javadoc"))
        classifier = "javadoc"
    }

    tasks.named("assemble").configure {
        dependsOn(sourcesJarTask)
        dependsOn(javadocJarTask)
    }
}

private val Project.sourceSets: SourceSetContainer
    get() = extensions.getByName("sourceSets") as SourceSetContainer
