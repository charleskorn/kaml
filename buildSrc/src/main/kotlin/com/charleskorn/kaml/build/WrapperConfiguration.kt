package com.charleskorn.kaml.build

import org.gradle.api.Project
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.kotlin.dsl.named

fun Project.configureWrapper() {
    tasks.named<Wrapper>("wrapper") {
        distributionType = Wrapper.DistributionType.ALL
    }
}
