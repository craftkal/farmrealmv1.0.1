package com.farmmanager.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class SpawnAreaData extends SavedData {
    private static final String DATA_KEY = "farmmanager_spawn_areas";

    private boolean spawnEnabled = true;
    private boolean spawnCommandsEnabled = true;
    private final Map<String, SpawnAreaEntry> areas = new LinkedHashMap<>();

    public SpawnAreaData() {
    }

    public static SpawnAreaData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(SpawnAreaData::new, SpawnAreaData::load, DataFixTypes.LEVEL),
            DATA_KEY
        );
    }

    public static SpawnAreaData load(CompoundTag tag, HolderLookup.Provider registries) {
        SpawnAreaData data = new SpawnAreaData();
        data.spawnEnabled = tag.getBoolean("spawnEnabled");
        data.spawnCommandsEnabled = tag.contains("spawnCommandsEnabled") ? tag.getBoolean("spawnCommandsEnabled") : true;
        CompoundTag areasTag = tag.getCompound("areas");
        for (String key : areasTag.getAllKeys()) {
            CompoundTag entryTag = areasTag.getCompound(key);
            int minX = entryTag.getInt("minX");
            int minY = entryTag.getInt("minY");
            int minZ = entryTag.getInt("minZ");
            int maxX = entryTag.getInt("maxX");
            int maxY = entryTag.getInt("maxY");
            int maxZ = entryTag.getInt("maxZ");
            String entityType = entryTag.getString("entityType");
            int rate = entryTag.getInt("rate");
            boolean enabled = entryTag.contains("enabled") ? entryTag.getBoolean("enabled") : true;
            String owner = entryTag.contains("owner") ? entryTag.getString("owner") : "";
            data.areas.put(key, new SpawnAreaEntry(minX, minY, minZ, maxX, maxY, maxZ, entityType, rate, enabled, owner));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("spawnEnabled", spawnEnabled);
        tag.putBoolean("spawnCommandsEnabled", spawnCommandsEnabled);
        CompoundTag areasTag = new CompoundTag();
        for (Map.Entry<String, SpawnAreaEntry> entry : areas.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            SpawnAreaEntry area = entry.getValue();
            entryTag.putInt("minX", area.minX);
            entryTag.putInt("minY", area.minY);
            entryTag.putInt("minZ", area.minZ);
            entryTag.putInt("maxX", area.maxX);
            entryTag.putInt("maxY", area.maxY);
            entryTag.putInt("maxZ", area.maxZ);
            entryTag.putString("entityType", area.entityTypeId);
            entryTag.putInt("rate", area.rate);
            entryTag.putBoolean("enabled", area.enabled);
            entryTag.putString("owner", area.owner);
            areasTag.put(entry.getKey(), entryTag);
        }
        tag.put("areas", areasTag);
        return tag;
    }

    public boolean isSpawnEnabled() {
        return spawnEnabled;
    }

    public void setSpawnEnabled(boolean enabled) {
        this.spawnEnabled = enabled;
        setDirty();
    }

    public boolean isSpawnCommandsEnabled() {
        return spawnCommandsEnabled;
    }

    public void setSpawnCommandsEnabled(boolean enabled) {
        this.spawnCommandsEnabled = enabled;
        setDirty();
    }

    public boolean addArea(String name, BlockPos pos1, BlockPos pos2, String entityTypeId, int rate, String owner) {
        if (areas.containsKey(name)) {
            return false;
        }
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        areas.put(name, new SpawnAreaEntry(minX, minY, minZ, maxX, maxY, maxZ, entityTypeId, rate, true, owner));
        setDirty();
        return true;
    }

    public boolean removeArea(String name) {
        SpawnAreaEntry removed = areas.remove(name);
        if (removed != null) {
            setDirty();
            return true;
        }
        return false;
    }

    public boolean setAreaEnabled(String name, boolean enabled) {
        SpawnAreaEntry entry = areas.get(name);
        if (entry == null) return false;
        entry.enabled = enabled;
        setDirty();
        return true;
    }

    public void setAreasEnabledByOwner(String owner, boolean enabled) {
        for (SpawnAreaEntry entry : areas.values()) {
            if (owner.equals(entry.owner)) {
                entry.enabled = enabled;
            }
        }
        setDirty();
    }

    public Map<String, SpawnAreaEntry> getAreas() {
        return Collections.unmodifiableMap(areas);
    }

    public static class SpawnAreaEntry {
        public final int minX, minY, minZ;
        public final int maxX, maxY, maxZ;
        public final String entityTypeId;
        public final int rate;
        public final String owner;
        public boolean enabled;

        public SpawnAreaEntry(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, String entityTypeId, int rate, boolean enabled, String owner) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
            this.entityTypeId = entityTypeId;
            this.rate = rate;
            this.enabled = enabled;
            this.owner = owner;
        }

        public BlockPos getRandomPos(Random random) {
            int x = minX + random.nextInt(maxX - minX + 1);
            int y = minY + random.nextInt(maxY - minY + 1);
            int z = minZ + random.nextInt(maxZ - minZ + 1);
            return new BlockPos(x, y, z);
        }
    }
}
