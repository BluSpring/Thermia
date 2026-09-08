package sylenthuntress.thermia.data.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import sylenthuntress.thermia.temperature.TemperatureHelper;

public record LocationTemperaturePredicate(
    MinMaxBounds.Doubles ambientTemperature,
    MinMaxBounds.Doubles blockTemperature,
    MinMaxBounds.Doubles fluidTemperature,
    MinMaxBounds.Doubles regionalTemperature,
    MinMaxBounds.Doubles seasonalTemperature
) {
    public static final Codec<LocationTemperaturePredicate> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            MinMaxBounds.Doubles.CODEC.optionalFieldOf("ambient_temperature", MinMaxBounds.Doubles.ANY)
                                    .forGetter(LocationTemperaturePredicate::ambientTemperature),
                            MinMaxBounds.Doubles.CODEC.optionalFieldOf("block_temperature", MinMaxBounds.Doubles.ANY)
                                    .forGetter(LocationTemperaturePredicate::blockTemperature),
                            MinMaxBounds.Doubles.CODEC.optionalFieldOf("fluid_temperature", MinMaxBounds.Doubles.ANY)
                                    .forGetter(LocationTemperaturePredicate::fluidTemperature),
                            MinMaxBounds.Doubles.CODEC.optionalFieldOf("regional_temperature", MinMaxBounds.Doubles.ANY)
                                    .forGetter(LocationTemperaturePredicate::regionalTemperature),
                            MinMaxBounds.Doubles.CODEC.optionalFieldOf("seasonal_temperature", MinMaxBounds.Doubles.ANY)
                                    .forGetter(LocationTemperaturePredicate::seasonalTemperature)
                    )
                    .apply(instance, LocationTemperaturePredicate::new)
    );

    public MapCodec<? extends EntitySubPredicate> getCodec() {
        return null;
    }

    public boolean test(Level world, BlockPos pos) {
        if (!ambientTemperature().matches(TemperatureHelper.getAmbientTemperature(world, pos))) {
            return false;
        } else if (!blockTemperature().matches(TemperatureHelper.getBlockTemperature(world, pos))) {
            return false;
        } else if (!fluidTemperature().matches(TemperatureHelper.getFluidTemperature(world, pos))) {
            return false;
        } else if (!regionalTemperature().matches(TemperatureHelper.getRegionalTemperature(world, pos))) {
            return false;
        } else return seasonalTemperature().matches(TemperatureHelper.getSeasonalTemperature(world));
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private MinMaxBounds.Doubles ambientTemperature;
        private MinMaxBounds.Doubles blockTemperature;
        private MinMaxBounds.Doubles fluidTemperature;
        private MinMaxBounds.Doubles regionalTemperature;
        private MinMaxBounds.Doubles seasonalTemperature;

        public static LocationTemperaturePredicate.Builder create() {
            return new LocationTemperaturePredicate.Builder();
        }

        public LocationTemperaturePredicate.Builder setAmbientTemperature(MinMaxBounds.Doubles ambientTemperature) {
            this.ambientTemperature = ambientTemperature;
            return this;
        }

        public LocationTemperaturePredicate.Builder setBlockTemperature(MinMaxBounds.Doubles blockTemperature) {
            this.blockTemperature = blockTemperature;
            return this;
        }

        public LocationTemperaturePredicate.Builder setFluidTemperature(MinMaxBounds.Doubles fluidTemperature) {
            this.fluidTemperature = fluidTemperature;
            return this;
        }

        public LocationTemperaturePredicate.Builder setRegionalTemperature(MinMaxBounds.Doubles regionalTemperature) {
            this.regionalTemperature = regionalTemperature;
            return this;
        }

        public LocationTemperaturePredicate.Builder setSeasonalTemperature(MinMaxBounds.Doubles seasonalTemperature) {
            this.seasonalTemperature = seasonalTemperature;
            return this;
        }

        public LocationTemperaturePredicate build() {
            return new LocationTemperaturePredicate(this.ambientTemperature, this.blockTemperature, this.fluidTemperature, this.regionalTemperature, this.seasonalTemperature);
        }
    }
}
