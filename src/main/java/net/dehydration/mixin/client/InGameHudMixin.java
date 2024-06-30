package net.dehydration.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.hud.InGameHud;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @ModifyReturnValue(method = "getHeartRows", at = @At("RETURN"))
    private int getHeartRowsMixin(int original) {
        return original + 1;
    }

}