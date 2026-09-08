package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;
import sylenthuntress.thermia.temperature.TemperatureHelper;

@SuppressWarnings("DataFlowIssue")
@Mixin(Gui.class)
public class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyExpressionValue(method = "renderCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getFrozenTicks()I"))
    private int thermia$enableFrozenOverlay(int original) {
        return TemperatureHelper.getTemperatureManager(this.minecraft.player).isHypothermic() ? 1 : original;
    }

    @ModifyExpressionValue(method = "renderCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getFreezingScale()F"))
    private float thermia$incrementFrozenOverlay(float original) {
        return TemperatureHelper.getTemperatureManager(this.minecraft.player).isHypothermic()
                ? Math.min(1F, 0.01F + (0.99F * this.minecraft.player.getEffect(ThermiaStatusEffects.HYPOTHERMIA).getAmplifier() * 0.1F))
                : original;
    }
}
