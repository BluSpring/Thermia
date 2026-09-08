package sylenthuntress.thermia.registry;

import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import sylenthuntress.thermia.Thermia;

public class ThermiaItems {
    public static final Item THERMIA_ICON = register(
            Thermia.modIdentifier("icon")
    );

    protected static Item register(ResourceLocation id) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)))
        );
    }

    public static void registerAll() {

    }
}
