package sylenthuntress.thermia.data.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.server.level.ServerPlayer;
import sylenthuntress.thermia.registry.ThermiaCriteria;

import java.util.Optional;

public class FreezeCriterion extends SimpleCriterionTrigger<FreezeCriterion.Conditions> {
    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, int amplifier) {
        this.trigger(player, conditions -> conditions.matches(amplifier));
    }

    public record Conditions(Optional<ContextAwarePredicate> player,
                             MinMaxBounds.Ints amplifier) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<FreezeCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(FreezeCriterion.Conditions::player),
                                MinMaxBounds.Ints.CODEC.optionalFieldOf("amplifier", MinMaxBounds.Ints.ANY).forGetter(FreezeCriterion.Conditions::amplifier)
                        )
                        .apply(instance, FreezeCriterion.Conditions::new)
        );

        public static Criterion<FreezeCriterion.Conditions> create() {
            return create(MinMaxBounds.Ints.ANY);
        }

        public static Criterion<FreezeCriterion.Conditions> create(MinMaxBounds.Ints amplifier) {
            return ThermiaCriteria.PLAYER_FROZEN.createCriterion(
                    new FreezeCriterion.Conditions(
                            Optional.empty(),
                            amplifier
                    )
            );
        }

        public boolean matches(int amplifier) {
            return amplifier().matches(amplifier);
        }
    }
}
