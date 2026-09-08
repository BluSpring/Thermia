package sylenthuntress.thermia.data;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.data.advancement.criterion.FreezeCriterion;
import sylenthuntress.thermia.data.advancement.criterion.OverheatCriterion;
import sylenthuntress.thermia.registry.ThermiaEnchantments;
import sylenthuntress.thermia.registry.ThermiaItems;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ThermiaDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(AdvancementsProvider::new);
        pack.addProvider(ModelProvider::new);
        pack.addProvider(EnchantmentProvider::new);
        pack.addProvider(BlockTagsProvider::new);
        pack.addProvider(EnchantingTagsProvider::new);
        pack.addProvider(ItemTagsProvider::new);
        pack.addProvider(EntityTypeTagsProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        DataGeneratorEntrypoint.super.buildRegistry(registryBuilder);
        registryBuilder.add(Registries.ENCHANTMENT, ThermiaEnchantments::bootstrap);
    }

    static class ModelProvider extends FabricModelProvider {
        public ModelProvider(FabricPackOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(BlockModelGenerators generator) {
        }

        @Override
        public void generateItemModels(ItemModelGenerators generator) {
            generator.generateFlatItem(ThermiaItems.THERMIA_ICON, ModelTemplates.FLAT_ITEM);
        }
    }

    static class EnchantmentProvider extends FabricCodecDataProvider<Enchantment> {
        protected EnchantmentProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(packOutput, registriesFuture, PackOutput.Target.DATA_PACK, "enchantment", Enchantment.DIRECT_CODEC);
        }

        @Override
        protected void configure(BiConsumer<Identifier, Enchantment> provider, HolderLookup.Provider registryLookup) {
            provider.accept(ThermiaEnchantments.FROST_PROTECTION.identifier(), registryLookup.getOrThrow(ThermiaEnchantments.FROST_PROTECTION).value());
        }

        @Override
        public String getName() {
            return "Thermia Enchantment Provider";
        }
    }

    static class BlockTagsProvider extends FabricTagsProvider.BlockTagsProvider {
        public BlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
            super(output, registryLookupFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            builder(ThermiaTags.Block.COLD_BLOCKS)
                .forceAddTag(BlockTags.ICE)
                .forceAddTag(BlockTags.SNOW);

            builder(ThermiaTags.Block.HOT_BLOCKS)
                .forceAddTag(BlockTags.CAMPFIRES)
                .forceAddTag(BlockTags.FIRE)
                .add(Blocks.MAGMA_BLOCK.properties().blockIdOrThrow());
        }
    }

    static class EnchantingTagsProvider extends FabricTagsProvider<Enchantment> {
        public EnchantingTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
            super(output, Registries.ENCHANTMENT, registryLookupFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            builder(EnchantmentTags.ARMOR_EXCLUSIVE)
                .add(ThermiaEnchantments.FROST_PROTECTION);

            builder(EnchantmentTags.TRADES_SNOW_COMMON)
                .add(ThermiaEnchantments.FROST_PROTECTION);

            builder(EnchantmentTags.IN_ENCHANTING_TABLE)
                .add(ThermiaEnchantments.FROST_PROTECTION);

            builder(EnchantmentTags.NON_TREASURE)
                .add(ThermiaEnchantments.FROST_PROTECTION);

            builder(ThermiaTags.Enchantment.HYPERTHERMIA_PROTECTION)
                .add(Enchantments.FIRE_PROTECTION);

            builder(ThermiaTags.Enchantment.HYPOTHERMIA_PROTECTION)
                .add(ThermiaEnchantments.FROST_PROTECTION);

            builder(ThermiaTags.Enchantment.PROVIDES_CHILL)
                .add(Enchantments.FROST_WALKER);

            builder(ThermiaTags.Enchantment.PROVIDES_WARMTH)
                .add(Enchantments.FIRE_ASPECT)
                .add(Enchantments.FLAME);
        }
    }

    static class EntityTypeTagsProvider extends FabricTagsProvider.EntityTypeTagsProvider {
        public EntityTypeTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
            super(output, registryLookupFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            builder(ThermiaTags.EntityType.HAS_FUR)
                .add(EntityType.BAT.builtInRegistryHolder().key())
                .add(EntityType.CAT.builtInRegistryHolder().key())
                .add(EntityType.COW.builtInRegistryHolder().key())
                .add(EntityType.DONKEY.builtInRegistryHolder().key())
                .add(EntityType.FOX.builtInRegistryHolder().key())
                .add(EntityType.GOAT.builtInRegistryHolder().key())
                .add(EntityType.HORSE.builtInRegistryHolder().key())
                .add(EntityType.MOOSHROOM.builtInRegistryHolder().key())
                .add(EntityType.MULE.builtInRegistryHolder().key())
                .add(EntityType.OCELOT.builtInRegistryHolder().key())
                .add(EntityType.PANDA.builtInRegistryHolder().key())
                .add(EntityType.POLAR_BEAR.builtInRegistryHolder().key())
                .add(EntityType.RABBIT.builtInRegistryHolder().key())
                .add(EntityType.SNIFFER.builtInRegistryHolder().key())
                .add(EntityType.WOLF.builtInRegistryHolder().key());

            builder(ThermiaTags.EntityType.HAS_WOOL)
                .add(EntityType.GOAT.builtInRegistryHolder().key())
                .add(EntityType.LLAMA.builtInRegistryHolder().key())
                .add(EntityType.SHEEP.builtInRegistryHolder().key())
                .add(EntityType.TRADER_LLAMA.builtInRegistryHolder().key());

            builder(ThermiaTags.EntityType.UNDEAD)
                .forceAddTag(EntityTypeTags.UNDEAD);

            builder(ThermiaTags.EntityType.TEMPERATURE_IMMUNE)
                .forceAddTag(ConventionalEntityTypeTags.BOSSES)
                .add(EntityType.ALLAY.builtInRegistryHolder().key())
                .add(EntityType.CREAKING.builtInRegistryHolder().key())
                .add(EntityType.ENDERMITE.builtInRegistryHolder().key())
                .add(EntityType.IRON_GOLEM.builtInRegistryHolder().key())
                .add(EntityType.SLIME.builtInRegistryHolder().key())
                .add(EntityType.VEX.builtInRegistryHolder().key());

            builder(ThermiaTags.EntityType.CLIMATE_UNAFFECTED);
        }
    }

    static class ItemTagsProvider extends FabricTagsProvider.ItemTagsProvider {
        public ItemTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
            super(output, registryLookupFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            builder(ThermiaTags.Item.Consumable.HOT_FOODS)
                .forceAddTag(ConventionalItemTags.SOUP_FOODS)
                .forceAddTag(ConventionalItemTags.PIE_FOODS);

            builder(ThermiaTags.Item.Consumable.REFRESHING_FOODS)
                .forceAddTag(ConventionalItemTags.FRUIT_FOODS)
                .forceAddTag(ConventionalItemTags.DRINKS)
                .forceAddTag(ConventionalItemTags.VEGETABLE_FOODS);

            builder(ThermiaTags.Item.Consumable.WARM_FOODS)
                .forceAddTag(ConventionalItemTags.COOKED_MEAT_FOODS)
                .forceAddTag(ConventionalItemTags.COOKED_FISH_FOODS)
                .add(Items.BAKED_POTATO.builtInRegistryHolder().key());

            builder(ThermiaTags.Item.Equippable.COLD_WHEN_HELD)
                .add(Items.BLUE_ICE.builtInRegistryHolder().key())
                .add(Items.BREEZE_ROD.builtInRegistryHolder().key())
                .add(Items.ICE.builtInRegistryHolder().key())
                .add(Items.PACKED_ICE.builtInRegistryHolder().key())
                .add(Items.POWDER_SNOW_BUCKET.builtInRegistryHolder().key())
                .add(Items.SNOW.builtInRegistryHolder().key())
                .add(Items.SNOW_BLOCK.builtInRegistryHolder().key())
                .add(Items.SNOWBALL.builtInRegistryHolder().key());

            builder(ThermiaTags.Item.Equippable.HOT_WHEN_HELD)
                .add(Items.BLAZE_POWDER.builtInRegistryHolder().key())
                .add(Items.BLAZE_ROD.builtInRegistryHolder().key())
                .add(Items.FIRE_CHARGE.builtInRegistryHolder().key())
                .add(Items.LAVA_BUCKET.builtInRegistryHolder().key())
                .add(Items.MAGMA_BLOCK.builtInRegistryHolder().key())
                .add(Items.MAGMA_CREAM.builtInRegistryHolder().key());

            builder(ThermiaTags.Item.Equippable.INSULATING)
                .add(Items.LEATHER_HELMET.builtInRegistryHolder().key())
                .add(Items.LEATHER_CHESTPLATE.builtInRegistryHolder().key())
                .add(Items.LEATHER_LEGGINGS.builtInRegistryHolder().key())
                .add(Items.LEATHER_BOOTS.builtInRegistryHolder().key())
                .add(Items.TURTLE_HELMET.builtInRegistryHolder().key());

            builder(ThermiaTags.Item.Consumable.APPLIES_FROST_RESISTANCE)
                .add(Items.ENCHANTED_GOLDEN_APPLE.builtInRegistryHolder().key());
        }
    }

    static class AdvancementsProvider extends FabricAdvancementProvider {
        protected AdvancementsProvider(FabricPackOutput dataGen, CompletableFuture<HolderLookup.Provider> registryLookup) {
            super(dataGen, registryLookup);
        }

        @Override
        public void generateAdvancement(HolderLookup.Provider wrapperLookup, Consumer<AdvancementHolder> consumer) {
            AdvancementHolder rootAdvancement = Advancement.Builder.advancement()
                    .display(
                            ThermiaItems.THERMIA_ICON,
                            Component.translatable("advancements.thermia.root.title"),
                            Component.translatable("advancements.thermia.root.description"),
                            Identifier.withDefaultNamespace("textures/block/powder_snow.png"),
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("got_hypothermia", FreezeCriterion.Conditions.create())
                    .addCriterion("got_hyperthermia", OverheatCriterion.Conditions.create())
                    .save(consumer, Thermia.modIdentifier("root"));
            generateColdAdvancements(consumer, rootAdvancement);
            generateHotAdvancements(consumer, rootAdvancement);
        }

        protected void generateColdAdvancements(Consumer<AdvancementHolder> consumer, AdvancementHolder rootAdvancement) {
            Advancement.Builder.advancement()
                    .parent(rootAdvancement)
                    .display(
                            Items.POWDER_SNOW_BUCKET,
                            Component.translatable("advancements.thermia.got_hypothermia.title"),
                            Component.translatable("advancements.thermia.got_hypothermia.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("got_hypothermia", FreezeCriterion.Conditions.create())
                    .save(consumer, Thermia.modIdentifier("got_hypothermia"));
        }

        protected void generateHotAdvancements(Consumer<AdvancementHolder> consumer, AdvancementHolder rootAdvancement) {
            Advancement.Builder.advancement()
                    .parent(rootAdvancement)
                    .display(
                            Items.MAGMA_BLOCK,
                            Component.translatable("advancements.thermia.got_hyperthermia.title"),
                            Component.translatable("advancements.thermia.got_hyperthermia.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("got_hyperthermia", OverheatCriterion.Conditions.create())
                    .save(consumer, Thermia.modIdentifier("got_hyperthermia"));
        }
    }
}
