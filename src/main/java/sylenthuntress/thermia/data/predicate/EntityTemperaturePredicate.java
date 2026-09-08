package sylenthuntress.thermia.data.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import sylenthuntress.thermia.temperature.TemperatureHelper;

public record EntityTemperaturePredicate(
    MinMaxBounds.Doubles baseTemperature,
    MinMaxBounds.Doubles currentTemperature,
    MinMaxBounds.Doubles targetTemperature,
    MinMaxBounds.Doubles unmodifiedTemperature
) {
    public static final Codec<EntityTemperaturePredicate> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            MinMaxBounds.Doubles.CODEC.optionalFieldOf("base_temperature", MinMaxBounds.Doubles.ANY)
                                    .forGetter(EntityTemperaturePredicate::baseTemperature),
                            MinMaxBounds.Doubles.CODEC.optionalFieldOf("current_temperature", MinMaxBounds.Doubles.ANY)
                                    .forGetter(EntityTemperaturePredicate::currentTemperature),
                            MinMaxBounds.Doubles.CODEC.optionalFieldOf("target_temperature", MinMaxBounds.Doubles.ANY)
                                    .forGetter(EntityTemperaturePredicate::targetTemperature),
                            MinMaxBounds.Doubles.CODEC.optionalFieldOf("unmodified_temperature", MinMaxBounds.Doubles.ANY)
                                    .forGetter(EntityTemperaturePredicate::unmodifiedTemperature)
                    )
                    .apply(instance, EntityTemperaturePredicate::new)
    );

    public MapCodec<? extends EntitySubPredicate> getCodec() {
        return null;
    }

    public boolean test(Entity entity) {
        if (TemperatureHelper.lacksTemperature(entity)) {
            return false;
        }

        final var temperatureManager = TemperatureHelper.getTemperatureManager(entity);

        if (!baseTemperature.matches(temperatureManager.getBaseTemperature())) {
            return false;
        } else if (!currentTemperature.matches(temperatureManager.getModifiedTemperature())) {
            return false;
        } else if (!targetTemperature.matches(temperatureManager.getTargetTemperature())) {
            return false;
        } else return unmodifiedTemperature.matches(temperatureManager.getTemperature());
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private MinMaxBounds.Doubles baseTemperature;
        private MinMaxBounds.Doubles currentTemperature;
        private MinMaxBounds.Doubles targetTemperature;
        private MinMaxBounds.Doubles unmodifiedTemperature;

        public static EntityTemperaturePredicate.Builder create() {
            return new EntityTemperaturePredicate.Builder();
        }

        public EntityTemperaturePredicate.Builder setBaseTemperature(MinMaxBounds.Doubles baseTemperature) {
            this.baseTemperature = baseTemperature;
            return this;
        }

        public EntityTemperaturePredicate.Builder setCurrentTemperature(MinMaxBounds.Doubles currentTemperature) {
            this.currentTemperature = currentTemperature;
            return this;
        }

        public EntityTemperaturePredicate.Builder setTargetTemperature(MinMaxBounds.Doubles targetTemperature) {
            this.targetTemperature = targetTemperature;
            return this;
        }

        public EntityTemperaturePredicate.Builder setUnmodifiedTemperature(MinMaxBounds.Doubles unmodifiedTemperature) {
            this.unmodifiedTemperature = unmodifiedTemperature;
            return this;
        }

        public EntityTemperaturePredicate build() {
            return new EntityTemperaturePredicate(this.baseTemperature, this.currentTemperature, this.targetTemperature, this.unmodifiedTemperature);
        }
    }
}
