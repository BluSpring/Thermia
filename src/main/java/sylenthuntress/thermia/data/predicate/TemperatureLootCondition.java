package sylenthuntress.thermia.data.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.util.context.ContextKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import sylenthuntress.thermia.registry.loot_conditions.ThermiaLootConditionTypes;

import java.util.Optional;
import java.util.Set;

public record TemperatureLootCondition(Optional<EntityTemperaturePredicate> entityPredicate,
                                       Optional<LocationTemperaturePredicate> locationPredicate,
                                       Optional<LootContext.EntityTarget> entity,
                                       BlockPos offset) implements LootItemCondition {
    private static final MapCodec<BlockPos> OFFSET_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codec.INT.optionalFieldOf("offsetX", 0).forGetter(Vec3i::getX),
                            Codec.INT.optionalFieldOf("offsetY", 0).forGetter(Vec3i::getY),
                            Codec.INT.optionalFieldOf("offsetZ", 0).forGetter(Vec3i::getZ)
                    )
                    .apply(instance, BlockPos::new)
    );

    public static final MapCodec<TemperatureLootCondition> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            EntityTemperaturePredicate.CODEC.optionalFieldOf("entity_predicate").forGetter(TemperatureLootCondition::entityPredicate),
                            LocationTemperaturePredicate.CODEC.optionalFieldOf("location_predicate").forGetter(TemperatureLootCondition::locationPredicate),
                            LootContext.EntityTarget.CODEC.optionalFieldOf("entity").forGetter(TemperatureLootCondition::entity),
                            OFFSET_CODEC.forGetter(TemperatureLootCondition::offset)
                    )
                    .apply(instance, TemperatureLootCondition::new)
    );

    public static LootItemCondition.Builder create(LootContext.EntityTarget entity) {
        return builder(entity, EntityTemperaturePredicate.Builder.create(), LocationTemperaturePredicate.Builder.create());
    }

    public static LootItemCondition.Builder builder(LootContext.EntityTarget entity, EntityTemperaturePredicate.Builder entityPredicateBuilder, LocationTemperaturePredicate.Builder locationPredicateBuilder) {
        return () -> new TemperatureLootCondition(
                Optional.of(entityPredicateBuilder.build()),
                Optional.of(locationPredicateBuilder.build()),
                Optional.of(entity),
                BlockPos.ZERO
        );
    }

    @Override
    public LootItemConditionType getType() {
        return ThermiaLootConditionTypes.TEMPERATURE;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return this.entity.map(entityTarget -> Set.of(
                LootContextParams.ORIGIN,
                entityTarget.getParam()
        )).orElseGet(() -> Set.of(LootContextParams.ORIGIN));
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "DataFlowIssue"})
    public boolean test(LootContext lootContext) {
        Vec3 origin = lootContext.getOptionalParameter(LootContextParams.ORIGIN);
        return (this.entityPredicate.isEmpty() || this.entityPredicate.get().test(lootContext.getOptionalParameter(this.entity.get().getParam())))
                && (this.locationPredicate.isEmpty() || this.locationPredicate.get().test(lootContext.getLevel(), BlockPos.containing(origin)));
    }
}
