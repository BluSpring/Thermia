package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.data_component.TemperatureModifiersComponent;
import sylenthuntress.thermia.temperature.TemperatureModifier;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder {
    @Unique private static final ThreadLocal<ItemStack> thermia$stack = ThreadLocal.withInitial(() -> ItemStack.EMPTY);
    @Unique private static final ThreadLocal<AtomicInteger> thermia$slotIndex0 = ThreadLocal.withInitial(AtomicInteger::new);
    @Unique private static final ThreadLocal<AtomicInteger> thermia$slotIndex1 = ThreadLocal.withInitial(AtomicInteger::new);

    @SuppressWarnings("DataFlowIssue")
    @ModifyExpressionValue(
            method = "forEachModifier*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private Object thermia$allowTemperatureModifiers(Object obj) {
        var component = (ItemAttributeModifiers) obj;

        // Guard if no temperature modifiers are found on stack, or if they're hidden
        if (!this.has(ThermiaComponents.TEMPERATURE_MODIFIERS)
                || !this.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT).shows(ThermiaComponents.TEMPERATURE_MODIFIERS)) {
            return component;
        }

        // Add temperature modifiers as faux attribute modifiers
        for (TemperatureModifiersComponent.Entry entry
                : this.get(ThermiaComponents.TEMPERATURE_MODIFIERS).modifiers()) {
            component = component.withModifierAdded(
                    ThermiaAttributes.BASE_TEMPERATURE,
                    new AttributeModifier(
                            // Suffix is used to identify itself as a faux attribute
                            entry.modifier().id().withSuffix(".temperature_modifier"),
                            entry.modifier().amount(),
                            entry.modifier().operation().asAttributeOperation()
                    ),
                    entry.slot()
            );
        }

        thermia$slotIndex0.remove();
        thermia$slotIndex1.remove();
        return component;
    }

    @WrapMethod(method = "addAttributeTooltips")
    private void thermia$storeItemStack(Consumer<Component> consumer, TooltipDisplay display, @Nullable Player player, Operation<Void> original) {
        try {
            thermia$stack.set((ItemStack) (Object) this);
            original.call(consumer, display, player);
        } finally {
            thermia$stack.remove();
        }
    }

    @WrapOperation(
            method = "addAttributeTooltips",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/component/TooltipDisplay;shows(Lnet/minecraft/core/component/DataComponentType;)Z"
            )
    )
    private boolean thermia$allowTemperatureModifiers(TooltipDisplay instance, DataComponentType<?> component, Operation<Boolean> original) {
        if (this.has(ThermiaComponents.TEMPERATURE_MODIFIERS)) {
            return original.call(instance, component) || instance.shows(ThermiaComponents.TEMPERATURE_MODIFIERS);
        }

        return original.call(instance, component);
    }

    @WrapOperation(
            method = "lambda$addAttributeTooltips$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers$Display;apply(Ljava/util/function/Consumer;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V"
            )
    )
    private static void thermia$applyTemperatureModifiers(ItemAttributeModifiers.Display instance, Consumer<Component> consumer, @Nullable Player player, Holder<Attribute> attribute, AttributeModifier modifier, Operation<Void> original, @Local(argsOnly = true) EquipmentSlotGroup modifierSlot) {
        // Guard in case of regular attribute modifiers
        if (!modifier.id().getPath().endsWith(".temperature_modifier")) {
            // Prevent showing hidden attribute modifiers
            if (!thermia$stack.get().getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT).shows(DataComponents.ATTRIBUTE_MODIFIERS)) {
                return;
            }

            original.call(instance, consumer, player, attribute, modifier);
            return;
        }

        // Prevent similar tooltips from doubling up
        if (modifier.operation().ordinal() == 0) {
            if (thermia$slotIndex0.get().get() == modifierSlot.ordinal()) {
                return;
            }
            thermia$slotIndex0.get().incrementAndGet();
        } else if (modifier.operation().ordinal() == 1) {
            if (thermia$slotIndex0.get().get() == modifierSlot.ordinal()) {
                return;
            }
            thermia$slotIndex1.get().incrementAndGet();
        }

        // Combine similar modifiers into one tooltip
        double amount = 0;
        for (TemperatureModifiersComponent.Entry entry : thermia$stack.get().getOrDefault(ThermiaComponents.TEMPERATURE_MODIFIERS, TemperatureModifiersComponent.DEFAULT).modifiers()) {
            if (entry.slot() != modifierSlot
                    || entry.modifier().operation().asAttributeOperation() != modifier.operation()) {
                continue;
            }

            amount += entry.modifier().amount();
        }

        String temperatureScale = "temperature.scale.fahrenheit";
        switch (Thermia.CONFIG.temperatureScaleDisplay()) {
            case CELSIUS -> {
                amount *= 5.0 / 9.0;
                temperatureScale = "temperature.scale.celsius";
            }
            case KELVIN -> {
                amount *= 5.0 / 9.0;
                temperatureScale = "temperature.scale.kelvin";
            }
        }

        // Change the display amount based on operation
        final double displayAmount;
        if (modifier.operation().ordinal() == 1) {
            displayAmount = amount * 100.0;
        } else if (modifier.operation().ordinal() == 2) {
            consumer.accept(
                    Component.translatable(
                            "temperature.modifier.new",
                            amount
                    ).withStyle(ChatFormatting.BLUE)
            );
            return;
        } else {
            displayAmount = amount;
        }

        // Finally apply tooltip
        if (amount > 0.0) {
            consumer.accept(
                    Component.translatable(
                            "temperature.modifier.hot."
                                    + TemperatureModifier.Operation.asTemperatureOperation(modifier.operation())
                                    .getId(),
                            TemperatureModifiersComponent.DECIMAL_FORMAT.format(displayAmount)
                    ).append(
                            Component.translatable(temperatureScale)
                    ).append(
                            Component.translatable("temperature.symbol.fire", " ")
                    ).withStyle(ChatFormatting.GOLD)
            );
        } else {
            consumer.accept(
                    Component.translatable(
                            "temperature.modifier.cold."
                                    + TemperatureModifier.Operation.asTemperatureOperation(modifier.operation())
                                    .getId(),
                            TemperatureModifiersComponent.DECIMAL_FORMAT.format(-displayAmount)
                    ).append(
                            Component.translatable(temperatureScale)
                    ).append(
                            Component.translatable("temperature.symbol.snowflake", " ")
                    ).withStyle(ChatFormatting.AQUA)
            );
        }
    }
}
