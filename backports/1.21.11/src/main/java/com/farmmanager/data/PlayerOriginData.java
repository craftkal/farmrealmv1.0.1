package com.farmmanager.data;

import com.farmmanager.FarmManagerMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerOriginData extends SavedData {
    private static final Identifier DATA_ID = Identifier.fromNamespaceAndPath(FarmManagerMod.MOD_ID, "player_origins");

    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    private static final Codec<OriginEntry> ORIGIN_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("dimension").forGetter(e -> e.dimension),
            Codec.DOUBLE.fieldOf("x").forGetter(e -> e.x),
            Codec.DOUBLE.fieldOf("y").forGetter(e -> e.y),
            Codec.DOUBLE.fieldOf("z").forGetter(e -> e.z),
            Codec.FLOAT.fieldOf("yaw").forGetter(e -> e.yaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(e -> e.pitch)
        ).apply(instance, OriginEntry::new)
    );

    private static final Codec<Map<UUID, OriginEntry>> ORIGINS_CODEC =
        Codec.unboundedMap(UUID_CODEC, ORIGIN_ENTRY_CODEC);

    public static final Codec<PlayerOriginData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ORIGINS_CODEC.fieldOf("origins").forGetter(data -> data.origins)
        ).apply(instance, PlayerOriginData::new)
    );

    public static final SavedDataType<PlayerOriginData> TYPE = new SavedDataType<>(
        DATA_ID.toString(),
        PlayerOriginData::new,
        CODEC,
        DataFixTypes.LEVEL
    );

    private final Map<UUID, OriginEntry> origins = new HashMap<>();

    public PlayerOriginData() {
    }

    public PlayerOriginData(Map<UUID, OriginEntry> origins) {
        this.origins.putAll(origins);
    }

    public static PlayerOriginData get(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(TYPE);
    }

    public static PlayerOriginData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public void saveOrigin(ServerPlayer player) {
        OriginEntry entry = new OriginEntry(
            player.level().dimension().identifier().toString(),
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
            ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimension));
            ServerLevel world = server.getLevel(worldKey);
            if (world == null) {
                world = server.overworld();
            }
            return world;
        }
    }
}
