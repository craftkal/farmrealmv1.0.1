package com.farmmanager.data;

import com.farmmanager.FarmManagerMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class FarmWeatherData extends SavedData {
    private static final Identifier DATA_ID = Identifier.fromNamespaceAndPath(FarmManagerMod.MOD_ID, "farm_config");

    private static final Codec<FarmWeatherData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.BOOL.fieldOf("raining").forGetter(d -> d.raining),
            Codec.LONG.fieldOf("rainEndTick").forGetter(d -> d.rainEndTick),
            Codec.LONG.fieldOf("nextNaturalRainTick").forGetter(d -> d.nextNaturalRainTick),
            Codec.BOOL.fieldOf("naturalWeatherEnabled").forGetter(d -> d.naturalWeatherEnabled),
            Codec.INT.fieldOf("spawnX").forGetter(d -> d.spawnX),
            Codec.INT.fieldOf("spawnY").forGetter(d -> d.spawnY),
            Codec.INT.fieldOf("spawnZ").forGetter(d -> d.spawnZ)
        ).apply(instance, FarmWeatherData::new)
    );

    public static final SavedDataType<FarmWeatherData> TYPE = new SavedDataType<>(
        DATA_ID.toString(),
        FarmWeatherData::new,
        CODEC,
        DataFixTypes.LEVEL
    );

    private boolean raining;
    private long rainEndTick;
    private long nextNaturalRainTick;
    private boolean naturalWeatherEnabled = true;
    private int spawnX = 0, spawnY = 3, spawnZ = 0;

    public FarmWeatherData() {
        scheduleNextNaturalRain(0);
    }

    public FarmWeatherData(boolean raining, long rainEndTick, long nextNaturalRainTick, boolean naturalWeatherEnabled, int spawnX, int spawnY, int spawnZ) {
        this.raining = raining;
        this.rainEndTick = rainEndTick;
        this.nextNaturalRainTick = nextNaturalRainTick;
        this.naturalWeatherEnabled = naturalWeatherEnabled;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
    }

    public static FarmWeatherData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isRaining() {
        return raining;
    }

    public long getRainEndTick() {
        return rainEndTick;
    }

    public long getNextNaturalRainTick() {
        return nextNaturalRainTick;
    }

    public void startRain(long currentTick, int durationTicks) {
        this.raining = true;
        this.rainEndTick = currentTick + durationTicks;
        setDirty();
    }

    public void stopRain() {
        this.raining = false;
        this.rainEndTick = 0;
        setDirty();
    }

    public void scheduleNextNaturalRain(long currentTick) {
        int days = 2 + FarmManagerMod.RANDOM.nextInt(3);
        this.nextNaturalRainTick = currentTick + days * 24000L;
        setDirty();
    }

    public boolean isNaturalWeatherEnabled() {
        return naturalWeatherEnabled;
    }

    public void setNaturalWeatherEnabled(boolean enabled) {
        this.naturalWeatherEnabled = enabled;
        setDirty();
    }

    public BlockPos getSpawnPos() {
        return new BlockPos(spawnX, spawnY, spawnZ);
    }

    public void setSpawnPos(BlockPos pos) {
        this.spawnX = pos.getX();
        this.spawnY = pos.getY();
        this.spawnZ = pos.getZ();
        setDirty();
    }

    public void resetSpawnPos() {
        this.spawnX = 0;
        this.spawnY = 3;
        this.spawnZ = 0;
        setDirty();
    }
}
