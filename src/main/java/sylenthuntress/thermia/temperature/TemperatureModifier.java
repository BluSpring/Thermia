package sylenthuntress.thermia.temperature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record TemperatureModifier(Identifier id, double amount, TemperatureModifier.Operation operation) {
    public static final MapCodec<TemperatureModifier> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Identifier.CODEC.fieldOf("id").forGetter(TemperatureModifier::id),
                            Codec.DOUBLE.fieldOf("amount").forGetter(TemperatureModifier::amount),
                            TemperatureModifier.Operation.CODEC.fieldOf("operation").forGetter(TemperatureModifier::operation)
                    )
                    .apply(instance, TemperatureModifier::new)
    );

    public boolean idMatches(Identifier id) {
        return id.equals(this.id);
    }

    public static boolean notGranted(Identifier id) {
        return !isGranted(id);
    }

    public static boolean isGranted(Identifier id) {
        return id.toString().startsWith("thermia:granted/");
    }

    public enum Operation implements StringRepresentable {
        ADD_VALUE("add_value", 0, AttributeModifier.Operation.ADD_VALUE),
        ADD_MULTIPLIED_VALUE("add_multiplied_value", 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
        SET_TOTAL("set_total", 2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        public static final Codec<TemperatureModifier.Operation> CODEC = StringRepresentable.fromEnum(TemperatureModifier.Operation::values);

        private final String name;
        private final int id;
        private final AttributeModifier.Operation attributeOperation;

        Operation(final String name, final int id, AttributeModifier.Operation operation) {
            this.name = name;
            this.id = id;
            this.attributeOperation = operation;
        }

        public int getId() {
            return this.id;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static Operation asTemperatureOperation(AttributeModifier.Operation operation) {
            return switch (operation) {
                case ADD_VALUE -> ADD_VALUE;
                case ADD_MULTIPLIED_BASE -> ADD_MULTIPLIED_VALUE;
                case ADD_MULTIPLIED_TOTAL -> SET_TOTAL;
            };
        }

        public AttributeModifier.Operation asAttributeOperation() {
            return this.attributeOperation;
        }
    }
}
