package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;

import java.util.Collection;

@Mixin(EffectsInInventory.class)
public class EffectsInInventoryMixin {
    @ModifyExpressionValue(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getActiveEffects()Ljava/util/Collection;"
            )
    )
    private Collection<MobEffectInstance> thermia(Collection<MobEffectInstance> original) {
        original.removeIf(effect -> effect.getEffect().is(effectKey ->
                (ThermiaStatusEffects.HYPOTHERMIA.is(effectKey)
                        && !Thermia.CONFIG.climateEffectDisplay.SHOW_HYPOTHERMIA())
                        || (ThermiaStatusEffects.HYPERTHERMIA.is(effectKey)
                        && !Thermia.CONFIG.climateEffectDisplay.SHOW_HYPERTHERMIA())
        ));

        return original;
    }

    @Unique private MobEffectInstance thermia$effect;

    @WrapOperation(method = "extractEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectsInInventory;extractText(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/Font;IIIIII)V"))
    private void thermia$storeCurrentEffect(EffectsInInventory instance, GuiGraphicsExtractor graphics, Component effectText, Component duration, Font font, int x0, int y0, int textureWidth, int yStep, int mouseX, int mouseY, Operation<Void> original, @Local(name = "effect") MobEffectInstance effect) {
        try {
            this.thermia$effect = effect;
            original.call(instance, graphics, effectText, duration, font, x0, y0, textureWidth, yStep, mouseX, mouseY);
        } finally {
            this.thermia$effect = null;
        }
    }

    @ModifyArgs(method = "extractText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)V", ordinal = 0))
    private void thermia$modifyDescription(Args args) {
        MobEffectInstance effect = thermia$effect;
        if (effect == null)
            return;

        if (!effect.getEffect().is(effectKey ->
                (ThermiaStatusEffects.HYPOTHERMIA.is(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPOTHERMIA_DISPLAY())
                        || (ThermiaStatusEffects.HYPERTHERMIA.is(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPERTHERMIA_DISPLAY())
        )) {
            return;
        }

        if (effect.isInfiniteDuration())
            args.set(3, ((int) args.get(3)) + 6);
        if (effect.getEffect().is(ThermiaStatusEffects.HYPOTHERMIA::is))
            args.set(4, ARGB.opaque(Mth.hsvToRgb(1.4F, Math.max(0, 0.6F - (effect.getAmplifier() * 0.17F)), 1F)));
        else args.set(4, ARGB.opaque(Mth.hsvToRgb(1.9F, 1F, Math.max(0.3F, 1F - (effect.getAmplifier() * 0.3F)))));
    }

    @ModifyArgs(method = "extractText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", ordinal = 0))
    private void thermia$disableDuration(Args args) {
        MobEffectInstance effect = thermia$effect;
        if (effect == null)
            return;

        if (!effect.getEffect().is(effectKey ->
                (ThermiaStatusEffects.HYPOTHERMIA.is(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPOTHERMIA_DISPLAY())
                        || (ThermiaStatusEffects.HYPERTHERMIA.is(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPERTHERMIA_DISPLAY())
        ) || !effect.isInfiniteDuration()) {
            return;
        }

        args.set(1, Component.empty());
    }

    @ModifyExpressionValue(method = "getEffectName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;getAmplifier()I", ordinal = 0))
    private int thermia$enableLowAmplifier(int original, MobEffectInstance effect) {
        return !effect.getEffect().is(effectKey ->
                (ThermiaStatusEffects.HYPOTHERMIA.is(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPOTHERMIA_DISPLAY())
                        || (ThermiaStatusEffects.HYPERTHERMIA.is(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPERTHERMIA_DISPLAY())
        ) ? original : Math.max(1, original);
    }

    @ModifyExpressionValue(method = "getEffectName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;getAmplifier()I", ordinal = 1))
    private int thermia$enableHighAmplifier(int original, MobEffectInstance effect) {
        return !effect.getEffect().is(effectKey ->
                (ThermiaStatusEffects.HYPOTHERMIA.is(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPOTHERMIA_DISPLAY())
                        || (ThermiaStatusEffects.HYPERTHERMIA.is(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPERTHERMIA_DISPLAY())
        ) && effect.getAmplifier() == 10 ? original : Math.max(9, original);
    }
}
