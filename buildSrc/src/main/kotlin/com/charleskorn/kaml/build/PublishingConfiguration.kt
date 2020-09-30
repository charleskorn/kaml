/*

   Copyright 2018-2020 Charles Korn.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package com.charleskorn.kaml.build

import de.marcphilipp.gradle.nexus.NexusPublishExtension
import de.marcphilipp.gradle.nexus.NexusPublishPlugin
import io.codearte.gradle.nexus.NexusStagingExtension
import io.codearte.gradle.nexus.NexusStagingPlugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import java.nio.file.Files
import java.util.Base64

fun Project.configurePublishing() {
    apply<NexusStagingPlugin>()
    apply<NexusPublishPlugin>()
    apply<SigningPlugin>()

    val usernameEnvironmentVariableName = "OSSRH_USERNAME"
    val passwordEnvironmentVariableName = "OSSRH_PASSWORD"
    val repoUsername = System.getenv(usernameEnvironmentVariableName)
    val repoPassword = System.getenv(passwordEnvironmentVariableName)

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

    createJarTasks()
    createPublishingTasks(repoUsername, repoPassword, validateCredentialsTask)
    createSigningTasks()
    createReleaseTasks(repoUsername, repoPassword, validateCredentialsTask)
}

private fun Project.createJarTasks() {
    val sourcesJarTask = tasks.register<Jar>("sourcesJar") {
        from(sourceSets.get("main").allSource)
        archiveClassifier.set("sources")
    }

    val javadocJarTask = tasks.register<Jar>("javadocJar") {
        from(tasks.named("javadoc"))
        archiveClassifier.set("javadoc")
    }

    tasks.named("assemble").configure {
        dependsOn(sourcesJarTask)
        dependsOn(javadocJarTask)
    }
}

private fun Project.createPublishingTasks(repoUsername: String?, repoPassword: String?, validateCredentialsTask: TaskProvider<Task>) {
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

    configure<NexusPublishExtension> {
        repositories {
            sonatype {
                username.set(repoUsername)
                password.set(repoPassword)
            }
        }
    }

    afterEvaluate {
        tasks.named("publishMavenJavaPublicationToSonatypeRepository").configure {
            dependsOn(validateCredentialsTask)
        }
    }
}

private fun Project.createSigningTasks() {
    configure<SigningExtension> {
        sign(publishing.publications["mavenJava"])
    }

    tasks.named<Sign>("signMavenJavaPublication").configure {
        doFirst {
            val keyId = getEnvironmentVariableOrThrow("GPG_KEY_ID")
            val keyRing = getEnvironmentVariableOrThrow("GPG_KEY_RING")
            val keyPassphrase = getEnvironmentVariableOrThrow("GPG_KEY_PASSPHRASE")

            val keyRingFilePath = Files.createTempFile("kaml-signing", ".gpg")
            keyRingFilePath.toFile().deleteOnExit()

            Files.write(keyRingFilePath, Base64.getDecoder().decode(keyRing))

            project.extra["signing.keyId"] = keyId
            project.extra["signing.secretKeyRingFile"] = keyRingFilePath.toString()
            project.extra["signing.password"] = keyPassphrase
        }
    }
}

private fun Project.createReleaseTasks(
    repoUsername: String?,
    repoPassword: String?,
    validateCredentialsTask: TaskProvider<Task>
) {
    configure<NexusStagingExtension> {
        numberOfRetries = 30
        username = repoUsername
        password = repoPassword
    }

    setOf("closeRepository", "releaseRepository", "getStagingProfile").forEach { taskName ->
        tasks.named(taskName).configure {
            dependsOn(validateCredentialsTask)
        }
    }

    val validateReleaseTask = tasks.register("validateRelease") {
        doFirst {
            if (version.toString().contains("-")) {
                throw RuntimeException("Attempting to publish a release of an untagged commit.")
            }
        }
    }

    tasks.register("publishSnapshot") {
        dependsOn("publishMavenJavaPublicationToSonatypeRepository")
    }

    tasks.named("closeRepository") {
        mustRunAfter("publishMavenJavaPublicationToSonatypeRepository")
    }

    tasks.register("publishRelease") {
        dependsOn(validateReleaseTask)
        dependsOn("publishMavenJavaPublicationToSonatypeRepository")
        dependsOn("closeAndReleaseRepository")
    }
}

private val Project.sourceSets: SourceSetContainer
    get() = extensions.getByName("sourceSets") as SourceSetContainer

private val Project.publishing: PublishingExtension
    get() = extensions.getByType<PublishingExtension>()

private fun getEnvironmentVariableOrThrow(name: String): String = System.getenv().getOrElse(name) {
    throw RuntimeException("Environment variable '$name' not set.")
}
