
dependencies {
    implementation(libs.kotlinx.serialization.json)
    api(project(":lib"))

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.core)
}

mavenPublishing {
    coordinates(project.group.toString(), "mocktor-json-matcher", project.version.toString())

    pom {
        name.set("Kotlin Mocktor JSON Matcher")
        description.set("JSON matching support for Kotlin Mocktor")
    }
}