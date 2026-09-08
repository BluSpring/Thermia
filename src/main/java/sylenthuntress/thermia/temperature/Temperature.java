package sylenthuntress.thermia.temperature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import sylenthuntress.thermia.registry.ThermiaAttributes;

public record Temperature(double value) {
    public final static Temperature DEFAULT = new Temperature(97);
    public static Codec<Temperature> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("temperature").forGetter(Temperature::value)
    ).apply(instance, Temperature::new));
    public static StreamCodec<ByteBuf, Temperature> PACKET_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, Temperature::value,
        Temperature::new
    );

    public Temperature(LivingEntity entity) {
        this(entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE));
    }

    public static Temperature setValue(double newTemperature) {
        return new Temperature(newTemperature);
    }
}
