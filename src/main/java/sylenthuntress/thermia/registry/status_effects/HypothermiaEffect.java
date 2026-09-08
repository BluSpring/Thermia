package sylenthuntress.thermia.registry.status_effects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.server.level.ServerLevel;
import sylenthuntress.thermia.Thermia;

public class HypothermiaEffect extends MobEffect {
    public HypothermiaEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        if (!entity.isFullyFrozen()) {
            entity.hurtServer(world, entity.damageSources().freeze(), 0.5F);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int damageInterval = 120 >> amplifier;
        return damageInterval == 0 || duration % damageInterval == 0;
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        super.onEffectStarted(entity, amplifier);

        AttributeInstance attribute = entity.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);

        if (attribute != null && !attribute.hasModifier(Thermia.modIdentifier("effect.hypothermia.slowness")))
            attribute.addPermanentModifier(
                    new AttributeModifier(
                            Thermia.modIdentifier("effect.hypothermia.slowness"),
                            -(0.05 * (1 + amplifier * 0.1)),
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void removeAttributeModifiers(AttributeMap container) {
        container.getInstance(Attributes.MOVEMENT_SPEED)
                .removeModifier(Thermia.modIdentifier("effect.hypothermia.slowness"));
    }
}
