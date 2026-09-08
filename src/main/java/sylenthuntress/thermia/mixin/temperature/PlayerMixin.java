package sylenthuntress.thermia.mixin.temperature;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.registry.ThermiaAttachmentTypes;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;
import sylenthuntress.thermia.temperature.GrantedThermoregulation;
import sylenthuntress.thermia.temperature.TemperatureHelper;

import java.util.List;
import java.util.UUID;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Unique
    protected boolean thermia$applyThermoregulation = false;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void thermia$allowThermoregulation(Level world, GameProfile gameProfile, CallbackInfo ci) {
        if (world.isClientSide()) {
            return;
        }

        // Apply thermoregulation on first join
        final List<UUID> grantedThermoregulationToList = world.getAttachedOrCreate(
                ThermiaAttachmentTypes.GRANTED_THERMOREGULATION
        ).playerUUIDs();

        if (grantedThermoregulationToList.contains(gameProfile.id())) {
            return;
        }

        thermia$applyThermoregulation = true;
        world.setAttached(
                ThermiaAttachmentTypes.GRANTED_THERMOREGULATION,
                GrantedThermoregulation.addPlayer(
                        grantedThermoregulationToList,
                        gameProfile
                )
        );
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void thermia$applyThermoregulation(CallbackInfo ci) {
        if (!thermia$applyThermoregulation) {
            return;
        }

        thermia$applyThermoregulation = false;

        this.addEffect(
                new MobEffectInstance(
                        ThermiaStatusEffects.THERMOREGULATION,
                        6000,
                        0,
                        false,
                        false,
                        true
                ),
                null
        );
    }

    @ModifyVariable(
            method = "causeFoodExhaustion",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float thermia$modifyExhaustion(float exhaustion) {
        if (TemperatureHelper.getTemperatureManager((Player) (Object) this).isHyperthermic())
            exhaustion *= 2;
        return exhaustion;
    }
}
