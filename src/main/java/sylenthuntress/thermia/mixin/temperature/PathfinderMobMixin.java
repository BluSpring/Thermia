package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.temperature.TemperatureHelper;

@Mixin(PathfinderMob.class)
public class PathfinderMobMixin {
    @ModifyReturnValue(
            method = "getWalkTargetValue(Lnet/minecraft/core/BlockPos;)F",
            at = @At("RETURN")
    )
    private float thermia$modifyFavorWithTemperature(float original, BlockPos pos) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (TemperatureHelper.lacksTemperature(entity)) {
            return original;
        }

        var temperatureManager = TemperatureHelper.getTemperatureManager(entity);
        float temperatureFavor = temperatureManager.distanceFromTemperateBounds(
                (TemperatureHelper.getBlockTemperature(entity.level(), pos)
                        + temperatureManager.getModifiedTemperature()) / 2
        ) * 0.1F;

        return temperatureManager.isHyperthermic() || temperatureManager.isHypothermic()
                ? original - temperatureFavor + 1
                : original - temperatureFavor;
    }
}
