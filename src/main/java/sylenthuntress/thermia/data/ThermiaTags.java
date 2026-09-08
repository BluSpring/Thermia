package sylenthuntress.thermia.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import sylenthuntress.thermia.Thermia;

@SuppressWarnings("unused")
public class ThermiaTags {
    public static class Block {
        public static final TagKey<net.minecraft.world.level.block.Block> COLD_BLOCKS = TagKey.create(
                Registries.BLOCK,
                Thermia.modIdentifier("climate/cold_blocks")
        );
        public static final TagKey<net.minecraft.world.level.block.Block> HOT_BLOCKS = TagKey.create(
                Registries.BLOCK,
                Thermia.modIdentifier("climate/hot_blocks")
        );
    }
    public static class EntityType {
        public static final TagKey<net.minecraft.world.entity.EntityType<?>> CLIMATE_UNAFFECTED = TagKey.create(
                Registries.ENTITY_TYPE,
                Thermia.modIdentifier("climate/unaffected")
        );
        public static final TagKey<net.minecraft.world.entity.EntityType<?>> HAS_FUR = TagKey.create(
                Registries.ENTITY_TYPE,
                Thermia.modIdentifier("climate/has_fur")
        );
        public static final TagKey<net.minecraft.world.entity.EntityType<?>> HAS_WOOL = TagKey.create(
                Registries.ENTITY_TYPE,
                Thermia.modIdentifier("climate/has_wool")
        );
        public static final TagKey<net.minecraft.world.entity.EntityType<?>> UNDEAD = TagKey.create(
                Registries.ENTITY_TYPE,
                Thermia.modIdentifier("climate/is_undead")
        );
        public static final TagKey<net.minecraft.world.entity.EntityType<?>> TEMPERATURE_IMMUNE = TagKey.create(
                Registries.ENTITY_TYPE,
                Thermia.modIdentifier("climate/temperature_immune")
        );
    }
    public static class Item {
        public static class Consumable {
            public static final TagKey<net.minecraft.world.item.Item> COLD_FOODS = TagKey.create(
                    Registries.ITEM,
                    Thermia.modIdentifier("consumable_temperature/cold_foods")
            );
            public static final TagKey<net.minecraft.world.item.Item> HOT_FOODS = TagKey.create(
                    Registries.ITEM,
                    Thermia.modIdentifier("consumable_temperature/hot_foods")
            );
            public static final TagKey<net.minecraft.world.item.Item> REFRESHING_FOODS = TagKey.create(
                    Registries.ITEM,
                    Thermia.modIdentifier("consumable_temperature/refreshing_foods")
            );
            public static final TagKey<net.minecraft.world.item.Item> WARM_FOODS = TagKey.create(
                    Registries.ITEM,
                    Thermia.modIdentifier("consumable_temperature/warm_foods")
            );

            public static final TagKey<net.minecraft.world.item.Item> APPLIES_FROST_RESISTANCE = TagKey.create(
                    Registries.ITEM,
                    Thermia.modIdentifier("applies_frost_resistance")
            );
        }

        public static class Equippable {
            public static final TagKey<net.minecraft.world.item.Item> INSULATING = TagKey.create(
                    Registries.ITEM,
                    Thermia.modIdentifier("equippable_temperature/insulating")
            );

            public static final TagKey<net.minecraft.world.item.Item> BREEZY = TagKey.create(
                    Registries.ITEM,
                    Thermia.modIdentifier("equippable_temperature/breezy")
            );

            public static final TagKey<net.minecraft.world.item.Item> COLD_WHEN_HELD = TagKey.create(
                    Registries.ITEM,
                    Thermia.modIdentifier("equippable_temperature/cold_when_held")
            );

            public static final TagKey<net.minecraft.world.item.Item> HOT_WHEN_HELD = TagKey.create(
                    Registries.ITEM,
                    Thermia.modIdentifier("equippable_temperature/hot_when_held")
            );

            public static final TagKey<net.minecraft.world.item.Item> BLOCKS_SUNLIGHT = TagKey.create(
                    Registries.ITEM,
                    Thermia.modIdentifier("blocks_sunlight")
            );

            public static class BlocksSunlight {
                public static final TagKey<net.minecraft.world.item.Item> ANY = TagKey.create(
                        Registries.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/any")
                );

                public static final TagKey<net.minecraft.world.item.Item> BODY = TagKey.create(
                        Registries.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/body")
                );

                public static final TagKey<net.minecraft.world.item.Item> FEET = TagKey.create(
                        Registries.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/feet")
                );

                public static final TagKey<net.minecraft.world.item.Item> HANDS = TagKey.create(
                        Registries.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/hands")
                );

                public static final TagKey<net.minecraft.world.item.Item> HEAD = TagKey.create(
                        Registries.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/HEAD")
                );

                public static final TagKey<net.minecraft.world.item.Item> LEGS = TagKey.create(
                        Registries.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/legs")
                );

                public static final TagKey<net.minecraft.world.item.Item> MAINHAND = TagKey.create(
                        Registries.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/mainhand")
                );

                public static final TagKey<net.minecraft.world.item.Item> OFFHAND = TagKey.create(
                        Registries.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/offhand")
                );
            }
        }
    }

    public static class Enchantment {
        public static final TagKey<net.minecraft.world.item.enchantment.Enchantment> PROVIDES_CHILL = TagKey.create(
                Registries.ENCHANTMENT,
                Thermia.modIdentifier("provides_chill")
        );
        public static final TagKey<net.minecraft.world.item.enchantment.Enchantment> PROVIDES_WARMTH = TagKey.create(
                Registries.ENCHANTMENT,
                Thermia.modIdentifier("provides_warmth")
        );

        public static final TagKey<net.minecraft.world.item.enchantment.Enchantment> HYPOTHERMIA_PROTECTION = TagKey.create(
                Registries.ENCHANTMENT,
                Thermia.modIdentifier("hypothermia_protection")
        );

        public static final TagKey<net.minecraft.world.item.enchantment.Enchantment> HYPERTHERMIA_PROTECTION = TagKey.create(
                Registries.ENCHANTMENT,
                Thermia.modIdentifier("hyperthermia_protection")
        );
    }
}
