package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.temperature.TemperatureHelper;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private static Identifier BLUR_POST_CHAIN_ID;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private @Nullable Identifier postEffectId;

    @Shadow
    protected abstract void setPostEffect(Identifier id);

    @Shadow
    public abstract void clearPostEffect();

    @Inject(method = "render", at = @At("HEAD"))
    private void thermia$loadHyperthermiaShader(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        if (minecraft.getCameraEntity() instanceof LivingEntity livingEntity &&
                TemperatureHelper.getTemperatureManager(livingEntity).shouldBlurVision()) {
            this.setPostEffect(BLUR_POST_CHAIN_ID);
        } else if (postEffectId == BLUR_POST_CHAIN_ID)
            this.clearPostEffect();
    }

    @ModifyExpressionValue(method = "extractGui", at = @At(value = "NEW", target = "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/state/gui/GuiRenderState;II)Lnet/minecraft/client/gui/GuiGraphicsExtractor;"))
    private GuiGraphicsExtractor thermia$renderRedVision(GuiGraphicsExtractor context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && TemperatureHelper.getTemperatureManager(client.player).isHypothermic()) {
            int color = Mth.hsvToArgb(
                    0,
                    0,
                    1,
                    15
            );
            context.fillGradient(
                    0,
                    0,
                    client.getWindow().getScreenWidth(),
                    client.getWindow().getScreenWidth(),
                    color,
                    color
            );
        }
        return context;
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;portalEffectIntensity:F", opcode = Opcodes.GETFIELD))
    private float thermia$wobbleVision(float original) {
        return TemperatureHelper.getTemperatureManager(this.minecraft.player).doHeatEffects()
                ? Math.max(0.1F, original)
                : original;
    }
}
