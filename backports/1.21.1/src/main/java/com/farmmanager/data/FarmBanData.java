package com.farmmanager.data;

import com.farmmanager.FarmManagerMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class FarmBanData extends SavedData {
    private static final String DATA_KEY = "farmmanager_farm_bans";

    private final Map<String, String> bans = new LinkedHashMap<>();

    public FarmBanData() {
    }

    public static FarmBanData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(FarmBanData::new, FarmBanData::load, DataFixTypes.LEVEL),
            DATA_KEY
        );
    }

    public static FarmBanData load(CompoundTag tag, HolderLookup.Provider registries) {
        FarmBanData data = new FarmBanData();
        ListTag list = tag.getList("bans", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            String uuid = entry.getString("uuid");
            String name = entry.getString("name");
            data.bans.put(uuid, name);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (var entry : bans.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("uuid", entry.getKey());
            entryTag.putString("name", entry.getValue());
            list.add(entryTag);
        }
        tag.put("bans", list);
        return tag;
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
