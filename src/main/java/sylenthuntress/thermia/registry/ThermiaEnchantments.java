package sylenthuntress.thermia.registry;

import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.TagPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import sylenthuntress.thermia.Thermia;

import java.util.List;
import java.util.Optional;

public class ThermiaEnchantments {
    public static final ResourceKey<Enchantment> FROST_PROTECTION = create("frost_protection");

    private static ResourceKey<Enchantment> create(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Thermia.modIdentifier(id));
    }

    public static void bootstrap(BootstrapContext<Enchantment> registry) {
        registry.register(FROST_PROTECTION, Enchantment
            .enchantment(new Enchantment.EnchantmentDefinition(
                registry.lookup(Registries.ITEM).getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                Optional.empty(),
                5, 4,
                new Enchantment.Cost(10, 8),
                new Enchantment.Cost(0, 0),
                2,
                List.of(EquipmentSlotGroup.ARMOR)
            ))
            .exclusiveWith(registry.lookup(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE))
            .withEffect(EnchantmentEffectComponents.DAMAGE_PROTECTION, new AddValue(
                new LevelBasedValue.Linear(2, 2)
            ), DamageSourceCondition.hasDamageSource(
                DamageSourcePredicate.Builder.damageType()
                    .tag(TagPredicate.is(DamageTypeTags.IS_FREEZING))
                    .tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY))
            ))
            .build(ThermiaEnchantments.FROST_PROTECTION.identifier()));
    }
}
