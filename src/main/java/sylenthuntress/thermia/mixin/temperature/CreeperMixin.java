package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;

import java.util.Collection;

@Mixin(Creeper.class)
public class CreeperMixin {
    @ModifyExpressionValue(
            method = "spawnLingeringCloud",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Creeper;getActiveEffects()Ljava/util/Collection;"
            )
    )
    private Collection<MobEffectInstance> thermia$cancelClimateEffectsCloud(Collection<MobEffectInstance> original) {
        original.removeIf(effect ->
                effect.getEffect().is(key ->
                        ThermiaStatusEffects.HYPOTHERMIA.is(key)
                                || ThermiaStatusEffects.HYPERTHERMIA.is(key)
                )
        );

        return original;
    }
}
