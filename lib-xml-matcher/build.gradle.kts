
dependencies {
    implementation(libs.xmlunit.core)
    api(project(":lib"))

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.core)
}

mavenPublishing {
    coordinates(project.group.toString(), "mocktor-xml", project.version.toString())

    pom {
        name.set("Kotlin Mocktor XML Matcher")
        description.set("XML matching support for Kotlin Mocktor")
    }
}