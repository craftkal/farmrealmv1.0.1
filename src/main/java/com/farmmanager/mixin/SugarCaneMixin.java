package com.farmmanager.mixin;

import com.farmmanager.data.FarmGrowthData;
import com.farmmanager.dimension.FarmDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SugarCaneBlock.class)
public class SugarCaneMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!level.dimension().equals(FarmDimension.FARM_WORLD_KEY)) return;

        int multiplier = FarmGrowthData.get(level.getServer()).getMultiplier();
        if (multiplier <= 0) return;

        ci.cancel();

        SugarCaneBlock block = (SugarCaneBlock)(Object)this;

        if (!level.isEmptyBlock(pos.above())) return;

        int height = 1;
        while (level.getBlockState(pos.below(height)).is(block)) {
            height++;
        }
        if (height >= 3) return;

        int age = state.getValue(SugarCaneBlock.AGE);
        int newAge = age + multiplier;

        if (newAge >= 15) {
            level.setBlockAndUpdate(pos.above(), block.defaultBlockState());
            level.setBlock(pos, state.setValue(SugarCaneBlock.AGE, newAge - 15), 260);
        } else {
            level.setBlock(pos, state.setValue(SugarCaneBlock.AGE, newAge), 260);
        }
    }
}
