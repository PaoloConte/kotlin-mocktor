
dependencies {
    implementation(libs.xmlunit.core)
    implementation(project(":lib"))

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.core)
}