package sylenthuntress.thermia.data.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import sylenthuntress.thermia.registry.ThermiaCriteria;

import java.util.Optional;

public class TemperatureChangedCriterion extends SimpleCriterionTrigger<TemperatureChangedCriterion.Conditions> {
    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, double temperature, boolean modified) {
        this.trigger(player, conditions -> conditions.matches(temperature, modified));
    }

    public record Conditions(Optional<ContextAwarePredicate> player, MinMaxBounds.Doubles temperature,
                             boolean modified) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TemperatureChangedCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TemperatureChangedCriterion.Conditions::player),
                                MinMaxBounds.Doubles.CODEC.optionalFieldOf("temperature", MinMaxBounds.Doubles.ANY).forGetter(TemperatureChangedCriterion.Conditions::temperature),
                                Codec.BOOL.optionalFieldOf("modified", true).forGetter(TemperatureChangedCriterion.Conditions::modified)
                        )
                        .apply(instance, TemperatureChangedCriterion.Conditions::new)
        );

        public static Criterion<TemperatureChangedCriterion.Conditions> create(MinMaxBounds.Doubles temperature, boolean modified) {
            return ThermiaCriteria.TEMPERATURE_CHANGED.createCriterion(
                    new TemperatureChangedCriterion.Conditions(
                            Optional.empty(),
                            temperature,
                            modified
                    )
            );
        }

        public boolean matches(double temperature, boolean modified) {
            return temperature().matches(temperature) && modified == modified();
        }
    }
}
