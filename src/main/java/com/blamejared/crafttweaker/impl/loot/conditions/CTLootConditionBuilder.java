package com.blamejared.crafttweaker.impl.loot.conditions;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.loot.ILootCondition;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@ZenRegister
@ZenCodeType.Name("crafttweaker.api.loot.conditions.LootConditionBuilder")
@Document("vanilla/api/loot/conditions/LootConditionBuilder")
public final class CTLootConditionBuilder {
    private static final Map<Class<? extends ILootConditionTypeBuilder>, Function<CTLootConditionBuilder, ? extends ILootConditionTypeBuilder>> BUILDERS = new HashMap<>();

    private final List<ILootCondition> conditions;

    private CTLootConditionBuilder() {
        this.conditions = new ArrayList<>();
    }

    // Registration
    public static <T extends ILootConditionTypeBuilder> void register(final Class<T> typeToken, final Function<CTLootConditionBuilder, T> creator) {
        if (BUILDERS.containsKey(typeToken)) {
            throw new IllegalStateException("A builder for the given type '" + typeToken.getName() + "' was already registered");
        }
        BUILDERS.put(typeToken, creator);
    }

    public static <T extends ILootConditionTypeBuilder> void register(final Class<T> typeToken, final Supplier<T> creator) {
        register(typeToken, ignore -> creator.get());
    }

    static {
        // Vanilla
        register(InvertedLootConditionTypeBuilder.class, InvertedLootConditionTypeBuilder::new);
        register(AlternativeLootConditionTypeBuilder.class, AlternativeLootConditionTypeBuilder::new);
        register(RandomChanceLootConditionBuilder.class, RandomChanceLootConditionBuilder::new);
        register(RandomChanceWithLootingLootConditionBuilder.class, RandomChanceWithLootingLootConditionBuilder::new);
        // TODO("ENTITY_PROPERTIES")
        register(KilledByPlayerLootConditionTypeBuilder.class, () -> KilledByPlayerLootConditionTypeBuilder.INSTANCE);
        register(EntityScoresLootConditionTypeBuilder.class, EntityScoresLootConditionTypeBuilder::new);
        register(BlockStatePropertyLootConditionTypeBuilder.class, BlockStatePropertyLootConditionTypeBuilder::new);
        register(MatchToolLootConditionBuilder.class, MatchToolLootConditionBuilder::new);
        register(TableBonusLootConditionTypeBuilder.class, TableBonusLootConditionTypeBuilder::new);
        register(SurvivesExplosionLootConditionTypeBuilder.class, () -> SurvivesExplosionLootConditionTypeBuilder.INSTANCE);
        // TODO("DAMAGE_SOURCE_PROPERTIES")
        // TODO("LOCATION_CHECK")
        register(WeatherCheckLootConditionTypeBuilder.class, WeatherCheckLootConditionTypeBuilder::new);
        register(ReferenceLootConditionTypeBuilder.class, ReferenceLootConditionTypeBuilder::new);
        register(TimeCheckLootConditionTypeBuilder.class, TimeCheckLootConditionTypeBuilder::new);
        // TODO("LOOT_TABLE_ID, pending Forge PR")

        // CrT
        register(JsonLootConditionTypeBuilder.class, JsonLootConditionTypeBuilder::new);
    }

    // Creation
    @ZenCodeType.Method
    public static CTLootConditionBuilder create() {
        return new CTLootConditionBuilder();
    }

    @ZenCodeType.Method
    public static CTLootConditionBuilder createInAnd() {
        return create();
    }

    @ZenCodeType.Method
    public static CTLootConditionBuilder createInOr(final Consumer<AlternativeLootConditionTypeBuilder> lender) {
        return createForSingle(AlternativeLootConditionTypeBuilder.class, lender);
    }

    @ZenCodeType.Method
    public static <T extends ILootConditionTypeBuilder> CTLootConditionBuilder createForSingle(final Class<T> reifiedType, final Consumer<T> lender) {
        final CTLootConditionBuilder base = create();
        base.add(reifiedType, lender);
        return base;
    }

    @ZenCodeType.Method
    public static <T extends ILootConditionTypeBuilder> ILootCondition makeSingle(final Class<T> reifiedType, final Consumer<T> lender) {
        return createForSingle(reifiedType, lender).single();
    }

    @ZenCodeType.Method
    public static ILootCondition makeJson(final ResourceLocation type, final IData data) {
        return makeSingle(JsonLootConditionTypeBuilder.class, builder -> builder.withJson(type, data));
    }

    @ZenCodeType.Method
    public static ILootCondition makeJson(final String type, final IData data) {
        return makeSingle(JsonLootConditionTypeBuilder.class, builder -> builder.withJson(type, data));
    }

    // Usage
    @ZenCodeType.Method
    public <T extends ILootConditionTypeBuilder> CTLootConditionBuilder add(final Class<T> reifiedType) {
        return this.add(reifiedType, ignore -> {});
    }

    @ZenCodeType.Method
    public <T extends ILootConditionTypeBuilder> CTLootConditionBuilder add(final Class<T> reifiedType, final Consumer<T> lender) {
        final ILootCondition built = this.make(reifiedType, "main loot builder", lender);
        if (built != null) this.conditions.add(built);
        return this;
    }

    final <T extends ILootConditionTypeBuilder> ILootCondition make(final Class<T> type, final String id, final Consumer<T> lender) {
        final T builder;
        try {
            builder = this.findFor(type);
        } catch (final NullPointerException | ClassCastException e) {
            CraftTweakerAPI.logThrowing("Unable to create a loot condition builder for type '{}'", e, type.getName());
            return null;
        }

        try {
            lender.accept(builder);
        } catch (final ClassCastException e) {
            CraftTweakerAPI.logThrowing("Unable to pass a loot condition builder for type '{}' to lender", e, type.getName());
            return null;
        } catch (final Exception e) {
            CraftTweakerAPI.logThrowing("An error has occurred while populating the builder for type '{}'", e, type.getName());
            return null;
        }

        try {
            return builder.finish();
        } catch (final IllegalStateException e) {
            CraftTweakerAPI.logThrowing("Unable to add a loot condition of type '{}' to '{}' due to an invalid builder state", e, type.getName(), id);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    final <T extends ILootConditionTypeBuilder> T findFor(final Class<T> type) {
        final Function<CTLootConditionBuilder, ? extends ILootConditionTypeBuilder> builderCreator = BUILDERS.get(type);
        return Objects.requireNonNull((T) builderCreator.apply(this));
    }

    @ZenCodeType.Method
    public ILootCondition[] build() {
        return this.conditions.toArray(new ILootCondition[0]);
    }

    ILootCondition single() {
        return this.conditions.size() != 1? null : this.conditions.get(0);
    }
}
