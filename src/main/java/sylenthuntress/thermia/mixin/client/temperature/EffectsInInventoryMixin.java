package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;

import java.util.Collection;

@Mixin(EffectsInInventory.class)
public class EffectsInInventoryMixin {
    @ModifyExpressionValue(
            method = "renderEffects(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getStatusEffects()Ljava/util/Collection;"
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

    @ModifyArgs(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I", ordinal = 0))
    private void thermia$modifyDescription(Args args, @Local MobEffectInstance effect) {
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
            args.set(4, Mth.hsvToRgb(1.4F, Math.max(0, 0.6F - (effect.getAmplifier() * 0.17F)), 1F));
        else args.set(4, Mth.hsvToRgb(1.9F, 1F, Math.max(0.3F, 1F - (effect.getAmplifier() * 0.3F))));
    }

    @ModifyArgs(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I", ordinal = 1))
    private void thermia$disableDuration(Args args, @Local MobEffectInstance effect) {
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
