

dependencies {
    implementation(libs.ktor.client.core)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
}



mavenPublishing {
    coordinates(project.group.toString(), "mocktor", project.version.toString())

    pom {
        name.set("Kotlin Mocktor library")
        description.set("This library provides a ktor client engine to mock responses with a convenient DSL")
    }
}