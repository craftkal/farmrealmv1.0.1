package com.farmmanager.dimension;

import com.farmmanager.FarmManagerMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import java.util.List;
import java.util.Optional;

public class FarmDimension {
    public static final ResourceKey<Level> FARM_WORLD_KEY =
        ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(FarmManagerMod.MOD_ID, "farm"));

    public static final ResourceKey<DimensionType> FARM_DIMENSION_TYPE_KEY =
        ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(FarmManagerMod.MOD_ID, "farm_type"));

    public static DimensionType createDimensionType() {
        return new DimensionType(
            true, // hasFixedTime
            false, // hasSkyLight
            false, // hasCeiling
            1.0, // coordinateScale
            0, // minY
            256, // height
            256, // logicalHeight
            BlockTags.INFINIBURN_OVERWORLD,
            1.0f, // ambientLight
            new DimensionType.MonsterSettings(
                ConstantInt.ZERO,
                0
            ),
            DimensionType.Skybox.END,
            DimensionType.CardinalLightType.DEFAULT,
            EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.SKY_COLOR, 0)
                .set(EnvironmentAttributes.FOG_COLOR, 0)
                .set(EnvironmentAttributes.BED_RULE, net.minecraft.world.attribute.BedRule.CAN_SLEEP_WHEN_DARK)
                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, true)
                .build(),
            HolderSet.empty()
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
            Optional.empty(),
            biome,
            List.of()
        );
        return new LevelStem(dimensionType, new FlatLevelSource(settings));
    }
}
