package com.farmmanager.data;

import com.farmmanager.FarmManagerMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerOriginData extends SavedData {
    private static final String DATA_KEY = "farmmanager_player_origins";

    private final Map<UUID, OriginEntry> origins = new HashMap<>();

    public PlayerOriginData() {
    }

    public static PlayerOriginData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(PlayerOriginData::new, PlayerOriginData::load, DataFixTypes.LEVEL),
            DATA_KEY
        );
    }

    public static PlayerOriginData load(CompoundTag tag, HolderLookup.Provider registries) {
        PlayerOriginData data = new PlayerOriginData();
        ListTag list = tag.getList("origins", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID uuid = entry.getUUID("uuid");
            String dimension = entry.getString("dimension");
            double x = entry.getDouble("x");
            double y = entry.getDouble("y");
            double z = entry.getDouble("z");
            float yaw = entry.getFloat("yaw");
            float pitch = entry.getFloat("pitch");
            data.origins.put(uuid, new OriginEntry(dimension, x, y, z, yaw, pitch));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, OriginEntry> entry : origins.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("uuid", entry.getKey());
            entryTag.putString("dimension", entry.getValue().dimension);
            entryTag.putDouble("x", entry.getValue().x);
            entryTag.putDouble("y", entry.getValue().y);
            entryTag.putDouble("z", entry.getValue().z);
            entryTag.putFloat("yaw", entry.getValue().yaw);
            entryTag.putFloat("pitch", entry.getValue().pitch);
            list.add(entryTag);
        }
        tag.put("origins", list);
        return tag;
    }

    public void saveOrigin(ServerPlayer player) {
        OriginEntry entry = new OriginEntry(
            ((ServerLevel)player.level()).dimension().location().toString(),
            player.getX(), player.getY(), player.getZ(),
            player.getYRot(), player.getXRot()
        );
        origins.put(player.getUUID(), entry);
        setDirty();
    }

    public OriginEntry getOrigin(UUID playerUuid) {
        return origins.get(playerUuid);
    }

    public void removeOrigin(UUID playerUuid) {
        origins.remove(playerUuid);
        setDirty();
    }

    public static class OriginEntry {
        public final String dimension;
        public final double x;
        public final double y;
        public final double z;
        public final float yaw;
        public final float pitch;

        public OriginEntry(String dimension, double x, double y, double z, float yaw, float pitch) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public ServerLevel getWorld(MinecraftServer server) {
            ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension));
            ServerLevel world = server.getLevel(worldKey);
            if (world == null) {
                world = server.overworld();
            }
            return world;
        }
    }
}
