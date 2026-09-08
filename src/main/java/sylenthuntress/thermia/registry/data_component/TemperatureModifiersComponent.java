package sylenthuntress.thermia.registry.data_component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlotGroup;
import sylenthuntress.thermia.temperature.TemperatureModifier;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record TemperatureModifiersComponent(List<Entry> modifiers) {
    public static final TemperatureModifiersComponent DEFAULT = new TemperatureModifiersComponent(List.of());
    public static final DecimalFormat DECIMAL_FORMAT = Util.make(
            new DecimalFormat("#.##"), format -> format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT))
    );
    private static final Codec<TemperatureModifiersComponent> BASE_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            TemperatureModifiersComponent.Entry.CODEC.listOf().fieldOf("modifiers").forGetter(TemperatureModifiersComponent::modifiers)
                    )
                    .apply(instance, TemperatureModifiersComponent::new)
    );
    public static final Codec<TemperatureModifiersComponent> CODEC = Codec.withAlternative(
            BASE_CODEC, TemperatureModifiersComponent.Entry.CODEC.listOf(), TemperatureModifiersComponent::new
    );

    public static final StreamCodec<ByteBuf, TemperatureModifiersComponent> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.<ByteBuf, Entry>list().apply(Entry.STREAM_CODEC), TemperatureModifiersComponent::modifiers,
        TemperatureModifiersComponent::new
    );

    public TemperatureModifiersComponent with(TemperatureModifier modifier, EquipmentSlotGroup slot) {
        if (hasModifier(modifier.id())) {
            return this;
        }

        ArrayList<Entry> newModifiers = new ArrayList<>(modifiers);

        newModifiers.add(
                new Entry(
                        modifier,
                        slot
                )
        );

        return new TemperatureModifiersComponent(newModifiers);
    }

    public boolean hasModifier(Identifier id) {
        return modifiers.stream().anyMatch((entry -> entry.modifier().idMatches(id)));
    }

    public record Entry(TemperatureModifier modifier, EquipmentSlotGroup slot) {
        public static final Codec<TemperatureModifiersComponent.Entry> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                TemperatureModifier.MAP_CODEC.forGetter(TemperatureModifiersComponent.Entry::modifier),
                                EquipmentSlotGroup.CODEC.optionalFieldOf("slot", EquipmentSlotGroup.ANY).forGetter(TemperatureModifiersComponent.Entry::slot)
                        )
                        .apply(instance, TemperatureModifiersComponent.Entry::new)
        );

        public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
            TemperatureModifier.STREAM_CODEC, Entry::modifier,
            EquipmentSlotGroup.STREAM_CODEC, Entry::slot,
            Entry::new
        );
    }
}
