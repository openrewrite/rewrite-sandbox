plugins {
    id("org.openrewrite.build.recipe-library") version "1.7.0"
}

// Set as appropriate for your organization
group = "org.openrewrite.recipe"
description = "Rewrite sandbox."

val rewriteVersion = rewriteRecipe.rewriteVersion.get()
dependencies {
    implementation("org.openrewrite:rewrite-java:${rewriteVersion}")
    implementation("org.openrewrite:rewrite-maven:${rewriteVersion}")
    runtimeOnly("org.openrewrite:rewrite-java-17:${rewriteVersion}")

    // Need to have a slf4j binding to see any output enabled from the parser.
    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.+")
}
