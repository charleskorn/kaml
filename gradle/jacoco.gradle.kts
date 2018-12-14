apply(plugin = "jacoco")

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

configure<JacocoPluginExtension> {
    toolVersion = "0.8.3-SNAPSHOT"
}

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.isEnabled = true
    }
}
