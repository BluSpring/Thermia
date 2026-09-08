package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
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
    private static ResourceLocation BLUR_POST_CHAIN_ID;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private @Nullable ResourceLocation postEffectId;

    @Shadow
    protected abstract void setPostEffect(ResourceLocation id);

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

    @WrapOperation(method = "render", at = @At(value = "NEW", target = "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)Lnet/minecraft/client/gui/GuiGraphics;"))
    private GuiGraphics thermia$renderRedVision(Minecraft client, MultiBufferSource.BufferSource vertexConsumers, Operation<GuiGraphics> original) {
        GuiGraphics context = original.call(client, vertexConsumers);
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

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;spinningEffectIntensity:F"))
    private float thermia$wobbleVision(float original) {
        return TemperatureHelper.getTemperatureManager(this.minecraft.player).doHeatEffects()
                ? Math.max(0.1F, original)
                : original;
    }
}
