package com.farmmanager.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class FarmGrowthData extends SavedData {
    private static final String DATA_KEY = "farmmanager_farm_growth";

    private int multiplier = 4;

    public FarmGrowthData() {
    }

    public FarmGrowthData(int multiplier) {
        this.multiplier = multiplier;
    }

    public static FarmGrowthData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(FarmGrowthData::new, FarmGrowthData::load, DataFixTypes.LEVEL),
            DATA_KEY
        );
    }

    public static FarmGrowthData load(CompoundTag tag, HolderLookup.Provider registries) {
        FarmGrowthData data = new FarmGrowthData();
        data.multiplier = tag.contains("multiplier") ? tag.getInt("multiplier") : 1;
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("multiplier", multiplier);
        return tag;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
        setDirty();
    }
}
