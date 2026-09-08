package sylenthuntress.thermia.registry;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import sylenthuntress.thermia.Thermia;

public class ThermiaAttributes {
    public static final Holder.Reference<Attribute> BASE_TEMPERATURE = Registry.registerForHolder(
            BuiltInRegistries.ATTRIBUTE,
            Thermia.modIdentifier("base_temperature"),
            new RangedAttribute(
                    "attribute.name.generic.base_temperature",
                    97,
                    -100,
                    200
            ).setSyncable(true).setSentiment(Attribute.Sentiment.POSITIVE)
    );
    public static final Holder.Reference<Attribute> HEAT_OFFSET_THRESHOLD = Registry.registerForHolder(
            BuiltInRegistries.ATTRIBUTE,
            Thermia.modIdentifier("heat_offset_threshold"),
            new RangedAttribute(
                    "attribute.name.generic.heat_offset_threshold",
                    3,
                    -255,
                    255
            ).setSyncable(true).setSentiment(Attribute.Sentiment.POSITIVE)
    );
    public static final Holder.Reference<Attribute> COLD_OFFSET_THRESHOLD = Registry.registerForHolder(
            BuiltInRegistries.ATTRIBUTE,
            Thermia.modIdentifier("cold_offset_threshold"),
            new RangedAttribute(
                    "attribute.name.generic.cold_offset_threshold",
                    2,
                    -255,
                    255
            ).setSyncable(true).setSentiment(Attribute.Sentiment.POSITIVE)
    );

    public static void registerAll() {

    }
}