package sylenthuntress.thermia.mixin.temperature;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.data.ThermiaTags;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;
import sylenthuntress.thermia.registry.data_component.ConsumableTemperatureComponent;
import sylenthuntress.thermia.registry.data_component.TemperatureModifiersComponent;
import sylenthuntress.thermia.temperature.TemperatureModifier;

import java.util.Arrays;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder {
    @Shadow
    public abstract boolean is(TagKey<Item> tag);

    @Shadow
    @Nullable
    public abstract <T> T set(DataComponentType<? super T> type, @Nullable T value);

    @Shadow
    public abstract boolean isEnchanted();

    @Shadow
    public abstract ItemEnchantments getEnchantments();

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V",
            at = @At("TAIL")
    )
    private void thermia$applyDefaultComponents(ItemLike item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        if (!Thermia.SERVER_LOADED) {
            return;
        }

        final TemperatureModifiersComponent modifiers = this.getOrDefault(
                ThermiaComponents.TEMPERATURE_MODIFIERS,
                TemperatureModifiersComponent.DEFAULT
        );
        thermia$calculateTemperatureModifiers(modifiers);

        if (!this.has(DataComponents.CONSUMABLE)) {
            return;
        }

        if (this.has(DataComponents.CONSUMABLE)
                && !this.has(ThermiaComponents.CONSUMABLE_TEMPERATURE)) {
            final var component = this.get(DataComponents.CONSUMABLE);
            final double[] degrees = thermia$calculateConsumableTemperatures();

            if (Arrays.stream(degrees).anyMatch(value -> value != 0)) {
                this.set(
                        ThermiaComponents.CONSUMABLE_TEMPERATURE,
                        new ConsumableTemperatureComponent(degrees)
                );
            }

            if (this.is(ThermiaTags.Item.Consumable.APPLIES_FROST_RESISTANCE)) {
                @SuppressWarnings("DataFlowIssue") final var consumeEffects = component.onConsumeEffects();

                int duration = consumeEffects.stream().mapToInt(consumeEffect -> {
                    if (consumeEffect instanceof ApplyStatusEffectsConsumeEffect consumeStatusEffect) {
                        return consumeStatusEffect.effects().stream().mapToInt(effect
                                -> effect.getEffect() == MobEffects.FIRE_RESISTANCE
                                ? effect.getDuration()
                                : 0
                        ).sum();
                    }

                    return 0;
                }).sum();

                if (duration == 0) {
                    duration = 1200;
                }

                consumeEffects.add(
                        new ApplyStatusEffectsConsumeEffect(
                                new MobEffectInstance(
                                        ThermiaStatusEffects.FROST_RESISTANCE,
                                        duration
                                )
                        )
                );
            }
        }
    }

    @Unique
    private double[] thermia$calculateConsumableTemperatures() {
        double[] temperatures = {0, 0, 0};

        if (this.is(ThermiaTags.Item.Consumable.COLD_FOODS)) {
            temperatures[0] -= 2;
        }
        if (this.is(ThermiaTags.Item.Consumable.REFRESHING_FOODS)) {
            temperatures[0] -= 0.5;
        }
        if (this.is(ThermiaTags.Item.Consumable.WARM_FOODS)) {
            temperatures[0] += 0.5;
        }
        if (this.is(ThermiaTags.Item.Consumable.HOT_FOODS)) {
            temperatures[0] += 2;
        }

        return temperatures;
    }

    @Unique
    private void thermia$calculateTemperatureModifiers(TemperatureModifiersComponent component) {
        if (this.is(ThermiaTags.Item.Equippable.COLD_WHEN_HELD)) {
            this.set(
                    ThermiaComponents.TEMPERATURE_MODIFIERS,
                    component.with(
                            new TemperatureModifier(
                                    Thermia.modIdentifier("cold_when_held"),
                                    -1,
                                    TemperatureModifier.Operation.ADD_VALUE
                            ),
                            EquipmentSlotGroup.HAND
                    )
            );
        }
        if (this.is(ThermiaTags.Item.Equippable.HOT_WHEN_HELD)) {
            this.set(
                    ThermiaComponents.TEMPERATURE_MODIFIERS,
                    component.with(
                            new TemperatureModifier(
                                    Thermia.modIdentifier("hot_when_held"),
                                    1,
                                    TemperatureModifier.Operation.ADD_VALUE
                            ),
                            EquipmentSlotGroup.HAND
                    )
            );
        }

        // Guard-return for unenchanted items
        if (!this.isEnchanted()) {
            return;
        }

        // Apply default enchantment temperature modifiers
        final var enchantments = this.getEnchantments();
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            for (EquipmentSlotGroup slot : enchantment.value().definition().slots()) {
                if (enchantment.is(ThermiaTags.Enchantment.PROVIDES_CHILL)) {
                    this.set(
                            ThermiaComponents.TEMPERATURE_MODIFIERS,
                            component.with(
                                    new TemperatureModifier(
                                            Thermia.modIdentifier(
                                                    "enchantment."
                                                            + enchantment.getRegisteredName()
                                                            .replaceFirst("[A-Za-z0-9]+:", "")
                                                            + ".chill"
                                            ),
                                            -(2 + 0.25 * enchantments.getLevel(enchantment)),
                                            TemperatureModifier.Operation.ADD_VALUE
                                    ),
                                    slot
                            )
                    );
                }

                if (enchantment.is(ThermiaTags.Enchantment.PROVIDES_WARMTH)) {
                    this.set(
                            ThermiaComponents.TEMPERATURE_MODIFIERS,
                            component.with(
                                    new TemperatureModifier(
                                            Thermia.modIdentifier(
                                                    "enchantment."
                                                            + enchantment.getRegisteredName()
                                                            .replaceFirst("[A-Za-z0-9]+:", "")
                                                            + ".warmth"
                                            ),
                                            2 + 0.25 * enchantments.getLevel(enchantment),
                                            TemperatureModifier.Operation.ADD_VALUE
                                    ),
                                    slot
                            )
                    );
                }
            }
        }
    }
}
