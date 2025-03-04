package juuxel.adorn.lib

import juuxel.adorn.AdornCommon
import juuxel.adorn.config.Config
import juuxel.adorn.platform.PlatformBridges
import net.minecraft.world.GameRules
import net.minecraft.world.GameRules.BooleanRule
import net.minecraft.world.GameRules.Category
import net.minecraft.world.GameRules.Key
import net.minecraft.world.GameRules.Rule
import net.minecraft.world.GameRules.Type

object AdornGameRules {
    @JvmField
    val SKIP_NIGHT_ON_SOFAS: Key<BooleanRule> = register("skipNightOnSofas", Category.PLAYER, createBooleanRule { it.skipNightOnSofas })

    @JvmField
    val INFINITE_KITCHEN_SINKS: Key<BooleanRule> = register("infiniteKitchenSinks", Category.MISC, createBooleanRule { it.infiniteKitchenSinks })

    @JvmStatic
    fun init() {
    }

    private inline fun createBooleanRule(default: (Config.GameRuleDefaults) -> Boolean): Type<BooleanRule> =
        BooleanRule.create(default(PlatformBridges.configManager.config.gameRuleDefaults))

    // <T extends Rule<T>> Key<T> register(String name, Category category, Type<T> type)
    private fun <T : Rule<T>> register(name: String, category: Category, type: Type<T>): Key<T> =
        GameRules.register("${AdornCommon.NAMESPACE}:$name", category, type)
}
