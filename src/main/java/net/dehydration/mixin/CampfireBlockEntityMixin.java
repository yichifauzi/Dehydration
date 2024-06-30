package net.dehydration.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.dehydration.init.ItemInit;
import net.dehydration.mixin.accessor.CampfireBlockEntityAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {

    @Inject(method = "litServerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ItemScatterer;spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void litServerTickMixin(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo info, boolean bl, int i, ItemStack itemStack,
            SingleStackRecipeInput singleStackRecipeInput, ItemStack itemStack2) {
        if (itemStack2.getItem() instanceof PotionItem && itemStack2.get(DataComponentTypes.POTION_CONTENTS).matches(Potions.WATER)) {
            ItemScatterer.spawn(world, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), PotionContentsComponent.createStack(Items.POTION, ItemInit.PURIFIED_WATER));
            ((CampfireBlockEntityAccessor) campfire).getItemsBeingCooked().set(i, ItemStack.EMPTY);
            world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
            info.cancel();
        }
    }
}
