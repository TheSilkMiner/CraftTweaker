package com.blamejared.crafttweaker.impl.loot.modifiers;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.loot.modifiers.ILootModifier;
import com.blamejared.crafttweaker.impl.loot.CTLootManager;
import com.blamejared.crafttweaker.impl.loot.conditions.CTLootConditionBuilder;
import com.blamejared.crafttweaker.impl.loot.conditions.vanilla.BlockStatePropertyLootConditionTypeBuilder;
import com.blamejared.crafttweaker.impl.loot.conditions.vanilla.MatchToolLootConditionBuilder;
import com.blamejared.crafttweaker.impl.predicate.StatePropertiesPredicate;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.block.Block;
import org.openzen.zencode.java.ZenCodeType;

import java.util.function.Consumer;

@Document("vanilla/api/blocks/MCBlock")
@ZenCodeType.Expansion("crafttweaker.api.blocks.MCBlock")
@ZenRegister
public class ModifierBlockExpansion {
    @ZenCodeType.Method
    public static void addDrop(final Block $this, final String uniqueId, final IItemStack stack) {
        addLootModifier($this, uniqueId, CommonLootModifiers.add(stack));
    }
    
    @ZenCodeType.Method
    public static void addDrops(final Block $this, final String uniqueId, final IItemStack... stacks) {
        addLootModifier($this, uniqueId, CommonLootModifiers.addAll(stacks));
    }
    
    @ZenCodeType.Method
    public static void addLootModifier(final Block $this, final String name, final ILootModifier modifier) {
        CTLootManager.LOOT_MANAGER.getModifierManager().register(
                name,
                CTLootConditionBuilder.create().add(BlockStatePropertyLootConditionTypeBuilder.class, blockStateProps -> blockStateProps.withBlock($this)),
                modifier);
    }
    
    @ZenCodeType.Method
    public static void addStateDrop(final Block $this, final String uniqueId, final IItemStack stack, final Consumer<StatePropertiesPredicate> statePredicate) {
        addStateLootModifier($this, uniqueId, CommonLootModifiers.add(stack), statePredicate);
    }
    
    @ZenCodeType.Method
    public static void addStateLootModifier(final Block $this, final String name, final ILootModifier modifier, final Consumer<StatePropertiesPredicate> statePredicate) {
        CTLootManager.LOOT_MANAGER.getModifierManager().register(
                name,
                CTLootConditionBuilder.create().add(BlockStatePropertyLootConditionTypeBuilder.class, bs -> bs.withBlock($this).withStatePropertiesPredicate(statePredicate)),
                modifier
        );
    }
    
    @ZenCodeType.Method
    public static void addToolDrop(final Block $this, final String uniqueId, final IItemStack tool, final IItemStack stack) {
        addToolLootModifier($this, uniqueId, tool, CommonLootModifiers.add(stack));
    }
    
    @ZenCodeType.Method
    public static void addToolDrops(final Block $this, final String uniqueId, final IItemStack tool, final IItemStack... stacks) {
        addToolLootModifier($this, uniqueId, tool, CommonLootModifiers.addAll(stacks));
    }
    
    @ZenCodeType.Method
    public static void addToolLootModifier(final Block $this, final String name, final IItemStack tool, final ILootModifier modifier) {
        CTLootManager.LOOT_MANAGER.getModifierManager().register(
                name,
                CTLootConditionBuilder.create()
                        .add(BlockStatePropertyLootConditionTypeBuilder.class, bs -> bs.withBlock($this))
                        .add(MatchToolLootConditionBuilder.class, mt -> mt.withPredicate(item -> {
                            item.withItem(tool);
                            if (tool.hasTag()) {
                                item.withDataPredicate(nbt -> nbt.withData(tool.getTag()));
                            }
                        })),
                modifier
        );
    }
}
