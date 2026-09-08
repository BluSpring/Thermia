package sylenthuntress.thermia.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.util.CommonColors;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.status_effects.FrostResistanceEffect;
import sylenthuntress.thermia.registry.status_effects.HyperthermiaEffect;
import sylenthuntress.thermia.registry.status_effects.HypothermiaEffect;
import sylenthuntress.thermia.registry.status_effects.ThermoregulationEffect;

public class ThermiaStatusEffects {
    public static final Holder<MobEffect> HYPOTHERMIA = register(
            "hypothermia",
            new HypothermiaEffect(MobEffectCategory.HARMFUL, 12624973)
    );
    public static final Holder<MobEffect> HYPERTHERMIA = register(
            "hyperthermia",
            new HyperthermiaEffect(MobEffectCategory.HARMFUL, 14367241)
    );
    public static final Holder<MobEffect> FROST_RESISTANCE = register(
            "frost_resistance",
            new FrostResistanceEffect(MobEffectCategory.BENEFICIAL, 12445695)
    );
    public static final Holder<MobEffect> THERMOREGULATION = register(
            "thermoregulation",
            new ThermoregulationEffect(MobEffectCategory.BENEFICIAL, CommonColors.LIGHT_GRAY)
    );

    private static Holder<MobEffect> register(String id, MobEffect statusEffect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Thermia.modIdentifier(id), statusEffect);
    }

    public static void registerAll() {

    }
}
