# Detekt rules for Arrow

This project contains [Detekt](https://detekt.dev) rules for the [Arrow](https://arrow-kt.io/) ecosystem.

## How to use it

1. Add [JitPack](https://jitpack.io/) to `build.gradle.kts`
```kotlin
 allprojects {
     repositories {
         ...
         maven { url = uri("https://jitpack.io") }
     }
 }
```
2. Add this project as a Detekt plugin to `build.gradle.kts`
```kotlin
dependencies {
    detektPlugins("com.github.woltapp:arrow-detekt-rules:*TAG*")
}
```
3. Enable and configure rules in Detekt configuration, example:
```yaml
ArrowRuleSet:
  NoEffectScopeBindableValueAsStatement:
    active: true
```

## Contribution guidelines
- Read [Detekt documentation](https://detekt.dev/docs/introduction/extensions/) about extending it.
- When adding a new rule, don't forget to include it RuleSetProvider (or create one if adding a new rule set).
- Note that rules are disabled by default, enable them in the Detekt configuration.

## Rules

### NoEffectScopeBindableValueAsStatement

This rule reports any bindable value from Arrow-kt (like Either)
that is used as an unbound statement inside scope that allows binding values.

**Active by default**: No

**Requires Type Resolution**

**Noncompliant code**:
```kotlin
fun doSomething() = Either.Left(Throwable("Oh no"))
fun doSomethingElse() = Either.Right(5)
return either {
    doSomething()
    doSomethingElse().bind()
}
```
**Compliant code**:
```kotlin
fun doSomething() = Either.Left(Throwable("Oh no"))
fun doSomethingElse() = Either.Right(5)
return either {
    doSomething().bind()
    doSomethingElse().bind()
}
```
