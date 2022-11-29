package org.example.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class MyRuleTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `reports inner classes`() {
        val code = """
        class A {
          inner class B
        }
        """
        val findings = MyRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `doesn't report inner classes`() {
        val code = """
        class A {
          class B
        }
        """
        val findings = MyRule(Config.empty).compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }
}
