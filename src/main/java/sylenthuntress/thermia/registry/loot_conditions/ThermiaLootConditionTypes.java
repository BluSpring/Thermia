package sylenthuntress.thermia.registry.loot_conditions;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.data.predicate.TemperatureLootCondition;

public class ThermiaLootConditionTypes {
    static {
        register(Thermia.modIdentifier("temperature"), TemperatureLootCondition.CODEC);
    }

    private static void register(Identifier id, MapCodec<? extends LootItemCondition> codec) {
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, id, codec);
    }

    public static void registerAll() {

    }
}
