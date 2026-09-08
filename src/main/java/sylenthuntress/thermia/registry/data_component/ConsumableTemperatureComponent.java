package sylenthuntress.thermia.registry.data_component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ConsumableListener;
import net.minecraft.world.level.Level;
import sylenthuntress.thermia.access.LivingEntityAccess;
import sylenthuntress.thermia.temperature.TemperatureManager;

import java.util.Random;

public record ConsumableTemperatureComponent(double temperature, double minTemperature,
                                             double maxTemperature) implements ConsumableListener {
    public static final Codec<ConsumableTemperatureComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.DOUBLE.fieldOf("temperature").forGetter(ConsumableTemperatureComponent::temperature),
                            Codec.DOUBLE.optionalFieldOf("min_temperature", 0.0).forGetter(ConsumableTemperatureComponent::minTemperature),
                            Codec.DOUBLE.optionalFieldOf("max_temperature", 0.0).forGetter(ConsumableTemperatureComponent::maxTemperature)
                    )
                    .apply(instance, ConsumableTemperatureComponent::new)
    );

    public static final StreamCodec<ByteBuf, ConsumableTemperatureComponent> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, ConsumableTemperatureComponent::temperature,
        ByteBufCodecs.DOUBLE, ConsumableTemperatureComponent::minTemperature,
        ByteBufCodecs.DOUBLE, ConsumableTemperatureComponent::maxTemperature,
        ConsumableTemperatureComponent::new
    );

    public ConsumableTemperatureComponent(double... temperatures) {
        this(temperatures[0], temperatures[1], temperatures[2]);
    }

    @Override
    public void onConsume(Level world, LivingEntity user, ItemStack stack, Consumable consumable) {
        double temperature = temperature();

        if (maxTemperature > minTemperature) {
            temperature += random.nextDouble(
                    (int) Math.round(minTemperature * 1000),
                    (int) Math.round(maxTemperature * 1000)
            ) * 0.001;
        }

        if (temperature == 0) {
            return;
        }

        TemperatureManager temperatureManager = ((LivingEntityAccess) user).thermia$getTemperatureManager();
        temperatureManager.modifyTemperature(temperature);
    }

    private static final Random random = new Random();
}
