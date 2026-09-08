package sylenthuntress.thermia.temperature;

import io.wispforest.owo.config.ConfigSynchronizer;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.access.LivingEntityAccess;
import sylenthuntress.thermia.compat.SereneSeasonsCompatBase;
import sylenthuntress.thermia.data.ThermiaTags;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.data_component.SunBlockingComponent;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.ServiceLoader;

@SuppressWarnings("deprecation")
public abstract class TemperatureHelper {
    public static final DecimalFormat DECIMAL_FORMAT = Util.make(
            new DecimalFormat("#.###"), format -> format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT))
    );

    public static double getRegionalTemperature(Level world, BlockPos blockPos) {
        double regionalTemperature = 100.0F;

        // Guard-return if config toggle is off
        if (!Thermia.CONFIG.temperatureChecks.DO_REGIONAL()) {
            return regionalTemperature;
        }

        final DimensionType dimension = world.dimensionType();
        final Holder<Biome> biome = world.getBiome(blockPos);
        float biomeTemperature = biome.value().getBaseTemperature();

        if (dimension.ultraWarm())
            biomeTemperature *= 2;

        // Guard return for skylight calculations in nether-like dimensions
        if (dimension.hasCeiling() || dimension.hasFixedTime())
            return (-1 + biomeTemperature) * 5;

        // Calculate skylight modifier
        float maxTimeBonus = biome.is(ConventionalBiomeTags.IS_DRY) ? 2.5F : 1F;
        float timeBonus = (float) (
                (maxTimeBonus / 2) * Math.cos(
                        world.getSunAngle(1.0F)
                ) + 1
        );

        if (!world.canSeeSkyFromBelowWater(blockPos.offset(0, 1, 0))) {
            timeBonus -= maxTimeBonus * 0.5F;
        }

        for (Entity entity
                : world.getEntitiesOfClass(Entity.class, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(blockPos.offset(0, 6, 0))))) {
            timeBonus -= 0.1F;

            if (entity instanceof LivingEntity livingEntity) {
                for (EquipmentSlot slot : EquipmentSlot.VALUES) {
                    final var component = livingEntity.getItemBySlot(slot).getOrDefault(
                            ThermiaComponents.SUN_BLOCKING,
                            SunBlockingComponent.DEFAULT
                    );

                    if (!component.slot().test(slot)) {
                        continue;
                    }

                    timeBonus -= component.amount();
                }
            }
        }

        // Apply skylight modifier
        if (biomeTemperature >= 0) {
            biomeTemperature *= timeBonus;
        }
        else {
            biomeTemperature = -(-biomeTemperature * timeBonus);
        }

        regionalTemperature += (-1 + biomeTemperature) * 5;
        regionalTemperature -= (blockPos.getY() - world.getSeaLevel()) * 0.13F;
        return regionalTemperature;
    }

    public static double getBlockTemperature(Level world, BlockPos blockPos) {
        double blockTemperature = 0;

        // Guard-return if config toggle is off
        if (!Thermia.CONFIG.temperatureChecks.DO_BLOCK()) {
            return blockTemperature;
        }

        final BlockState blockState = world.getBlockState(blockPos);
        if (blockState.getValueOrElse(BlockStateProperties.WATERLOGGED, false) || blockState.liquid()) {
            blockTemperature = getFluidTemperature(world, blockPos);
        }

        blockTemperature += world.getBrightness(LightLayer.BLOCK, blockPos) / 4F;

        // Early guard-return
        if (!blockState.isAir())
            return blockTemperature;

        for (BlockPos pos : BlockPos.betweenClosed(blockPos.offset(-4, -4, -4), blockPos.offset(4, 4, 4))) {
            blockTemperature += world.getBestNeighborSignal(pos) / 8F;

            final BlockState nearbyBlock = world.getBlockState(pos);
            if (nearbyBlock.is(ThermiaTags.Block.COLD_BLOCKS)) {
                blockTemperature -= 0.2;
            }
            if (nearbyBlock.is(ThermiaTags.Block.HOT_BLOCKS)
                    || nearbyBlock.getValueOrElse(BlockStateProperties.LIT, false)) {
                blockTemperature += 0.2;
            }

            final FluidState fluidState = nearbyBlock.getFluidState();
            if (fluidState.is(FluidTags.LAVA) && fluidState.isSource()) {
                blockTemperature += 1;
            }
        }

        return blockTemperature;
    }

    public static double getFluidTemperature(Level world, BlockPos blockPos) {
        double fluidTemperature = 0;

        // Guard-return if config toggle is off
        if (!Thermia.CONFIG.temperatureChecks.DO_FLUID()) {
            return fluidTemperature;
        }

        for (BlockPos pos : BlockPos.betweenClosed(blockPos.offset(-1, -2, -1), blockPos.offset(1, 2, 1))) {
            final FluidState fluidState = world.getFluidState(pos);
            if (fluidState.is(FluidTags.LAVA)) {
                fluidTemperature += 1f;
            }

            if (world.getBlockState(pos).getValueOrElse(BlockStateProperties.WATERLOGGED, false)
                    || fluidState.is(FluidTags.WATER)) {
                if (getRegionalTemperature(world, pos) < 0) {
                    fluidTemperature -= 0.1f;
                }
                fluidTemperature -= 0.1f;
            }
        }

        return fluidTemperature;
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public static double getSeasonalTemperature(Level world) {
        double seasonTemperature = 0.0;

        // Guard-return if config toggle is off
        if (!FabricLoader.getInstance().isModLoaded("sereneseasons") || Thermia.CONFIG.temperatureChecks.DO_SEASONAL()) {
            return seasonTemperature;
        }

        ServiceLoader<SereneSeasonsCompatBase> loader = ServiceLoader.load(SereneSeasonsCompatBase.class);
        if (loader.findFirst().isEmpty()) {
            return 0;
        }
        var seasonState = loader.findFirst().get().getSeasonState(world);
        var season = seasonState.getSubSeason();

        switch (season) {
            case EARLY_AUTUMN -> seasonTemperature = 0.5;
            case MID_AUTUMN -> seasonTemperature = 0;
            case LATE_AUTUMN -> seasonTemperature = -0.5;
            case EARLY_WINTER -> seasonTemperature = -1.25;
            case MID_WINTER -> seasonTemperature = -2;
            case LATE_WINTER -> seasonTemperature = -1.25;
            case EARLY_SPRING -> seasonTemperature = -0.5;
            case MID_SPRING -> seasonTemperature = 0;
            case LATE_SPRING -> seasonTemperature = 0.5;
            case EARLY_SUMMER -> seasonTemperature = 1.25;
            case MID_SUMMER -> seasonTemperature = 2;
            case LATE_SUMMER -> seasonTemperature = 1.25;
        }

        return seasonTemperature;
    }

    public static double getAmbientTemperature(Level world, BlockPos blockPos) {
        double regionalTemperature = getRegionalTemperature(world, blockPos);
        double blockTemperature = getBlockTemperature(world, blockPos);
        double seasonalTemperature = getSeasonalTemperature(world);

        return regionalTemperature + blockTemperature + seasonalTemperature;
    }

    public static TemperatureManager getTemperatureManager(LivingEntity entity) {
        return ((LivingEntityAccess) entity).thermia$getTemperatureManager();
    }

    public static TemperatureManager getTemperatureManager(Entity entity) {
        return getTemperatureManager((LivingEntity) entity);
    }

    public static boolean lacksTemperature(Entity entity) {
        return !(entity.showVehicleHealth()
                && getTemperatureManager(entity).canHaveTemperature());
    }

    @SuppressWarnings("unused")
    public enum TemperatureScaleDisplay {
        FAHRENHEIT, CELSIUS, KELVIN;

        public static double celsiusToFahrenheit(double temperature) {
            return (temperature * 9 / 5) + 32;
        }

        public static double celsiusToKelvin(double temperature) {
            return temperature + 273.15;
        }

        public static double fahrenheitToCelsius(double temperature) {
            return (temperature - 32) * 5 / 9;
        }

        public static double fahrenheitToKelvin(double temperature) {
            return (temperature - 32) * 5 / 9 + 273.15;
        }

        public static double kelvinToFahrenheit(double temperature) {
            return (temperature - 273.15) * 9 / 5 + 32;
        }

        public static double kelvinToCelsius(double temperature) {
            return temperature - 273.15;
        }

        public static Component convertForClient(ServerPlayer player, double temperature) {
            @SuppressWarnings("DataFlowIssue") var temperatureScaleDisplay = (TemperatureScaleDisplay)
                    ConfigSynchronizer.getClientOptions(
                            player,
                            "thermia-config"
                    ).get(Thermia.CONFIG.keys.temperatureScaleDisplay);

            String temperatureScale = "temperature.scale.fahrenheit";
            switch (temperatureScaleDisplay) {
                case CELSIUS -> {
                    temperature = fahrenheitToCelsius(temperature);
                    temperatureScale = "temperature.scale.celsius";
                }
                case KELVIN -> {
                    temperature = fahrenheitToKelvin(temperature);
                    temperatureScale = "temperature.scale.kelvin";
                }
            }

            return Component.literal(
                    DECIMAL_FORMAT.format(temperature)
            ).append(Component.translatable(temperatureScale));
        }
    }
}
