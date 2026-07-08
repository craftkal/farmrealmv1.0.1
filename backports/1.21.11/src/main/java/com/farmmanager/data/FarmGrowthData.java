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

public class FarmGrowthData extends SavedData {
    private static final Identifier DATA_ID = Identifier.fromNamespaceAndPath(FarmManagerMod.MOD_ID, "farm_growth");

    public static final Codec<FarmGrowthData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.optionalFieldOf("multiplier", 1).forGetter(d -> d.multiplier)
        ).apply(instance, FarmGrowthData::new)
    );

    public static final SavedDataType<FarmGrowthData> TYPE = new SavedDataType<>(
        DATA_ID.toString(),
        FarmGrowthData::new,
        CODEC,
        DataFixTypes.LEVEL
    );

    private int multiplier = 4;

    public FarmGrowthData() {
    }

    public FarmGrowthData(int multiplier) {
        this.multiplier = multiplier;
    }

    public static FarmGrowthData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
        setDirty();
    }
}
