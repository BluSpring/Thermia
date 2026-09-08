package sylenthuntress.thermia.registry.data_component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.util.ExtraCodecs;

public record SunBlockingComponent(float amount, EquipmentSlotGroup slot) {
    public static final SunBlockingComponent DEFAULT = new SunBlockingComponent(0, EquipmentSlotGroup.ANY);

    public static final Codec<SunBlockingComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("amount").forGetter(SunBlockingComponent::amount),
            EquipmentSlotGroup.CODEC.optionalFieldOf("slot", EquipmentSlotGroup.ANY).forGetter(SunBlockingComponent::slot)
    ).apply(builder, SunBlockingComponent::new));
}
