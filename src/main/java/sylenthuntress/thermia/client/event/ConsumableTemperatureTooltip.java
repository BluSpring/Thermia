package sylenthuntress.thermia.client.event;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.data_component.TemperatureModifiersComponent;

import java.util.List;

public class ConsumableTemperatureTooltip implements ItemTooltipCallback {
    @SuppressWarnings("DataFlowIssue")
    @Override
    public void getTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag type, List<Component> lines) {
        if (!stack.has(ThermiaComponents.CONSUMABLE_TEMPERATURE)) {
            return;
        }

        lines.add(1,
                Component.translatable(
                        "temperature.modifiers.consumable"
                ).withStyle(ChatFormatting.GRAY)
        );
        lines.add(1,
                CommonComponents.EMPTY
        );

        double amount = stack.get(ThermiaComponents.CONSUMABLE_TEMPERATURE).temperature();
        double minAmount = stack.get(ThermiaComponents.CONSUMABLE_TEMPERATURE).minTemperature();
        double maxAmount = stack.get(ThermiaComponents.CONSUMABLE_TEMPERATURE).maxTemperature();
        String temperatureScale = "temperature.scale.fahrenheit";

        switch (Thermia.CONFIG.temperatureScaleDisplay()) {
            case CELSIUS -> {
                amount *= 5.0 / 9.0;
                minAmount *= 5.0 / 9.0;
                maxAmount *= 5.0 / 9.0;
                temperatureScale = "temperature.scale.celsius";
            }
            case KELVIN -> {
                amount *= 5.0 / 9.0;
                minAmount *= 5.0 / 9.0;
                maxAmount *= 5.0 / 9.0;
                temperatureScale = "temperature.scale.kelvin";
            }
        }

        if (minAmount < maxAmount) {
            if (amount != 0) {
                lines.add(3,
                        Component.translatable(
                                "temperature.modifier.random",
                                minAmount,
                                maxAmount
                        ).append(
                                Component.translatable(temperatureScale)
                        ).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                );
            } else {
                if (minAmount > 0.0) {
                    lines.add(3,
                            Component.translatable(
                                    "temperature.modifier.random.hot",
                                    TemperatureModifiersComponent.DECIMAL_FORMAT.format(minAmount),
                                    TemperatureModifiersComponent.DECIMAL_FORMAT.format(maxAmount)
                            ).append(
                                    Component.translatable(temperatureScale)
                            ).append(
                                    Component.translatable("temperature.symbol.fire", " ")
                            ).withStyle(ChatFormatting.GOLD)
                    );
                } else if (minAmount < 0.0 && maxAmount < 0.0) {
                    lines.add(3,
                            Component.translatable(
                                    "temperature.modifier.random.cold",
                                    TemperatureModifiersComponent.DECIMAL_FORMAT.format(minAmount),
                                    TemperatureModifiersComponent.DECIMAL_FORMAT.format(maxAmount)
                            ).append(
                                    Component.translatable(temperatureScale)
                            ).append(
                                    Component.translatable("temperature.symbol.snowflake", " ")
                            ).withStyle(ChatFormatting.AQUA)
                    );
                } else {
                    lines.add(3,
                            Component.translatable(
                                    "temperature.modifier.random.neutral",
                                    minAmount,
                                    maxAmount
                            ).append(
                                    Component.translatable(temperatureScale)
                            ).withStyle(ChatFormatting.BLUE)
                    );
                }

                return;
            }
        }

        if (amount > 0.0) {
            lines.add(3,
                    Component.translatable(
                            "temperature.modifier.hot.0",
                            TemperatureModifiersComponent.DECIMAL_FORMAT.format(amount)
                    ).append(
                            Component.translatable(temperatureScale)
                    ).append(
                            Component.translatable("temperature.symbol.fire", " ")
                    ).withStyle(ChatFormatting.GOLD)
            );
        } else if (amount < 0.0) {
            lines.add(3,
                    Component.translatable(
                            "temperature.modifier.cold.0",
                            TemperatureModifiersComponent.DECIMAL_FORMAT.format(-amount)
                    ).append(
                            Component.translatable(temperatureScale)
                    ).append(
                            Component.translatable("temperature.symbol.snowflake", " ")
                    ).withStyle(ChatFormatting.AQUA)
            );
        }
    }
}
