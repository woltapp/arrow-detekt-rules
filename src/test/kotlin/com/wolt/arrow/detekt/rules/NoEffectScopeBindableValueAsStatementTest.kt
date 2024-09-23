package com.wolt.arrow.detekt.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.lintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class NoEffectScopeBindableValueAsStatementTest(private val env: KotlinCoreEnvironment) {

    @Nested
    @DisplayName("bindable variations")
    inner class BindableVariations {
        @Test
        fun `reports unbound Either`() {
            val code = """
                import arrow.core.Either
                import arrow.core.continuations.Effect
                import arrow.core.continuations.effect
                
                fun test(): Effect<Throwable, Int> = effect {
                    Either.Right(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound Validated`() {
            val code = """
                import arrow.core.Validated
                import arrow.core.continuations.Effect
                import arrow.core.continuations.effect
                
                fun test(): Effect<Throwable, Int> = effect {
                    Validated.Valid(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound EagerEffect`() {
            val code = """
                import arrow.core.continuations.Effect
                import arrow.core.continuations.effect
                import arrow.core.continuations.eagerEffect

                fun test(): Effect<Throwable, Int> = effect {
                    eagerEffect<Throwable, Int> { 1 }
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound Effect`() {
            val code = """
                import arrow.core.continuations.Effect
                import arrow.core.continuations.effect
                
                fun test(): Effect<Throwable, Int> = effect {
                    effect <Throwable, Int> { 1 }
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound Option`() {
            val code = """
                import arrow.core.Option
                import arrow.core.continuations.Effect
                import arrow.core.continuations.effect
                
                fun test(): Effect<Throwable, Int> = effect {
                    Option(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound Result`() {
            val code = """
                import arrow.core.continuations.Effect
                import arrow.core.continuations.effect
                
                fun test(): Effect<Throwable, Int> = effect {
                    Result.success(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound Ior`() {
            val code = """
                import arrow.core.Ior
                import arrow.core.continuations.Effect
                import arrow.core.continuations.effect
                
                fun test(): Effect<Throwable, Int> = effect {
                    Ior.Right(5)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports bindable value with more than one level of inheritance`() {
            val code = """
            import arrow.core.continuations.effect
            import arrow.core.continuations.Effect

            abstract class MyEffect : Effect<Nothing, Int>

            class MyEffectChild : MyEffect() {
                override suspend fun <B> fold(recover: suspend (shifted: Nothing) -> B, transform: suspend (value: Int) -> B): B = TODO()
            }

            fun test(): Effect<Throwable, Int> = effect {
                MyEffectChild()
                1
            }
        """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }
    }

    @Nested
    @DisplayName("effect scope variations")
    inner class EffectScopeVariations {
        @Test
        fun `reports unbound value inside legacy effect {} scope`() {
            val code = """
                import arrow.core.Either
                import arrow.core.continuations.Effect
                import arrow.core.continuations.effect
                
                fun test(): Effect<Throwable, Int> = effect {
                    Either.Right(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound value inside effect {} scope`() {
            val code = """
                import arrow.core.Either
                import arrow.core.raise.Effect
                import arrow.core.raise.effect
                
                fun test(): Effect<Throwable, Int> = effect {
                    Either.Right(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound value inside legacy eagerEffect {} scope`() {
            val code = """
                import arrow.core.Either
                import arrow.core.continuations.EagerEffect
                import arrow.core.continuations.eagerEffect
                
                fun test(): EagerEffect<Throwable, Int> = eagerEffect {
                    Either.Right(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound value inside eagerEffect {} scope`() {
            val code = """
                import arrow.core.Either
                import arrow.core.raise.EagerEffect
                import arrow.core.raise.eagerEffect
                
                fun test(): EagerEffect<Throwable, Int> = eagerEffect {
                    Either.Right(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound value inside either {} scope`() {
            val code = """
                import arrow.core.Either
                import arrow.core.continuations.either
                
                suspend fun test(): Either<Throwable, Int> = either {
                    Either.Right(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound value inside either eager {} scope`() {
            val code = """
                import arrow.core.Either
                import arrow.core.continuations.either
                
                fun test(): Either<Throwable, Int> = either.eager {
                    Either.Right(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound value inside option {} scope`() {
            val code = """
                import arrow.core.Option
                import arrow.core.continuations.option
                
                suspend fun test(): Option<Int> = option {
                    Option(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound value inside option eager {} scope`() {
            val code = """
                import arrow.core.Option
                import arrow.core.continuations.option
                
                fun test(): Option<Int> = option.eager {
                    Option(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound value inside nullable {} scope`() {
            val code = """
                import arrow.core.Option
                import arrow.core.continuations.nullable
                
                suspend fun test(): Int? = nullable {
                    Option(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound value inside nullable eager {} scope`() {
            val code = """
                import arrow.core.Option
                import arrow.core.continuations.nullable
                
                fun test(): Int? = nullable.eager {
                    Option(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound value inside ior {} scope`() {
            val code = """
                import arrow.core.Ior
                import arrow.typeclasses.Semigroup
                import arrow.core.continuations.ior
                
                suspend fun test(): Ior<List<Int>, Int> = ior(Semigroup.list()) {
                    Ior.Right(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound value inside ior eager {} scope`() {
            val code = """
                import arrow.core.Ior
                import arrow.typeclasses.Semigroup
                import arrow.core.continuations.ior
                
                fun test(): Ior<List<Int>, Int> = ior.eager(Semigroup.list()) {
                    Ior.Right(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound value inside result {} scope`() {
            val code = """
                import arrow.core.continuations.result
                
                suspend fun test(): Result<Int> = result {
                    Result.success(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound value inside result eager {} scope`() {
            val code = """
                import arrow.core.continuations.result
                
                fun test(): Result<Int> = result.eager {
                    Result.success(1)
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports unbound value inside scope with more than one level of inheritance`() {
            val code = """
                import arrow.core.continuations.EffectScope
                import arrow.core.continuations.Effect
                import arrow.core.continuations.effect

                abstract class MyOptionScope : EffectScope<None>
                
                class MyChildScope : MyOptionScope() {
                    override suspend fun <B> shift(r: None): B = TODO()
                }
                
                object myChildScope {
                    inline operator fun <A> invoke(crossinline f: suspend MyChildScope.() -> A): Effect<Throwable, A> = TODO()
                }

                fun test(): Effect<Throwable, Int> = myChildScope {
                    effect <Throwable, Int> { 1 }
                    1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }
    }

    @Test
    fun `reports unbound reference expression`() {
        val code = """
            import arrow.core.Either
            import arrow.core.continuations.either
            
            val e = Either.Right(1)
            
            fun test(): Either<Throwable, Int> = either.eager {
                e
                1
            }
        """
        val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports unbound reference expression with dot qualified expression`() {
        val code = """
            import arrow.core.Either
            import arrow.core.continuations.either
            
            object T {
                val e = Either.Right(1)
            }
            
            fun test(): Either<Throwable, Int> = either.eager {
                T.e
                1
            }
        """
        val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports unbound call expression`() {
        val code = """
            import arrow.core.Either
            import arrow.core.continuations.either
    
            fun a() = Either.Right(1)
    
            fun test(): Either<Throwable, Int> = either.eager {
                a()
                1
            }
        """
        val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports unbound implicit return with Unit type`() {
        val code = """
            import arrow.core.Either
            import arrow.core.continuations.either

            fun test(): Either<Throwable, Unit> = either.eager {
                Either.Right(1)
            }
        """
        val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports unbound value inside an if`() {
        val code = """
            import arrow.core.Either
            import arrow.core.continuations.either

            fun test(): Either<Throwable, Int> = either.eager {
                if (true) {
                    Either.Right(1)
                }
                1
            }
        """
        val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports unbound value inside nested context`() {
        val code = """
            import arrow.core.Either
            import arrow.core.continuations.either

            fun test(): Either<Throwable, Unit> = either.eager {
                with (1) { Either.Right(this) }
            }
        """
        val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `does not report if the value is handled`() {
        val code = """
            import arrow.core.Either
            import arrow.core.continuations.either
            import arrow.core.getOrHandle

            fun test(): Either<Throwable, Unit> = either.eager {
                Either.Right(1).getOrHandle { 1 }
            }
        """
        val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `does not report if the value is bound`() {
        val code = """
            import arrow.core.Either
            import arrow.core.continuations.either

            fun test(): Either<Throwable, Unit> = either.eager {
                Either.Right(1).bind()
            }
        """
        val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
        findings shouldHaveSize 0
    }
}
