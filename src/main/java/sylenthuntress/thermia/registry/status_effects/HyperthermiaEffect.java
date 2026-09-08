package sylenthuntress.thermia.registry.status_effects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;

public class HyperthermiaEffect extends MobEffect {
    private boolean canDamage;

    public HyperthermiaEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        if (canDamage && !entity.isOnFire()) {
            entity.hurtServer(world, entity.damageSources().onFire(), 0.5F);
            canDamage = false;
        }
        else if (entity instanceof Player player) {
            player.causeFoodExhaustion(0.01F * (float) (amplifier + 1));
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int damageInterval = 120 >> amplifier;
        canDamage = damageInterval == 0 || duration % damageInterval == 0;
        return true;
    }
}
