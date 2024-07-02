package net.dehydration.item;

import net.minecraft.item.*;

import net.dehydration.init.FluidInit;

public class PurifiedBucket extends BucketItem {
    public PurifiedBucket(Settings settings) {
        super(FluidInit.PURIFIED_WATER, settings);
    }
}
