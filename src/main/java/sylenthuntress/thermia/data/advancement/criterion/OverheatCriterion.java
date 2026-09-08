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

public class OverheatCriterion extends SimpleCriterionTrigger<OverheatCriterion.Conditions> {
    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, int amplifier) {
        this.trigger(player, conditions -> conditions.matches(amplifier));
    }

    public record Conditions(Optional<ContextAwarePredicate> player,
                             MinMaxBounds.Ints amplifier) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<OverheatCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(OverheatCriterion.Conditions::player),
                                MinMaxBounds.Ints.CODEC.optionalFieldOf("amplifier", MinMaxBounds.Ints.ANY).forGetter(OverheatCriterion.Conditions::amplifier)
                        )
                        .apply(instance, OverheatCriterion.Conditions::new)
        );

        public static Criterion<OverheatCriterion.Conditions> create() {
            return create(MinMaxBounds.Ints.ANY);
        }

        public static Criterion<OverheatCriterion.Conditions> create(MinMaxBounds.Ints amplifier) {
            return ThermiaCriteria.PLAYER_OVERHEATING.createCriterion(
                    new OverheatCriterion.Conditions(
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
