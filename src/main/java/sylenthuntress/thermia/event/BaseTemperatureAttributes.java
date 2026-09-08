package sylenthuntress.thermia.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import sylenthuntress.thermia.data.ThermiaTags;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.temperature.TemperatureHelper;

public class BaseTemperatureAttributes implements ServerEntityEvents.Load {
    @Override
    public void onLoad(Entity entity, ServerLevel world) {
        if (!(entity instanceof LivingEntity livingEntity) || entity.isAlwaysTicking()) {
            return;
        }

        calculateBaseTemperature(livingEntity, world);
        calculateOffsets(livingEntity, world);
    }

    protected void calculateBaseTemperature(LivingEntity entity, ServerLevel world) {
        final var temperatureManager = TemperatureHelper.getTemperatureManager(entity);
        double baseTemperature = temperatureManager.getBaseTemperature();

        // Guard-return if following calculations have already been done
        if (baseTemperature != ThermiaAttributes.BASE_TEMPERATURE.value().getDefaultValue()) {
            return;
        }

        double ambientTemperature = TemperatureHelper.getAmbientTemperature(world, entity.blockPosition());
        baseTemperature = (baseTemperature + ambientTemperature) / 2;

        var attributeInstance = entity.getAttributes().getInstance(ThermiaAttributes.BASE_TEMPERATURE);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(baseTemperature);
        }
    }

    protected void calculateOffsets(LivingEntity entity, ServerLevel world) {
        double coldOffset = entity.getAttributeValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD);
        double heatOffset = entity.getAttributeValue(ThermiaAttributes.HEAT_OFFSET_THRESHOLD);

        // Guard-return if following calculations have already been done
        if (coldOffset != ThermiaAttributes.COLD_OFFSET_THRESHOLD.value().getDefaultValue()) {
            return;
        } else if (heatOffset != ThermiaAttributes.HEAT_OFFSET_THRESHOLD.value().getDefaultValue()) {
            return;
        }

        var biome = world.getBiome(entity.blockPosition());

        if (biome.is(ConventionalBiomeTags.IS_DRY)) {
            heatOffset += 3;
            coldOffset += 3;
        }
        if (biome.value().getPrecipitationAt(entity.blockPosition(), world.getSeaLevel()) == Biome.Precipitation.SNOW) {
            coldOffset += 3;
        }

        if (entity.is(ThermiaTags.EntityType.HAS_FUR)) {
            coldOffset += 2;
            heatOffset -= 1.5;
        }
        if (entity.is(ThermiaTags.EntityType.HAS_WOOL)) {
            coldOffset += 4.5;
            heatOffset -= 2.5;
        }
        if (entity.is(ThermiaTags.EntityType.UNDEAD)) {
            coldOffset += 10;
            heatOffset += 3;
        }

        var attributeInstance = entity.getAttributes().getInstance(ThermiaAttributes.COLD_OFFSET_THRESHOLD);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(coldOffset);
        }

        attributeInstance = entity.getAttributes().getInstance(ThermiaAttributes.HEAT_OFFSET_THRESHOLD);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(heatOffset);
        }
    }
}
