plugins {
    kotlin("jvm") version "1.7.20"
    `maven-publish`
    signing
}

group = "com.wolt.arrow.detekt"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
}

val ktlint: Configuration by configurations.creating

dependencies {
    compileOnly("io.gitlab.arturbosch.detekt:detekt-api:1.22.0")

    testImplementation(platform("io.arrow-kt:arrow-stack:1.1.2"))
    testImplementation("io.arrow-kt:arrow-core")

    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.22.0")
    testImplementation("io.kotest:kotest-assertions-core:5.5.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")

    ktlint("com.pinterest:ktlint:0.48.0")
}

val mavenPublicationName = "library"

publishing {
    publications {
        create<MavenPublication>(mavenPublicationName) {
            version = project.version.toString()
            groupId = project.group.toString()
            artifactId = "rules"
            from(components["kotlin"])
            artifact(tasks.kotlinSourcesJar)
            artifact(tasks.named("javadocJar"))
            pom {
                name.set("Arrow Detekt rules")
                description.set("Detekt rules that validate usage of Arrow")
                url.set("https://github.com/woltapp/arrow-detekt-rules")
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://github.com/woltapp/arrow-detekt-rules/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("diastremskii")
                        name.set("Daniil Iastremskii")
                        email.set("daniil.iastremskii@wolt.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/woltapp/arrow-detekt-rules.git")
                    developerConnection.set("scm:git:https://github.com/woltapp/arrow-detekt-rules.git")
                    url.set("https://github.com/woltapp/arrow-detekt-rules")
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            credentials(PasswordCredentials::class)
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications[mavenPublicationName])
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
    systemProperty("compile-snippet-tests", project.hasProperty("compile-test-snippets"))
    testLogging {
        events("failed", "skipped", "passed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

val inputFiles = project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))

val ktlintCheck by tasks.creating(JavaExec::class) {
    group = "ktlint"
    inputs.files(inputFiles)
    description = "Check Kotlin code style."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("src/**/*.kt")
}

val ktlintFormat by tasks.creating(JavaExec::class) {
    group = "ktlint"
    inputs.files(inputFiles)
    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("-F", "src/**/*.kt")
    jvmArgs = listOf("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}

val ktlintIdeaSettings by tasks.creating(JavaExec::class) {
    group = "ktlint"
    inputs.files(inputFiles)
    description = "IntelliJ IDEA Project settings."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("applyToIDEAProject", "-y")
}
