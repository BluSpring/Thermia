package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.temperature.TemperatureHelper;

@Mixin(Gui.HeartType.class)
public class InGameHudHeartTypeMixin {
    @ModifyExpressionValue(method = "forPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isFrozen()Z"))
    private static boolean thermia$setFrozenHearts(boolean original, Player player) {
        return original || TemperatureHelper.getTemperatureManager(player).isHypothermic();
    }
}
