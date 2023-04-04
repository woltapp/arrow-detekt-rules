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
internal class NoRaiseBindableValueAsStatementTest(private val env: KotlinCoreEnvironment) {

    @Nested
    @DisplayName("bindable variations")
    inner class BindableVariations {
        @Test
        fun `reports unbound Either`() {
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
        fun `reports unbound Validated`() {
            val code = """
                import arrow.core.Validated
                import arrow.core.raise.Effect
                import arrow.core.raise.effect
                
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
                import arrow.core.raise.Effect
                import arrow.core.raise.effect
                import arrow.core.raise.eagerEffect

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
                import arrow.core.raise.Effect
                import arrow.core.raise.effect
                
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
                import arrow.core.raise.Effect
                import arrow.core.raise.effect
                
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
                import arrow.core.raise.Effect
                import arrow.core.raise.effect
                
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
                import arrow.core.raise.Effect
                import arrow.core.raise.effect
                
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
            import arrow.core.raise.effect
            import arrow.core.raise.Effect

            abstract class MyEffect : Effect<Nothing, Int>

            class MyEffectChild : MyEffect {
                override suspend fun invoke(p1: Raise<Nothing>): Int = TODO()
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
                import arrow.core.raise.either
                
                suspend fun test(): Either<Throwable, Int> = either {
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
                import arrow.core.raise.option
                
                suspend fun test(): Option<Int> = option {
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
                import arrow.core.raise.nullable
                
                suspend fun test(): Int? = nullable {
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
                import arrow.core.raise.ior
                
                suspend fun test(): Ior<List<Int>, Int> = ior(Semigroup.list()) {
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
                import arrow.core.raise.result
                
                suspend fun test(): Result<Int> = result {
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
                import arrow.core.None
                import arrow.core.raise.Raise
                import arrow.core.raise.Effect
                import arrow.core.raise.effect

                abstract class MyOptionScope : Raise<None>
                
                class MyChildScope : MyOptionScope() {
                    override fun raise(r: None): Nothing = TODO()
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

        @Test
        fun `reports unbound value inside extension function with receiver extending Raise`() {
            val code = """
                import arrow.core.Either
                import arrow.core.raise.Raise
                import arrow.core.raise.either
                
                fun t(): Either<String, Int> = either { 1 }
                
                class MyChildRaise: Raise<String> {
                    override fun raise(r: String): Nothing = TODO()
                }
                
                fun MyChildRaise.test(): Int {
                    t()
                    return 1
                }
            """
            val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
            findings shouldHaveSize 1
        }

        @Test
        fun `reports Raise extension function with unbound value`() {
            val code = """
                import arrow.core.raise.Raise
                import arrow.core.raise.either
                import arrow.core.Either
    
                fun Raise<String>.test(): Int {
                    Either.Right(1)
                    return 1
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
            import arrow.core.raise.either
            
            val e = Either.Right(1)
            
            fun test(): Either<Throwable, Int> = either {
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
            import arrow.core.raise.either
            
            object T {
                val e = Either.Right(1)
            }
            
            fun test(): Either<Throwable, Int> = either {
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
            import arrow.core.raise.either
    
            fun a() = Either.Right(1)
    
            fun test(): Either<Throwable, Int> = either {
                a()
                1
            }
        """
        val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports Raise extension function with unbound call expression`() {
        val code = """
            import arrow.core.Either
            import arrow.core.raise.Raise
            import arrow.core.raise.either
            
            fun a() = Either.Right(1)
            
            fun Raise<String>.test(): Int {
                a()
                return 1
            }
        """
        val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports unbound implicit return with Unit type`() {
        val code = """
            import arrow.core.Either
            import arrow.core.raise.either

            fun test(): Either<Throwable, Unit> = either {
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
            import arrow.core.raise.either

            fun test(): Either<Throwable, Int> = either {
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
            import arrow.core.raise.either

            fun test(): Either<Throwable, Unit> = either {
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
            import arrow.core.raise.either
            import arrow.core.getOrHandle

            fun test(): Either<Throwable, Unit> = either {
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
            import arrow.core.raise.either

            fun test(): Either<Throwable, Unit> = either {
                Either.Right(1).bind()
            }
        """
        val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `does not report Raise extension function with nothing to bind`() {
        val code = """
            import arrow.core.Either
            import arrow.core.raise.Raise
            import arrow.core.raise.either
            
            fun Raise<String>.test(): Int = raise("failure")
        """
        val findings = NoEffectScopeBindableValueAsStatement(Config.empty).lintWithContext(env, code)
        findings shouldHaveSize 0
    }
}
