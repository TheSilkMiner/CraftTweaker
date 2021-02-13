package com.blamejared.crafttweaker.impl.loot.conditions.vanilla;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.loot.conditions.ILootCondition;
import com.blamejared.crafttweaker.impl.loot.conditions.ILootConditionTypeBuilder;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("crafttweaker.api.loot.conditions.vanilla.RandomChanceWithLooting")
@Document("vanilla/api/loot/conditions/vanilla/RandomChanceWithLooting")
public final class RandomChanceWithLootingLootConditionBuilder implements ILootConditionTypeBuilder {
    private float chance;
    private float lootingMultiplier;

    RandomChanceWithLootingLootConditionBuilder() {}

    @ZenCodeType.Method
    public RandomChanceWithLootingLootConditionBuilder withChance(final float chance) {
        this.chance = chance;
        return this;
    }

    @ZenCodeType.Method
    public RandomChanceWithLootingLootConditionBuilder withLootingMultiplier(final float lootingMultiplier) {
        this.lootingMultiplier = lootingMultiplier;
        return this;
    }

    @Override
    public ILootCondition finish() {
        if (this.chance >= 1.0) {
            CraftTweakerAPI.logWarning(
                    "Chance in a 'RandomChanceWithLooting' condition is a number that is equal to or higher than 1.0 (currently: %f): this condition will always match!",
                    this.chance
            );
        }
        if (this.chance <= 0.0) {
            CraftTweakerAPI.logWarning(
                    "Chance in a 'RandomChanceWithLooting' condition is a number that is equal to or lower than 0.0 (currently: %f): this condition will never match!",
                    this.chance
            );
        }
        if (this.lootingMultiplier <= 0.0) {
            CraftTweakerAPI.logWarning(
                    "Looting modifier in a 'RandomChanceWithLooting' condition is a number that is equal to or lower than 0.0 (currently: %f): this condition will never match!",
                    this.lootingMultiplier
            );
        }
        return context -> context.getRandom().nextFloat() < (this.chance + context.getLootingModifier() * this.lootingMultiplier);
    }
}
