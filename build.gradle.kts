plugins {
    kotlin("jvm") version "1.7.20"
    `maven-publish`
}

group = "com.wolt.arrow.detekt"
version = "1.0-SNAPSHOT"

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

    testImplementation(platform("io.arrow-kt:arrow-stack:1.1.3"))
    testImplementation("io.arrow-kt:arrow-core")

    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.22.0")
    testImplementation("io.kotest:kotest-assertions-core:5.5.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")

    ktlint("com.pinterest:ktlint:0.47.1")
}

publishing {
    publications {
        create<MavenPublication>("library") {
            version = project.version.toString()
            groupId = project.group.toString()
            artifactId = "rules"
            from(components["kotlin"])
            artifact(tasks.kotlinSourcesJar)
        }
    }
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
