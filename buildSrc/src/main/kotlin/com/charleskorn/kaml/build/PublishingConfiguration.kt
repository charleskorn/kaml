package com.charleskorn.kaml.build

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import org.gradle.kotlin.dsl.register

fun Project.configurePublishing() {
    apply<MavenPublishPlugin>()

    createJarTasks()

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

val Project.sourceSets: SourceSetContainer
    get() = extensions.getByName("sourceSets") as SourceSetContainer
