package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.temperature.TemperatureHelper;

@SuppressWarnings("ConstantValue")
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @ModifyReturnValue(method = "isSprintingPossible", at = @At("RETURN"))
    private boolean thermia$disableSprinting(boolean original) {
        return original && !TemperatureHelper.getTemperatureManager((Player) (Object) this).isHyperthermic();
    }
}
