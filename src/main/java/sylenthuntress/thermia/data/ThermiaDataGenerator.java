package sylenthuntress.thermia.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.world.item.Items;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.data.advancement.criterion.FreezeCriterion;
import sylenthuntress.thermia.data.advancement.criterion.OverheatCriterion;
import sylenthuntress.thermia.registry.ThermiaItems;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ThermiaDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(AdvancementsProvider::new);
    }

    static class AdvancementsProvider extends FabricAdvancementProvider {
        protected AdvancementsProvider(FabricDataOutput dataGen, CompletableFuture<HolderLookup.Provider> registryLookup) {
            super(dataGen, registryLookup);
        }

        @Override
        public void generateAdvancement(HolderLookup.Provider wrapperLookup, Consumer<AdvancementHolder> consumer) {
            AdvancementHolder rootAdvancement = Advancement.Builder.advancement()
                    .display(
                            ThermiaItems.THERMIA_ICON,
                            Component.translatable("advancements.thermia.root.title"),
                            Component.translatable("advancements.thermia.root.description"),
                            ResourceLocation.withDefaultNamespace("textures/block/powder_snow.png"),
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("got_hypothermia", FreezeCriterion.Conditions.create())
                    .addCriterion("got_hyperthermia", OverheatCriterion.Conditions.create())
                    .save(consumer, Thermia.MOD_ID + "/root");
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
                    .save(consumer, Thermia.MOD_ID + "/got_hypothermia");
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
                    .save(consumer, Thermia.MOD_ID + "/got_hyperthermia");
        }
    }
}
