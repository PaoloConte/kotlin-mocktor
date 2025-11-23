
dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":lib"))

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.core)
}