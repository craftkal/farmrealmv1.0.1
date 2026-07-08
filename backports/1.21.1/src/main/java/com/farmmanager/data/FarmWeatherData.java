package com.farmmanager.data;

import com.farmmanager.FarmManagerMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class FarmWeatherData extends SavedData {
    private static final String DATA_KEY = "farmmanager_farm_config";

    private boolean raining;
    private long rainEndTick;
    private long nextNaturalRainTick;
    private boolean naturalWeatherEnabled = true;
    private int spawnX = 0, spawnY = 3, spawnZ = 0;

    public FarmWeatherData() {
        scheduleNextNaturalRain(0);
    }

    public static FarmWeatherData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(FarmWeatherData::new, FarmWeatherData::load, DataFixTypes.LEVEL),
            DATA_KEY
        );
    }

    public static FarmWeatherData load(CompoundTag tag, HolderLookup.Provider registries) {
        FarmWeatherData data = new FarmWeatherData();
        data.raining = tag.getBoolean("raining");
        data.rainEndTick = tag.getLong("rainEndTick");
        data.nextNaturalRainTick = tag.getLong("nextNaturalRainTick");
        data.naturalWeatherEnabled = tag.getBoolean("naturalWeatherEnabled");
        data.spawnX = tag.getInt("spawnX");
        data.spawnY = tag.getInt("spawnY");
        data.spawnZ = tag.getInt("spawnZ");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("raining", raining);
        tag.putLong("rainEndTick", rainEndTick);
        tag.putLong("nextNaturalRainTick", nextNaturalRainTick);
        tag.putBoolean("naturalWeatherEnabled", naturalWeatherEnabled);
        tag.putInt("spawnX", spawnX);
        tag.putInt("spawnY", spawnY);
        tag.putInt("spawnZ", spawnZ);
        return tag;
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
