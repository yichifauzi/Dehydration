package net.dehydration.item.storage;

import net.dehydration.block.entity.BambooPumpEntity;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.item.ItemStack;

public class BambooPumpStorage extends SingleStackStorage {

    private final BambooPumpEntity pumpEntity;

    public BambooPumpStorage(BambooPumpEntity pumpEntity) {
        this.pumpEntity = pumpEntity;
    }

    @Override
    protected ItemStack getStack() {
        return pumpEntity.getStack(0);
    }

    @Override
    protected void setStack(ItemStack stack) {
        pumpEntity.setStack(0, stack);
    }
}
