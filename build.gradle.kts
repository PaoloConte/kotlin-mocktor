import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.dokka)
}

repositories {
    mavenCentral()
}

group = "io.paoloconte"
version = "2.1.2"

subprojects {
    group = rootProject.group
    version = rootProject.version

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.vanniktech.maven.publish")
    apply(plugin = "org.jetbrains.dokka")

    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    configure<KotlinJvmProjectExtension> {
        jvmToolchain(11)
    }

    configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
        configure(
            KotlinJvm(
                javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
                sourcesJar = true,
            )
        )

        pom {
            inceptionYear.set("2025")
            url.set("https://github.com/PaoloConte/kotlin-mocktor")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://github.com/PaoloConte/kotlin-mocktor/blob/main/LICENSE")
                    distribution.set("https://github.com/PaoloConte/kotlin-mocktor/blob/main/LICENSE")
                }
            }
            developers {
                developer {
                    id.set("PaoloConte")
                    name.set("Paolo Conte")
                    url.set("https://github.com/PaoloConte/")
                }
            }
            scm {
                url.set("https://github.com/PaoloConte/kotlin-mocktor")
                connection.set("scm:git:git@github.com:PaoloConte/kotlin-mocktor.git")
                developerConnection.set("scm:git:ssh://git@github.com/PaoloConte/kotlin-mocktor.git")
            }
        }

        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

        signAllPublications()
    }
}

dependencies {
    dokka(project(":lib"))
    dokka(project(":lib-json-matcher"))
    dokka(project(":lib-xml-matcher"))
}

dokka {
    moduleName.set("Mocktor")
}
