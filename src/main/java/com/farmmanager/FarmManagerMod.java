package com.farmmanager;

import com.farmmanager.command.FarmCommand;
import com.farmmanager.data.FarmWeatherData;
import com.farmmanager.data.SpawnAreaData;
import com.farmmanager.dimension.FarmDimension;
import com.farmmanager.mixin.MappedRegistryAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public class FarmManagerMod implements ModInitializer {
    public static final String MOD_ID = "farmmanager";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Random RANDOM = new Random();

    private static final int RAIN_DURATION_TICKS = 6000;
    private static final int SPAWN_INTERVAL = 80;
    private static final int RAIN_INTERVAL = 20;
    private static final int RAIN_HEIGHT = 40;

    private static List<ItemStack> rainItems;

    private static List<ItemStack> getRainItems() {
        if (rainItems == null) {
            rainItems = List.of(
                new ItemStack(Items.WHEAT_SEEDS, 3),
                new ItemStack(Items.CARROT, 2),
                new ItemStack(Items.POTATO, 2),
                new ItemStack(Items.BEETROOT_SEEDS, 3),
                new ItemStack(Items.DIRT, 2),
                new ItemStack(Items.COBBLESTONE, 3),
                new ItemStack(Items.GRAVEL, 2),
                new ItemStack(Items.SAND, 2),
                new ItemStack(Items.BONE, 3),
                new ItemStack(Items.BAMBOO, 2),
                new ItemStack(Items.STRING, 2),
                new ItemStack(Items.ROTTEN_FLESH, 1),
                new ItemStack(Items.IRON_NUGGET, 4),
                new ItemStack(Items.IRON_INGOT, 1),
                new ItemStack(Items.GOLD_INGOT, 1),
                new ItemStack(Items.REDSTONE, 3),
                new ItemStack(Items.LAPIS_LAZULI, 2),
                new ItemStack(Items.COAL, 2),
                new ItemStack(Items.AMETHYST_SHARD, 2),
                new ItemStack(Items.EXPERIENCE_BOTTLE, 2),
                new ItemStack(Items.DIAMOND, 1),
                new ItemStack(Items.EMERALD, 1),
                new ItemStack(Items.GOLDEN_APPLE, 1),
                new ItemStack(Items.ANCIENT_DEBRIS, 1),
                new ItemStack(Items.NETHERITE_SCRAP, 1),
                new ItemStack(Items.SWEET_BERRIES, 3)
            );
        }
        return rainItems;
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, selection) ->
            FarmCommand.register(dispatcher, access));

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            var registryAccess = server.registryAccess();

            var dimTypeRegistry = registryAccess.lookupOrThrow(Registries.DIMENSION_TYPE);
            unfreeze(dimTypeRegistry);
            var dimTypeHolder = Registry.registerForHolder(dimTypeRegistry, FarmDimension.FARM_DIMENSION_TYPE_KEY, FarmDimension.createDimensionType());
            freeze(dimTypeRegistry);

            var stemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
            var biomeGetter = registryAccess.lookupOrThrow(Registries.BIOME);
            var structureGetter = registryAccess.lookupOrThrow(Registries.STRUCTURE_SET);
            var featureGetter = registryAccess.lookupOrThrow(Registries.PLACED_FEATURE);

            var stemKey = Registries.levelToLevelStem(FarmDimension.FARM_WORLD_KEY);
            unfreeze(stemRegistry);
            Registry.register(stemRegistry, stemKey,
                FarmDimension.createLevelStem(dimTypeHolder, biomeGetter, structureGetter, featureGetter));
            freeze(stemRegistry);

            LOGGER.info("Registered Farm dimension type and level stem");
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerLevel farmLevel = server.getLevel(FarmDimension.FARM_WORLD_KEY);
            if (farmLevel != null) {
                ensurePlatform(farmLevel);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long tick = server.getTickCount();
            ServerLevel farmLevel = server.getLevel(FarmDimension.FARM_WORLD_KEY);
            if (farmLevel == null) return;

            FarmWeatherData weather = FarmWeatherData.get(server);

            if (tick % RAIN_INTERVAL == 0 && weather.isRaining()) {
                tickRain(farmLevel);
            }

            if (weather.isRaining() && tick >= weather.getRainEndTick()) {
                weather.stopRain();
                if (weather.isNaturalWeatherEnabled()) {
                    weather.scheduleNextNaturalRain(tick);
                }
            }

            if (weather.isNaturalWeatherEnabled() && !weather.isRaining() && tick >= weather.getNextNaturalRainTick()) {
                weather.startRain(tick, RAIN_DURATION_TICKS);
            }

            if (tick % SPAWN_INTERVAL == 0) {
                tickMobSpawn(server, farmLevel);
            }
        });

        LOGGER.info("Farmrealm v1.0.0 initialized");
    }

    private static void tickRain(ServerLevel farmLevel) {
        for (ServerPlayer player : farmLevel.players()) {
            if (player.level() != farmLevel) continue;
            int x = player.getBlockX() + RANDOM.nextInt(21) - 10;
            int z = player.getBlockZ() + RANDOM.nextInt(21) - 10;
            int y = RAIN_HEIGHT + RANDOM.nextInt(11);

            var items = getRainItems();
            ItemStack stack = items.get(RANDOM.nextInt(items.size())).copy();
            stack.setCount(1 + RANDOM.nextInt(stack.getMaxStackSize()));

            ItemEntity item = new ItemEntity(farmLevel, x + 0.5, y, z + 0.5, stack);
            farmLevel.addFreshEntity(item);
        }
    }

    private static void tickMobSpawn(MinecraftServer server, ServerLevel farmLevel) {
        SpawnAreaData spawnData = SpawnAreaData.get(server);
        if (!spawnData.isSpawnEnabled()) return;

        for (SpawnAreaData.SpawnAreaEntry area : spawnData.getAreas().values()) {
            if (!area.enabled) continue;
            int spawnCount = area.rate * SPAWN_INTERVAL / 1200;
            for (int i = 0; i < spawnCount; i++) {
                BlockPos pos = area.getRandomPos(RANDOM);
                spawnMob(farmLevel, area.entityTypeId, pos);
            }
        }
    }

    private static void spawnMob(ServerLevel level, String entityTypeId, BlockPos pos) {
        Identifier id = Identifier.parse(entityTypeId);
        var registry = level.registryAccess().lookupOrThrow(Registries.ENTITY_TYPE);
        registry.get(id).ifPresent(holder -> {
            holder.value().spawn(level, pos, EntitySpawnReason.COMMAND);
        });
    }

    private static void unfreeze(Registry<?> registry) {
        ((MappedRegistryAccessor) registry).setFrozen(false);
    }

    private static void freeze(Registry<?> registry) {
        ((MappedRegistryAccessor) registry).setFrozen(true);
    }

    private static void ensurePlatform(ServerLevel level) {
        level.getChunk(0, 0);

        boolean needsPlatform = true;
        checkLoop:
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (!level.getBlockState(new BlockPos(x, 2, z)).isAir()) {
                    needsPlatform = false;
                    break checkLoop;
                }
            }
        }

        if (needsPlatform) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    level.setBlock(new BlockPos(x, 2, z), Blocks.STONE.defaultBlockState(), 3);
                }
            }
            LOGGER.info("Placed Farm platform at 0,2,0");
        }
    }
}
