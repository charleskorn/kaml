package com.charleskorn.kaml.build

import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

fun Project.configureAssemble() {
    tasks.register<Copy>("assembleRelease") {
        description = "Prepares files for release."
        group = "Distribution"

        from(tasks.named("jar"))
        from(tasks.named("javadocJar"))
        from(tasks.named("sourcesJar"))
        from(tasks.named("signMavenJavaPublication"))
        from(tasks.named("generatePomFileForMavenJavaPublication"))

        into(buildDir.toPath().resolve("release"))

        rename { filename ->
            if (filename.startsWith("pom-default.xml")) {
                filename.replace("^pom-default\\.xml".toRegex(), "${project.name}-${project.version}.pom")
            } else {
                filename
            }
        }
    }
}
