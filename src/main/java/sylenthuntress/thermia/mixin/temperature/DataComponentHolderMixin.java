package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.data.ThermiaTags;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.data_component.SunBlockingComponent;

@Mixin(DataComponentHolder.class)
public interface DataComponentHolderMixin {
    @ModifyReturnValue(
            method = "getOrDefault",
            at = @At("RETURN")
    )
    private Object thermia$applyDefaultModifiers$1(Object obj) {
        return thermia$calculateDefaultModifiers(obj);
    }

    @ModifyReturnValue(
            method = "get",
            at = @At("RETURN")
    )
    private Object thermia$applyDefaultModifiers$2(Object obj) {
        return thermia$calculateDefaultModifiers(obj);
    }

    @Unique
    default Object thermia$calculateDefaultModifiers(Object obj) {
        //noinspection ConstantValue
        if (!((DataComponentHolder) this instanceof ItemStack stack)
                || !(obj instanceof ItemAttributeModifiers component)) {
            return obj;
        }

        // Apply default equippable attributes
        if (stack.has(DataComponents.EQUIPPABLE)) {
            if (stack.is(ThermiaTags.Item.Equippable.INSULATING)) {
                final EquipmentSlot itemSlot = stack.get(
                        DataComponents.EQUIPPABLE
                ).slot();

                component = component.withModifierAdded(
                        ThermiaAttributes.COLD_OFFSET_THRESHOLD,
                        new AttributeModifier(
                                Thermia.modIdentifier("cold_offset_modifier."
                                        + "."
                                        + itemSlot.getSerializedName()
                                ),
                                2,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.bySlot(
                                itemSlot
                        )
                );

                component = component.withModifierAdded(
                        ThermiaAttributes.HEAT_OFFSET_THRESHOLD,
                        new AttributeModifier(
                                Thermia.modIdentifier("heat_offset_modifier."
                                        + "."
                                        + itemSlot.getSerializedName()
                                ),
                                -0.2,
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        ),
                        EquipmentSlotGroup.bySlot(
                                itemSlot
                        )
                );
            }
            if (stack.is(ThermiaTags.Item.Equippable.BREEZY)) {
                final EquipmentSlot itemSlot = stack.get(
                        DataComponents.EQUIPPABLE
                ).slot();

                component = component.withModifierAdded(
                        ThermiaAttributes.HEAT_OFFSET_THRESHOLD,
                        new AttributeModifier(
                                Thermia.modIdentifier("heat_offset_modifier."
                                        + "."
                                        + itemSlot.getSerializedName()
                                ),
                                2,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.bySlot(
                                itemSlot
                        )
                );

                component = component.withModifierAdded(
                        ThermiaAttributes.COLD_OFFSET_THRESHOLD,
                        new AttributeModifier(
                                Thermia.modIdentifier("cold_offset_modifier."
                                        + "."
                                        + itemSlot.getSerializedName()
                                ),
                                -0.2,
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        ),
                        EquipmentSlotGroup.bySlot(
                                itemSlot
                        )
                );
            }
        }

        // Apply default sun blocking attributes
        if (!stack.has(ThermiaComponents.SUN_BLOCKING)
                && stack.is(ThermiaTags.Item.Equippable.BLOCKS_SUNLIGHT)) {
            if (stack.is(ThermiaTags.Item.Equippable.BlocksSunlight.ANY)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                EquipmentSlotGroup.ANY
                        )
                );
            }
            if (stack.is(ThermiaTags.Item.Equippable.BlocksSunlight.BODY)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                EquipmentSlotGroup.BODY
                        )
                );
            }
            if (stack.is(ThermiaTags.Item.Equippable.BlocksSunlight.FEET)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                EquipmentSlotGroup.FEET
                        )
                );
            }
            if (stack.is(ThermiaTags.Item.Equippable.BlocksSunlight.HANDS)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                EquipmentSlotGroup.HAND
                        )
                );
            }
            if (stack.is(ThermiaTags.Item.Equippable.BlocksSunlight.HEAD)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                EquipmentSlotGroup.HEAD
                        )
                );
            }
            if (stack.is(ThermiaTags.Item.Equippable.BlocksSunlight.LEGS)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                EquipmentSlotGroup.LEGS
                        )
                );
            }
            if (stack.is(ThermiaTags.Item.Equippable.BlocksSunlight.MAINHAND)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                EquipmentSlotGroup.MAINHAND
                        )
                );
            }
            if (stack.is(ThermiaTags.Item.Equippable.BlocksSunlight.OFFHAND)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                EquipmentSlotGroup.OFFHAND
                        )
                );
            }
        }

        // Guard-return for unenchanted items
        if (!stack.isEnchanted()) {
            return component;
        }

        // Apply default enchantment attribute modifiers
        final var enchantments = stack.getEnchantments();
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            for (EquipmentSlotGroup slot : enchantment.value().definition().slots()) {
                if (enchantment.is(ThermiaTags.Enchantment.HYPERTHERMIA_PROTECTION)) {
                    component = component.withModifierAdded(
                            ThermiaAttributes.HEAT_OFFSET_THRESHOLD,
                            new AttributeModifier(
                                    Thermia.modIdentifier(
                                            "enchantment."
                                                    + enchantment.getRegisteredName()
                                                    .replaceFirst("[A-Za-z0-9]+:", "")
                                                    + ".heat_offset_threshold"
                                    ),
                                    2 + 0.25 * enchantments.getLevel(enchantment),
                                    AttributeModifier.Operation.ADD_VALUE
                            ),
                            slot
                    );
                }

                if (enchantment.is(ThermiaTags.Enchantment.HYPOTHERMIA_PROTECTION)) {
                    component = component.withModifierAdded(
                            ThermiaAttributes.COLD_OFFSET_THRESHOLD,
                            new AttributeModifier(
                                    Thermia.modIdentifier(
                                            "enchantment."
                                                    + enchantment.getRegisteredName()
                                                    .replaceFirst("[A-Za-z0-9]+:", "")
                                                    + ".cold_offset_threshold"
                                    ),
                                    2 + 0.25 * enchantments.getLevel(enchantment),
                                    AttributeModifier.Operation.ADD_VALUE
                            ),
                            slot
                    );
                }

            }
        }

        return component;
    }
}
