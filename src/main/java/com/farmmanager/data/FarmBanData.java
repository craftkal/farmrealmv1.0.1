package com.farmmanager.data;

import com.farmmanager.FarmManagerMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class FarmBanData extends SavedData {
    private static final Identifier DATA_ID = Identifier.fromNamespaceAndPath(FarmManagerMod.MOD_ID, "farm_bans");

    private static final Codec<Map<String, String>> BANS_CODEC =
        Codec.unboundedMap(Codec.STRING, Codec.STRING);

    public static final Codec<FarmBanData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            BANS_CODEC.fieldOf("bans").forGetter(data -> data.bans)
        ).apply(instance, FarmBanData::new)
    );

    public static final SavedDataType<FarmBanData> TYPE = new SavedDataType<>(
        DATA_ID,
        FarmBanData::new,
        CODEC,
        DataFixTypes.LEVEL
    );

    private final Map<String, String> bans = new LinkedHashMap<>();

    public FarmBanData() {
    }

    public FarmBanData(Map<String, String> bans) {
        this.bans.putAll(bans);
    }

    public static FarmBanData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean ban(UUID uuid, String name) {
        String key = uuid.toString();
        if (bans.containsKey(key)) {
            return false;
        }
        bans.put(key, name);
        setDirty();
        return true;
    }

    public boolean unban(String nameOrUuid) {
        String lower = nameOrUuid.toLowerCase();
        String foundKey = null;
        for (var entry : bans.entrySet()) {
            if (entry.getKey().equals(nameOrUuid) || entry.getValue().equalsIgnoreCase(nameOrUuid)) {
                foundKey = entry.getKey();
                break;
            }
        }
        if (foundKey == null) {
            return false;
        }
        bans.remove(foundKey);
        setDirty();
        return true;
    }

    public boolean isBanned(UUID uuid) {
        return bans.containsKey(uuid.toString());
    }

    public Map<String, String> getBans() {
        return Map.copyOf(bans);
    }

    public int getBanCount() {
        return bans.size();
    }
}
