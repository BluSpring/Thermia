package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.temperature.TemperatureHelper;

@Mixin(RandomStrollGoal.class)
public class RandomStrollGoalMixin {
    @Shadow
    @Final
    protected PathfinderMob mob;

    @ModifyExpressionValue(
            method = "canUse",
            at = @At(
                value = "FIELD",
                target = "Lnet/minecraft/world/entity/ai/goal/RandomStrollGoal;interval:I",
                opcode = Opcodes.GETFIELD
            )
    )
    private int thermia$increaseChanceWithTemperature(int original) {
        var temperatureManager = TemperatureHelper.getTemperatureManager(this.mob);
        float chanceMultiplier = Math.min(0.01F, 1 - temperatureManager.normalizeWithinTemperateBounds(
                temperatureManager.getModifiedTemperature()
        ));

        return (int) Math.clamp(original * chanceMultiplier, 1, original);
    }
}
