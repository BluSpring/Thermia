package sylenthuntress.thermia.registry.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import sylenthuntress.thermia.compat.SereneSeasonsCompatBase;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.temperature.TemperatureHelper;
import sylenthuntress.thermia.temperature.TemperatureManager;
import sylenthuntress.thermia.temperature.TemperatureModifier;

import java.util.ServiceLoader;
import java.util.stream.Stream;

public class TemperatureCommand {
    private static final DynamicCommandExceptionType ENTITY_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            name -> Component.translatableEscape("commands.temperature.failure.entity.invalid", name)
    );
    private static final DynamicCommandExceptionType INVALID_MODIFIER_EXCEPTION = new DynamicCommandExceptionType(
            id -> Component.translatableEscape("commands.temperature.failure.modifier.invalid", id)
    );
    private static final DynamicCommandExceptionType DUPLICATE_MODIFIER_EXCEPTION = new DynamicCommandExceptionType(
            id -> Component.translatableEscape("commands.temperature.failure.modifier.duplicate", id)
    );
    private static final DynamicCommandExceptionType NO_MODIFIERS_EXCEPTION = new DynamicCommandExceptionType(
            name -> Component.translatableEscape("commands.temperature.failure.modifier.none", name)
    );
    private static final DynamicCommandExceptionType INVALID_POSITION_EXCEPTION = new DynamicCommandExceptionType(
            position -> Component.translatableEscape("commands.temperature.failure.position.invalid", position)
    );
    private static final DynamicCommandExceptionType UNLOADED_POSITION_EXCEPTION = new DynamicCommandExceptionType(
            position -> Component.translatableEscape("commands.temperature.failure.position.unloaded", position)
    );
    private static final DynamicCommandExceptionType ODD_EXCEPTION = new DynamicCommandExceptionType(
            none -> Component.translatableEscape("commands.temperature.failure.what")
    );

    // Thanks to eggohito for the suggestion on optimizing this!
    public static void register(CommandNode<CommandSourceStack> baseNode) {
        var powerNode = Commands.literal("temperature")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .build();

        //  Add the sub-nodes as children of the target selection node
        var targetNode = Commands.argument("target", EntityArgument.entity())
                .build();
        targetNode.addChild(GetEntityTemperatureNode.get());
        targetNode.addChild(SetTemperatureNode.get());
        targetNode.addChild(AddTemperatureNode.get());
        targetNode.addChild(ModifierTemperatureNode.get());

        //  Add the sub-nodes as children of the position selection node
        var positionNode = Commands.argument("position", BlockPosArgument.blockPos())
                .build();
        positionNode.addChild(GetPositionTemperatureNode.get());

        //  Add the selection sub-nodes as children of the main node
        powerNode.addChild(targetNode);
        powerNode.addChild(positionNode);

        //  Add alias
        var aliasNode = Commands.literal("temp")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .build();
        powerNode.getChildren().forEach(aliasNode::addChild);

        //  Add the main nodes as a child of the base node
        baseNode.addChild(powerNode);
        baseNode.addChild(aliasNode);
    }

    public static class GetEntityTemperatureNode {
        public static LiteralCommandNode<CommandSourceStack> get() {
            return Commands.literal("get")
                    .executes(context -> executeCurrent(context.getSource(), EntityArgument.getEntity(context, "target"), 1.0F))
                    .then(Commands.literal("current")
                            .executes(context -> executeCurrent(context.getSource(), EntityArgument.getEntity(context, "target"), 1)).then(Commands.argument("scale", FloatArgumentType.floatArg()).executes(context -> executeCurrent(context.getSource(), EntityArgument.getEntity(context, "target"), FloatArgumentType.getFloat(context, "scale")))))
                    .then(Commands.literal("unmodified")
                            .executes(context -> executeUnmodified(context.getSource(), EntityArgument.getEntity(context, "target"), 1))
                            .then(Commands.argument("scale", FloatArgumentType.floatArg())
                                    .executes(context -> executeUnmodified(context.getSource(), EntityArgument.getEntity(context, "target"), FloatArgumentType.getFloat(context, "scale")))))
                    .then(Commands.literal("base")
                            .executes(context -> executeBase(context.getSource(), EntityArgument.getEntity(context, "target"), 1))
                            .then(Commands.argument("scale", FloatArgumentType.floatArg())
                                    .executes(context -> executeBase(context.getSource(), EntityArgument.getEntity(context, "target"), FloatArgumentType.getFloat(context, "scale")))))
                    .then(Commands.literal("target")
                            .executes(context -> executeTarget(context.getSource(), EntityArgument.getEntity(context, "target"), 1))
                            .then(Commands.argument("scale", FloatArgumentType.floatArg())
                                    .executes(context -> executeTarget(context.getSource(), EntityArgument.getEntity(context, "target"), FloatArgumentType.getFloat(context, "scale"))))).build();
        }

        private static int executeBase(CommandSourceStack source, Entity target, float multiplier) throws CommandSyntaxException {
            if (TemperatureHelper.lacksTemperature(target)) {
                throw ENTITY_FAILED_EXCEPTION.create(target.getName());
            }

            double baseTemperature = ((LivingEntity) target).getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE);
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.temperature.get.entity.success",
                            "Base",
                            target.getName(),
                            TemperatureHelper.TemperatureScaleDisplay.convertForClient(
                                    source.getPlayer(),
                                    baseTemperature
                            )
                    ),
                    false
            );
            return (int) ((baseTemperature * multiplier) * 1000);
        }

        private static int executeCurrent(CommandSourceStack source, Entity target, float multiplier) throws CommandSyntaxException {
            if (TemperatureHelper.lacksTemperature(target)) {
                throw ENTITY_FAILED_EXCEPTION.create(target.getName());
            }

            TemperatureManager temperatureManager = TemperatureHelper.getTemperatureManager(target);
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.temperature.get.entity.success",
                            "Current",
                            target.getName(),
                            TemperatureHelper.TemperatureScaleDisplay.convertForClient(
                                    source.getPlayer(),
                                    temperatureManager.getModifiedTemperature()
                            )
                    ),
                    false
            );
            return (int) ((temperatureManager.getModifiedTemperature() * multiplier) * 1000);
        }

        private static int executeTarget(CommandSourceStack source, Entity target, float multiplier) throws CommandSyntaxException {
            if (TemperatureHelper.lacksTemperature(target)) {
                throw ENTITY_FAILED_EXCEPTION.create(target.getName());
            }

            double targetTemperature = TemperatureHelper.getTemperatureManager(target).getTargetTemperature();
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.temperature.get.entity.success",
                            "Target",
                            target.getName(),
                            TemperatureHelper.TemperatureScaleDisplay.convertForClient(
                                    source.getPlayer(),
                                    targetTemperature
                            )
                    ),
                    false
            );

            return (int) ((targetTemperature * multiplier) * 1000);
        }

        private static int executeUnmodified(CommandSourceStack source, Entity target, float multiplier) throws CommandSyntaxException {
            if (TemperatureHelper.lacksTemperature(target)) {
                throw ENTITY_FAILED_EXCEPTION.create(target.getName());
            }
            TemperatureManager temperatureManager = TemperatureHelper.getTemperatureManager(target);
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.temperature.get.entity.success",
                            "Unmodified",
                            target.getName(),
                            TemperatureHelper.TemperatureScaleDisplay.convertForClient(
                                    source.getPlayer(),
                                    temperatureManager.getTemperature()
                            )
                    ),
                    false
            );
            return (int) ((temperatureManager.getTemperature() * multiplier) * 1000);
        }
    }

    public static class SetTemperatureNode {
        public static LiteralCommandNode<CommandSourceStack> get() {
            return Commands.literal("set")
                    .then(Commands.argument("amount", FloatArgumentType.floatArg())
                            .executes(context -> execute(context.getSource(), EntityArgument.getEntity(context, "target"), FloatArgumentType.getFloat(context, "amount")))).build();
        }

        private static int execute(CommandSourceStack source, Entity target, double value) throws CommandSyntaxException {
            if (TemperatureHelper.lacksTemperature(target)) {
                throw ENTITY_FAILED_EXCEPTION.create(target.getName());
            }
            TemperatureManager temperatureManager = TemperatureHelper.getTemperatureManager(target);
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.temperature.change.success",
                            target.getName(),
                            TemperatureHelper.TemperatureScaleDisplay.convertForClient(
                                    source.getPlayer(),
                                    temperatureManager.getTemperature()
                            ),
                            TemperatureHelper.TemperatureScaleDisplay.convertForClient(
                                    source.getPlayer(),
                                    value
                            )
                    ),
                    false
            );
            return (int) (temperatureManager.setTemperature(value) * 1000);
        }
    }

    public static class AddTemperatureNode {
        public static LiteralCommandNode<CommandSourceStack> get() {
            return Commands.literal("add")
                    .then(Commands.argument("amount", FloatArgumentType.floatArg())
                            .executes(context -> execute(context.getSource(), EntityArgument.getEntity(context, "target"), FloatArgumentType.getFloat(context, "amount")))).build();
        }

        private static int execute(CommandSourceStack source, Entity target, double value) throws CommandSyntaxException {
            if (TemperatureHelper.lacksTemperature(target)) {
                throw ENTITY_FAILED_EXCEPTION.create(target.getName());
            }
            TemperatureManager temperatureManager = TemperatureHelper.getTemperatureManager(target);
            double newTemperature = temperatureManager.modifyTemperature(value);
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.temperature.change.success",
                            target.getName(),
                            TemperatureHelper.TemperatureScaleDisplay.convertForClient(
                                    source.getPlayer(),
                                    newTemperature - value
                            ),
                            TemperatureHelper.TemperatureScaleDisplay.convertForClient(
                                    source.getPlayer(),
                                    newTemperature
                            )
                    ),
                    false
            );
            return (int) (newTemperature * 1000);
        }


    }

    public static class ModifierTemperatureNode {
        public static LiteralCommandNode<CommandSourceStack> get() {
            return Commands.literal("modifier")
                    .then(Commands.literal("add")
                            .then(Commands.argument("id", IdentifierArgument.id())
                                    .executes(context -> executeAdd(context.getSource(), EntityArgument.getEntity(context, "target"), IdentifierArgument.getId(context, "id"), DoubleArgumentType.getDouble(context, "amount"), TemperatureModifier.Operation.ADD_VALUE))
                                    .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                            .then(Commands.literal("add_value")
                                                    .executes(context -> executeAdd(context.getSource(), EntityArgument.getEntity(context, "target"), IdentifierArgument.getId(context, "id"), DoubleArgumentType.getDouble(context, "amount"), TemperatureModifier.Operation.ADD_VALUE)))
                                            .then(Commands.literal("add_multiplied_value")
                                                    .executes(context -> executeAdd(context.getSource(), EntityArgument.getEntity(context, "target"), IdentifierArgument.getId(context, "id"), DoubleArgumentType.getDouble(context, "amount"), TemperatureModifier.Operation.ADD_MULTIPLIED_VALUE)))
                                            .then(Commands.literal("set_total")
                                                    .executes(context -> executeAdd(context.getSource(), EntityArgument.getEntity(context, "target"), IdentifierArgument.getId(context, "id"), DoubleArgumentType.getDouble(context, "amount"), TemperatureModifier.Operation.SET_TOTAL))))))
                    .then(Commands.literal("remove")
                            .then(Commands.argument("id", IdentifierArgument.id())
                                    .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(streamModifiers(EntityArgument.getEntity(context, "target")), builder))
                                    .executes(context -> executeRemove(context.getSource(), EntityArgument.getEntity(context, "target"), IdentifierArgument.getId(context, "id"))))
                            .then(Commands.literal("*")
                                    .executes(context -> executeRemoveAll(context.getSource(), EntityArgument.getEntity(context, "target")))))
                    .then(Commands.literal("get")
                            .then(Commands.argument("id", IdentifierArgument.id())
                                    .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(streamModifiers(EntityArgument.getEntity(context, "target")), builder))
                                    .executes(context -> executeGet(context.getSource(), EntityArgument.getEntity(context, "target"), IdentifierArgument.getId(context, "id"), 1))
                                    .then(Commands.argument("scale", FloatArgumentType.floatArg())
                                            .executes(context -> executeGet(context.getSource(), EntityArgument.getEntity(context, "target"), IdentifierArgument.getId(context, "id"), FloatArgumentType.getFloat(context, "scale")))))).build();
        }

        private static int executeRemoveAll(CommandSourceStack source, Entity target) throws CommandSyntaxException {
            if (TemperatureHelper.lacksTemperature(target)) {
                throw ENTITY_FAILED_EXCEPTION.create(target.getName());
            }

            var temperatureModifiers = TemperatureHelper.getTemperatureManager(target).getTemperatureModifiers();
            if (temperatureModifiers.getList()
                    .stream().map(TemperatureModifier::id).noneMatch(TemperatureModifier::notGranted)) {
                throw NO_MODIFIERS_EXCEPTION.create(target.getName());
            }

            int modifierCount = 0;

            for (Identifier id : temperatureModifiers.getList().stream().map(TemperatureModifier::id).toList()) {
                if (TemperatureModifier.isGranted(id)) {
                    continue;
                }

                temperatureModifiers.removeModifier(id);
                modifierCount++;
            }

            int displayedModifierCount = modifierCount;
            source.sendSuccess(
                    () -> Component.translatableEscape(
                            "commands.temperature.modifier.remove_all.success",
                            displayedModifierCount,
                            target.getName()
                    ),
                    false
            );

            return displayedModifierCount;
        }

        private static int executeRemove(CommandSourceStack source, Entity target, Identifier id) throws CommandSyntaxException {
            if (TemperatureHelper.lacksTemperature(target)) {
                throw ENTITY_FAILED_EXCEPTION.create(target.getName());
            }

            var temperatureModifiers = TemperatureHelper.getTemperatureManager(target).getTemperatureModifiers();
            if (!temperatureModifiers.hasModifier(id)) {
                throw INVALID_MODIFIER_EXCEPTION.create(id.toString());
            }

            temperatureModifiers.removeModifier(id);

            source.sendSuccess(
                    () -> Component.translatableEscape(
                            "commands.temperature.modifier.remove.success",
                            id.toString(),
                            target.getName()
                    ),
                    false
            );

            return 1;
        }

        private static Stream<Identifier> streamModifiers(Entity target) throws CommandSyntaxException {
            if (TemperatureHelper.lacksTemperature(target)) {
                throw ENTITY_FAILED_EXCEPTION.create(target.getName());
            }

            var temperatureModifiers = TemperatureHelper.getTemperatureManager(target).getTemperatureModifiers().getList();

            return temperatureModifiers.stream().map(TemperatureModifier::id).filter(TemperatureModifier::notGranted);
        }


        private static int executeGet(CommandSourceStack source, Entity target, Identifier id, double scale) throws CommandSyntaxException {
            if (TemperatureHelper.lacksTemperature(target)) {
                throw ENTITY_FAILED_EXCEPTION.create(target.getName());
            }

            var temperatureModifiers = TemperatureHelper.getTemperatureManager(target).getTemperatureModifiers();
            if (!temperatureModifiers.hasModifier(id)) {
                throw INVALID_MODIFIER_EXCEPTION.create(id.toString());
            }

            TemperatureModifier modifier = temperatureModifiers.getModifier(id);

            source.sendSuccess(
                    () -> Component.translatableEscape(
                            "commands.temperature.modifier.get.success." + modifier.operation().ordinal(),
                            id.toString(),
                            target.getName(),
                            TemperatureHelper.TemperatureScaleDisplay.convertForClient(
                                    source.getPlayer(),
                                    modifier.operation() == TemperatureModifier.Operation.ADD_VALUE
                                            ? modifier.amount()
                                            : modifier.amount() * 100.0
                            )
                    ),
                    false
            );

            return (int) ((modifier.amount() * scale) * 1000);
        }

        private static int executeAdd(CommandSourceStack source, Entity target, Identifier id, double value, TemperatureModifier.Operation operation) throws CommandSyntaxException {
            if (TemperatureHelper.lacksTemperature(target)) {
                throw ENTITY_FAILED_EXCEPTION.create(target.getName());
            }

            var temperatureModifiers = TemperatureHelper.getTemperatureManager(target).getTemperatureModifiers();
            if (temperatureModifiers.hasModifier(id)) {
                throw DUPLICATE_MODIFIER_EXCEPTION.create(id.toString());
            }

            temperatureModifiers.addModifier(new TemperatureModifier(id, value, operation));

            source.sendSuccess(
                    () -> Component.translatableEscape(
                            "commands.temperature.modifier.add.success",
                            id.toString(),
                            target.getName()
                    ),
                    false
            );

            return 1;
        }
    }

    public static class GetPositionTemperatureNode {
        public static LiteralCommandNode<CommandSourceStack> get() {
            var node = Commands.literal("get")
                    .executes(context -> executeAmbient(context.getSource(), BlockPosArgument.getBlockPos(context, "position"), 1.0F))
                    .then(Commands.literal("ambient")
                            .executes(context -> executeAmbient(context.getSource(), BlockPosArgument.getBlockPos(context, "position"), 1))
                            .then(Commands.argument("scale", FloatArgumentType.floatArg())
                                    .executes(context -> executeAmbient(context.getSource(), BlockPosArgument.getBlockPos(context, "position"), FloatArgumentType.getFloat(context, "scale")))))
                    .then(Commands.literal("block")
                            .executes(context -> executeBlock(context.getSource(), BlockPosArgument.getBlockPos(context, "position"), 1))
                            .then(Commands.argument("scale", FloatArgumentType.floatArg())
                                    .executes(context -> executeBlock(context.getSource(), BlockPosArgument.getBlockPos(context, "position"), FloatArgumentType.getFloat(context, "scale")))))
                    .then(Commands.literal("fluid")
                            .executes(context -> executeFluid(context.getSource(), BlockPosArgument.getBlockPos(context, "position"), 1))
                            .then(Commands.argument("scale", FloatArgumentType.floatArg())
                                    .executes(context -> executeFluid(context.getSource(), BlockPosArgument.getBlockPos(context, "position"), FloatArgumentType.getFloat(context, "scale")))))
                    .then(Commands.literal("regional")
                            .executes(context -> executeRegional(context.getSource(), BlockPosArgument.getBlockPos(context, "position"), 1))
                            .then(Commands.argument("scale", FloatArgumentType.floatArg())
                                    .executes(context -> executeRegional(context.getSource(), BlockPosArgument.getBlockPos(context, "position"), FloatArgumentType.getFloat(context, "scale"))))).build();

            if (FabricLoader.getInstance().isModLoaded("sereneseasons")) {
                node.addChild(
                        Commands.literal("seasonal")
                                .executes(context -> executeSeasonal(context.getSource(), 1))
                                .then(Commands.argument("scale", FloatArgumentType.floatArg())
                                        .executes(context -> executeSeasonal(context.getSource(), FloatArgumentType.getFloat(context, "scale")))).build()
                );
            }

            return node;
        }

        private static int executeRegional(CommandSourceStack source, BlockPos blockPos, float multiplier) throws CommandSyntaxException {
            if (!Level.isInSpawnableBounds(blockPos) || source.getLevel().isInWorldBounds(blockPos))
                throw INVALID_POSITION_EXCEPTION.create(blockPos.toShortString());
            if (!source.getLevel().isLoaded(blockPos))
                throw UNLOADED_POSITION_EXCEPTION.create(blockPos.toShortString());

            double regionalTemperature = TemperatureHelper.getRegionalTemperature(source.getLevel(), blockPos);
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.temperature.get.position.success",
                            "Regional",
                            blockPos.toShortString(),
                            TemperatureHelper.TemperatureScaleDisplay.convertForClient(
                                    source.getPlayer(),
                                    regionalTemperature
                            )
                    ),
                    false
            );

            return (int) ((regionalTemperature * multiplier) * 1000);
        }

        private static int executeAmbient(CommandSourceStack source, BlockPos blockPos, float multiplier) throws CommandSyntaxException {
            if (!Level.isInSpawnableBounds(blockPos) || source.getLevel().isInWorldBounds(blockPos))
                throw INVALID_POSITION_EXCEPTION.create(blockPos.toShortString());
            if (!source.getLevel().isLoaded(blockPos))
                throw UNLOADED_POSITION_EXCEPTION.create(blockPos.toShortString());

            double ambientTemperature = TemperatureHelper.getAmbientTemperature(source.getLevel(), blockPos);
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.temperature.get.position.success",
                            "Ambient",
                            blockPos.toShortString(),
                            TemperatureHelper.TemperatureScaleDisplay.convertForClient(
                                    source.getPlayer(),
                                    ambientTemperature
                            )
                    ),
                    false
            );

            return (int) ((ambientTemperature * multiplier) * 1000);
        }

        private static int executeBlock(CommandSourceStack source, BlockPos blockPos, float multiplier) throws CommandSyntaxException {
            if (!Level.isInSpawnableBounds(blockPos) || source.getLevel().isInWorldBounds(blockPos))
                throw INVALID_POSITION_EXCEPTION.create(blockPos.toShortString());
            if (!source.getLevel().isLoaded(blockPos))
                throw UNLOADED_POSITION_EXCEPTION.create(blockPos.toShortString());

            double blockTemperature = TemperatureHelper.getBlockTemperature(source.getLevel(), blockPos);
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.temperature.get.position.success",
                            "Block",
                            blockPos.toShortString(),
                            TemperatureHelper.TemperatureScaleDisplay.convertForClient(
                                    source.getPlayer(),
                                    97 + blockTemperature
                            )
                    ),
                    false
            );

            return (int) ((blockTemperature * multiplier) * 1000);
        }

        private static int executeFluid(CommandSourceStack source, BlockPos blockPos, float multiplier) throws CommandSyntaxException {
            if (!Level.isInSpawnableBounds(blockPos) || source.getLevel().isInWorldBounds(blockPos))
                throw INVALID_POSITION_EXCEPTION.create(blockPos.toShortString());
            if (!source.getLevel().isLoaded(blockPos))
                throw UNLOADED_POSITION_EXCEPTION.create(blockPos.toShortString());

            double fluidTemperature = TemperatureHelper.getFluidTemperature(source.getLevel(), blockPos);
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.temperature.get.position.success",
                            "Fluid",
                            blockPos.toShortString(),
                            TemperatureHelper.TemperatureScaleDisplay.convertForClient(
                                    source.getPlayer(),
                                    97 + fluidTemperature
                            )
                    ),
                    false
            );

            return (int) ((fluidTemperature * multiplier) * 1000);
        }


        private static int executeSeasonal(CommandSourceStack source, float multiplier) throws CommandSyntaxException {
            Level world = source.getLevel();
            ServiceLoader<SereneSeasonsCompatBase> loader = ServiceLoader.load(SereneSeasonsCompatBase.class);
            if (loader.findFirst().isEmpty()) {
                throw ODD_EXCEPTION.create(0);
            }
            var season = loader.findFirst().get().getSeasonState(world).getSubSeason();

            double seasonTemperature = TemperatureHelper.getSeasonalTemperature(source.getLevel());
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.temperature.get.season.success",
                            season.getSeason().name().toLowerCase(),
                            TemperatureHelper.TemperatureScaleDisplay.convertForClient(
                                    source.getPlayer(),
                                    97 + seasonTemperature
                            )
                    ),
                    false
            );
            return (int) ((seasonTemperature * multiplier) * 1000);
        }
    }
}
