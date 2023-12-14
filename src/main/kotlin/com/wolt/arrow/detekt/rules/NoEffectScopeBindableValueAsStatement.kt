package com.wolt.arrow.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.DetektVisitor
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForReceiver
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelectorOrThis
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.bindingContextUtil.isUsedAsStatement
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.supertypes

@RequiresTypeResolution
class NoEffectScopeBindableValueAsStatement(config: Config) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Defect,
        "Having a bindable value inside effect scope used as a statement " +
            "discards its result and usually represents an error.",
        Debt.FIVE_MINS,
    )

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        if (bindingContext == BindingContext.EMPTY) {
            return
        }

        val receiverType = bindingContext[BindingContext.FUNCTION, function]
            ?.extensionReceiverParameter
            ?.type
            ?: return

        val isEffectScope = isBindableScope(receiverType) || receiverType.supertypes().any(::isBindableScope)

        if (isEffectScope) {
            EffectScopeVisitor().visitNamedFunction(function)
        }
    }

    override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression) {
        super.visitLambdaExpression(lambdaExpression)

        if (bindingContext == BindingContext.EMPTY) {
            return
        }

        val firstArgumentType = bindingContext
            .getType(lambdaExpression)
            ?.arguments
            ?.firstOrNull()
            ?.type
            ?: return

        val isEffectScope = isBindableScope(firstArgumentType) || firstArgumentType.supertypes().any(::isBindableScope)

        if (isEffectScope) {
            EffectScopeVisitor().visitLambdaExpression(lambdaExpression)
        }
    }

    private fun isBindableScope(type: KotlinType): Boolean = type
        .constructor
        .declarationDescriptor
        ?.fqNameSafe
        ?.let {
            it == FqName("arrow.core.continuations.EffectScope") ||
                it == FqName("arrow.core.continuations.EagerEffectScope") ||
                it == FqName("arrow.core.raise.Raise")
        }
        ?: false

    private inner class EffectScopeVisitor : DetektVisitor() {
        override fun visitExpression(expression: KtExpression) {
            super.visitExpression(expression)

            if (expression is KtBlockExpression) {
                return
            }

            val expressionType = bindingContext.getType(expression) ?: return
            val isBindable = isBindable(expressionType) || expressionType.supertypes().any(::isBindable)

            if (!isBindable) {
                return
            }

            if (isLastChainedStatement(expression)) {
                val qualifiedExpressionForSelectorOrThis = expression.getQualifiedExpressionForSelectorOrThis()
                if (qualifiedExpressionForSelectorOrThis != expression &&
                    isLastChainedStatement(qualifiedExpressionForSelectorOrThis)
                ) {
                    return
                }

                report(
                    CodeSmell(
                        issue,
                        Entity.from(expression),
                        "This expression could be bound using the effect scope, but it is left unbound. " +
                            "The value of this expression is discarded.",
                    ),
                )
            }
        }

        private fun isLastChainedStatement(expression: KtExpression) =
            expression.isUsedAsStatement(bindingContext) && getNextChainedCall(expression) == null

        private fun getNextChainedCall(expression: KtExpression): KtExpression? =
            expression
                .getQualifiedExpressionForSelectorOrThis()
                .getQualifiedExpressionForReceiver()
                ?.selectorExpression

        private fun isBindable(type: KotlinType): Boolean {
            val isBindableFqNames =
                type
                    .constructor
                    .declarationDescriptor
                    ?.fqNameSafe
                    ?.let { it in BindableFqNames } ?: false

            val isRaiseExtensionFunction = type.annotations.hasAnnotation(FqName("kotlin.ExtensionFunctionType")) &&
                type
                    .arguments
                    .firstOrNull()
                    ?.type
                    ?.constructor
                    ?.declarationDescriptor
                    ?.fqNameSafe == FqName("arrow.core.raise.Raise")

            return isBindableFqNames || isRaiseExtensionFunction
        }
    }

    companion object {
        private val BindableFqNames: Set<FqName> = setOf(
            FqName("arrow.core.Either"),
            FqName("arrow.core.Validated"),
            FqName("arrow.core.continuations.EagerEffect"),
            FqName("arrow.core.continuations.Effect"),
            FqName("arrow.core.Ior"),
            FqName("arrow.core.Option"),
            FqName("kotlin.Result"),
        )
    }
}
