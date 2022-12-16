package com.wolt.arrow.detekt.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class ArrowRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "ArrowRuleSet"

    override fun instance(config: Config): RuleSet {
        return RuleSet(
            ruleSetId,
            listOf(
                NoEffectScopeBindableValueAsStatement(config),
            ),
        )
    }
}
