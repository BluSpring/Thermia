package sylenthuntress.thermia.registry.loot_conditions;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.data.predicate.TemperatureLootCondition;

public class ThermiaLootConditionTypes {
    public static final LootItemConditionType TEMPERATURE = register(Thermia.modIdentifier("temperature"), TemperatureLootCondition.CODEC);

    private static LootItemConditionType register(ResourceLocation id, MapCodec<? extends LootItemCondition> codec) {
        return Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, id, new LootItemConditionType(codec));
    }

    public static void registerAll() {

    }
}
