package sylenthuntress.thermia.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.data_component.ConsumableTemperatureComponent;
import sylenthuntress.thermia.registry.data_component.SunBlockingComponent;
import sylenthuntress.thermia.registry.data_component.TemperatureModifiersComponent;

public class ThermiaComponents {
    public static final DataComponentType<ConsumableTemperatureComponent> CONSUMABLE_TEMPERATURE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Thermia.modIdentifier("consumable_temperature"),
            DataComponentType.<ConsumableTemperatureComponent>builder().persistent(ConsumableTemperatureComponent.CODEC).build()
    );

    public static final DataComponentType<TemperatureModifiersComponent> TEMPERATURE_MODIFIERS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Thermia.modIdentifier("temperature_modifiers"),
            DataComponentType.<TemperatureModifiersComponent>builder().persistent(TemperatureModifiersComponent.CODEC).build()
    );
    public static final DataComponentType<SunBlockingComponent> SUN_BLOCKING = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Thermia.modIdentifier("sun_blocking"),
            DataComponentType.<SunBlockingComponent>builder().persistent(SunBlockingComponent.CODEC).build()
    );

    public static void registerAll() {

    }
}
