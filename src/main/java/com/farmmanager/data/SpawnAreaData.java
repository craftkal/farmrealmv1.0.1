package com.farmmanager.data;

import com.farmmanager.FarmManagerMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

public class SpawnAreaData extends SavedData {
    private static final Identifier DATA_ID = Identifier.fromNamespaceAndPath(FarmManagerMod.MOD_ID, "spawn_areas");

    private static final Codec<SpawnAreaEntry> ENTRY_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("minX").forGetter(e -> e.minX),
            Codec.INT.fieldOf("minY").forGetter(e -> e.minY),
            Codec.INT.fieldOf("minZ").forGetter(e -> e.minZ),
            Codec.INT.fieldOf("maxX").forGetter(e -> e.maxX),
            Codec.INT.fieldOf("maxY").forGetter(e -> e.maxY),
            Codec.INT.fieldOf("maxZ").forGetter(e -> e.maxZ),
            Codec.STRING.fieldOf("entityType").forGetter(e -> e.entityTypeId),
            Codec.INT.fieldOf("rate").forGetter(e -> e.rate),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(e -> e.enabled),
            Codec.STRING.optionalFieldOf("owner", "").forGetter(e -> e.owner)
        ).apply(instance, SpawnAreaEntry::new)
    );

    private static final Codec<Map<String, SpawnAreaEntry>> AREAS_CODEC =
        Codec.unboundedMap(Codec.STRING, ENTRY_CODEC);

    public static final Codec<SpawnAreaData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.BOOL.fieldOf("spawnEnabled").forGetter(data -> data.spawnEnabled),
            Codec.BOOL.optionalFieldOf("spawnCommandsEnabled", true).forGetter(data -> data.spawnCommandsEnabled),
            AREAS_CODEC.fieldOf("areas").forGetter(data -> data.areas)
        ).apply(instance, SpawnAreaData::new)
    );

    public static final SavedDataType<SpawnAreaData> TYPE = new SavedDataType<>(
        DATA_ID,
        SpawnAreaData::new,
        CODEC,
        DataFixTypes.LEVEL
    );

    private boolean spawnEnabled = true;
    private boolean spawnCommandsEnabled = true;
    private final Map<String, SpawnAreaEntry> areas = new LinkedHashMap<>();

    public SpawnAreaData() {
    }

    public SpawnAreaData(boolean spawnEnabled, boolean spawnCommandsEnabled, Map<String, SpawnAreaEntry> areas) {
        this.spawnEnabled = spawnEnabled;
        this.spawnCommandsEnabled = spawnCommandsEnabled;
        this.areas.putAll(areas);
    }

    public static SpawnAreaData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
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
