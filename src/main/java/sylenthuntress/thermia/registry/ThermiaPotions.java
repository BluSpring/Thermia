package sylenthuntress.thermia.registry;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import sylenthuntress.thermia.Thermia;

public class ThermiaPotions {
    public static final Holder<Potion> FROST_RESISTANCE = register(
            "frost_resistance",
            new Potion("frost_resistance",
                    new MobEffectInstance(
                            ThermiaStatusEffects.FROST_RESISTANCE,
                            3600,
                            0
                    )
            )
    );
    public static final Holder<Potion> LONG_FROST_RESISTANCE = register(
            "long_frost_resistance",
            new Potion("frost_resistance",
                    new MobEffectInstance(
                            ThermiaStatusEffects.FROST_RESISTANCE,
                            9600
                    )
            )
    );

    private static Holder<Potion> register(String id, Potion potion) {
        return Registry.registerForHolder(BuiltInRegistries.POTION, Thermia.modIdentifier(id), potion);
    }

    public static void registerAll() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.addMix(
                    Potions.FIRE_RESISTANCE,
                    Items.FERMENTED_SPIDER_EYE,
                    FROST_RESISTANCE
            );

            builder.addMix(
                    FROST_RESISTANCE,
                    Items.REDSTONE,
                    LONG_FROST_RESISTANCE
            );
        });

    }
}
