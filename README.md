# Detekt rules for Arrow

This project contains [Detekt](https://detekt.dev) rules for the [Arrow](https://arrow-kt.io/) ecosystem.

## How to use it

1. Add this project as a Detekt plugin to `build.gradle.kts`
```kotlin
dependencies {
    detektPlugins("com.wolt.arrow.detekt:rules:*VERSION*")
}
```
2. Enable and configure rules in Detekt configuration, example:
```yaml
ArrowRuleSet:
  NoEffectScopeBindableValueAsStatement:
    active: true
```

## Contribution guidelines
- Read [Detekt documentation](https://detekt.dev/docs/introduction/extensions/) about extending it.
- When adding a new rule, don't forget to include it RuleSetProvider (or create one if adding a new rule set).
- Note that rules are disabled by default, enable them in the Detekt configuration.

## Releasing

1. [Draft a new release](https://github.com/woltapp/arrow-detekt-rules/releases/new) on GitHub.
2. Create a new tag (e.g. `v0.1.1` if the previous was `v0.1.0` and you want to bump the patch version).
3. Auto-generate release notes.
4. Publish the release.
5. This will trigger a CI workflow to build and publish the library to Sonatype Nexus. You can see it [here](https://github.com/woltapp/arrow-detekt-rules/actions). After publishing succeeds, login to [Sonatype Nexus](https://oss.sonatype.org/) and select "Staging repositories".
6. Check the content of the new entry.
7. If there are no issues, press "Close". It will run the checks required for sync with Maven Central. If there are issues, press "Drop".
8. Press "Release" to sync with Maven Central (Or "Drop" if there are problems with content/checks).

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
