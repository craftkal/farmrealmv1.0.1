package com.farmmanager.mixin;

import com.farmmanager.data.FarmGrowthData;
import com.farmmanager.dimension.FarmDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public class CropBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!level.dimension().equals(FarmDimension.FARM_WORLD_KEY)) return;

        int multiplier = FarmGrowthData.get(level.getServer()).getMultiplier();
        if (multiplier <= 0) return;

        ci.cancel();

        CropBlock block = (CropBlock)(Object)this;
        int age = block.getAge(state);
        int maxAge = block.getMaxAge();

        if (age < maxAge) {
            int newAge = Math.min(age + multiplier, maxAge);
            level.setBlock(pos, block.getStateForAge(newAge), 2);
        }
    }
}
