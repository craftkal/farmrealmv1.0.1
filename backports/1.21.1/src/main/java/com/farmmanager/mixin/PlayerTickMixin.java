package com.farmmanager.mixin;

import com.farmmanager.dimension.FarmDimension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class PlayerTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        Level level = player.level();

        if (level.dimension().equals(FarmDimension.FARM_WORLD_KEY)) {
            if (player.getY() <= 1.0) {
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 0, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0, false, false, true));
            }
        }
    }
}
