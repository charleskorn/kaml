package com.charleskorn.kaml.build

import com.diffplug.gradle.spotless.SpotlessPlugin
import org.ajoberstar.reckon.gradle.ReckonExtension
import org.ajoberstar.reckon.gradle.ReckonPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

fun Project.configureVersioning() {
    apply<ReckonPlugin>()

    configure<ReckonExtension> {
        scopeFromProp()
        snapshotFromProp()
    }
}
