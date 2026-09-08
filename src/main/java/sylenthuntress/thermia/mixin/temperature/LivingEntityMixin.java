package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sylenthuntress.thermia.access.LivingEntityAccess;
import sylenthuntress.thermia.data.ThermiaTags;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;
import sylenthuntress.thermia.registry.data_component.TemperatureModifiersComponent;
import sylenthuntress.thermia.temperature.TemperatureHelper;
import sylenthuntress.thermia.temperature.TemperatureManager;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityAccess {
    @Unique
    private TemperatureManager thermia$temperatureManager;

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyReturnValue(
            method = "createLivingAttributes",
            at = @At("RETURN")
    )
    private static AttributeSupplier.Builder thermia$addAttributes(AttributeSupplier.Builder original) {
        return original
                .add(ThermiaAttributes.BASE_TEMPERATURE)
                .add(ThermiaAttributes.COLD_OFFSET_THRESHOLD)
                .add(ThermiaAttributes.HEAT_OFFSET_THRESHOLD);
    }

    @ModifyReturnValue(
            method = "canFreeze",
            at = @At("RETURN")
    )
    private boolean thermia$applyFrostResistance(boolean original) {
        return original
                && !this.hasEffect(ThermiaStatusEffects.FROST_RESISTANCE);
    }

    @Shadow
    public abstract boolean isInvulnerableTo(ServerLevel world, DamageSource source);

    @Shadow
    public abstract boolean hasEffect(Holder<MobEffect> effect);

    @Shadow
    public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    @Shadow
    public abstract double getAttributeValue(Holder<Attribute> attribute);

    @Shadow
    public abstract double getAttributeBaseValue(Holder<Attribute> attribute);

    public TemperatureManager thermia$getTemperatureManager() {
        return thermia$temperatureManager;
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void thermia$setTemperatureManager(EntityType<? extends LivingEntity> entityType, Level world, CallbackInfo ci) {
        thermia$temperatureManager = new TemperatureManager((LivingEntity) (Object) this);
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void thermia$calculateTemperature(CallbackInfo ci) {
        if (this.level().isClientSide()) {
            if (thermia$temperatureManager.doHeatEffects() && this.tickCount % 6 == 0) {
                this.level().addParticle(
                        ParticleTypes.FALLING_WATER,
                        true,
                        true,
                        this.getRandomX(0.5),
                        this.getRandomY(),
                        this.getRandomZ(0.5),
                        0.0,
                        0.0,
                        0.0
                );
            }

            return;
        }

        if (this.tickCount % 5 == 0) {
            thermia$temperatureManager.stepPassiveTemperature();
        }
    }

    @Inject(
            method = "actuallyHurt",
            at = @At(value = "TAIL")
    )
    private void thermia$damageInteractions(ServerLevel world, DamageSource source, float amount, CallbackInfo ci) {
        if (!this.isInvulnerableTo(world, source)) {
            TemperatureManager temperatureManager = TemperatureHelper.getTemperatureManager((LivingEntity) (Object) this);
            double[] interactionTemperatures = {0, 0};
            if (source.getEntity() != null) {
                if (source.is(DamageTypeTags.IS_FREEZING))
                    interactionTemperatures[0] -= 1.5;
                if (source.is(DamageTypeTags.IS_FIRE))
                    interactionTemperatures[1] += 1.5;
                if (source.getEntity().getType().is(ThermiaTags.EntityType.UNDEAD))
                    interactionTemperatures[0] -= 0.1;
                if (source.getEntity().getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES))
                    interactionTemperatures[1] += 0.5;
            } else {
                if (source.is(DamageTypeTags.BURN_FROM_STEPPING))
                    interactionTemperatures[1] += 0.5;
            }
            if (source.getDirectEntity() instanceof Snowball)
                interactionTemperatures[0] -= 3;
            if (source.is(DamageTypeTags.IS_LIGHTNING))
                interactionTemperatures[1] += 10;
            temperatureManager.modifyTemperature(interactionTemperatures);
        }
    }

    @Inject(
            method = "collectEquipmentChanges",
            at = @At("HEAD")
    )
    private void thermia$addTemperatureModifiers(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            final ItemStack stack = this.getItemBySlot(slot);

            for (TemperatureModifiersComponent.Entry entry : stack.getOrDefault(
                    ThermiaComponents.TEMPERATURE_MODIFIERS,
                    TemperatureModifiersComponent.DEFAULT
            ).modifiers()) {
                if (!entry.slot().test(slot)) {
                    continue;
                }

                thermia$temperatureManager.getTemperatureModifiers().addGrantedModifier(
                        entry.modifier()
                );
            }
        }
    }

    @Inject(
            method = "stopLocationBasedEffects",
            at = @At("TAIL")
    )
    private void thermia$removeTemperatureModifiers(ItemStack removedEquipment, EquipmentSlot slot, AttributeMap container, CallbackInfo ci) {
        for (TemperatureModifiersComponent.Entry entry : removedEquipment.getOrDefault(
                ThermiaComponents.TEMPERATURE_MODIFIERS,
                TemperatureModifiersComponent.DEFAULT
        ).modifiers()) {
            if (!entry.slot().test(slot)) {
                continue;
            }

            thermia$temperatureManager.getTemperatureModifiers().removeModifier(
                    entry.modifier().id().withPrefix("granted/")
            );
        }
    }

    @ModifyReturnValue(
            method = "canFreeze",
            at = @At("RETURN")
    )
    private boolean thermia$applyInsulation(boolean original) {
        return original && this.getAttributeValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD)
                < 8 + this.getAttributeBaseValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD);
    }
}