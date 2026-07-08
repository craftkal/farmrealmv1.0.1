package com.farmmanager.dimension;

import com.farmmanager.FarmManagerMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public class FarmDimension {
    public static final ResourceKey<Level> FARM_WORLD_KEY =
        ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(FarmManagerMod.MOD_ID + ":farm"));

    public static final ResourceKey<DimensionType> FARM_DIMENSION_TYPE_KEY =
        ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.parse(FarmManagerMod.MOD_ID + ":farm_type"));

    private static final ResourceLocation END_EFFECTS = ResourceLocation.parse("minecraft:the_end");

    public static DimensionType createDimensionType() {
        return new DimensionType(
            OptionalLong.of(18000L),
            false,
            false,
            false,
            false,
            1.0,
            false,
            false,
            0,
            256,
            256,
            BlockTags.INFINIBURN_OVERWORLD,
            END_EFFECTS,
            1.0f,
            new DimensionType.MonsterSettings(false, false, ConstantInt.ZERO, 0)
        );
    }

    public static LevelStem createLevelStem(
        Holder<DimensionType> dimensionType,
        HolderGetter<Biome> biomeGetter,
        HolderGetter<StructureSet> structureGetter,
        HolderGetter<PlacedFeature> featureGetter
    ) {
        Holder<Biome> biome = biomeGetter.getOrThrow(Biomes.THE_VOID);
        FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(
            Optional.<HolderSet<StructureSet>>empty(),
            biome,
            List.of()
        );
        return new LevelStem(dimensionType, new FlatLevelSource(settings));
    }
}
