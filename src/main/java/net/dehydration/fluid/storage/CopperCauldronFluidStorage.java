package net.dehydration.fluid.storage;

import com.google.common.primitives.Ints;
import net.dehydration.init.BlockInit;
import net.dehydration.init.FluidInit;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CopperCauldronFluidStorage extends SnapshotParticipant<BlockState> implements SingleSlotStorage<FluidVariant> {

    private final World world;
    private final BlockPos pos;

    private BlockState lastReleasedSnapshot;

    public CopperCauldronFluidStorage(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    // Retrieve the current CauldronFluidContent.
    private CopperCauldronFluidContent getCurrentContent() {
        return new CopperCauldronFluidContent(createSnapshot().getBlock());
    }

    // Called by insert and extract to update the block state.
    private void updateLevel(CopperCauldronFluidContent newContent, int level, TransactionContext transaction) {
        updateSnapshots(transaction);
        BlockState newState = newContent.block.getDefaultState();

        if (newContent.levelProperty != null) {
            newState = newState.with(newContent.levelProperty, level);
        }

        // Set block state without updates.
        world.setBlockState(pos, newState, 0);
    }

    @Override
    public long insert(FluidVariant fluidVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(fluidVariant, maxAmount);

        CopperCauldronFluidContent insertContent = new CopperCauldronFluidContent(fluidVariant.getFluid());

        int maxLevelsInserted = Ints.saturatedCast(maxAmount / insertContent.amountPerLevel);

        if (getAmount() == 0) {
            // Currently empty, so we can accept any fluid.
            int levelsInserted = Math.min(maxLevelsInserted, insertContent.maxLevel);

            if (levelsInserted > 0) {
                updateLevel(insertContent, levelsInserted, transaction);
            }

            return levelsInserted * insertContent.amountPerLevel;
        }

        CopperCauldronFluidContent currentContent = getCurrentContent();

        if (fluidVariant.isOf(currentContent.fluid)) {
            // Otherwise we can only accept the same fluid as the current one.
            int currentLevel = currentContent.currentLevel(createSnapshot());
            int levelsInserted = Math.min(maxLevelsInserted, currentContent.maxLevel - currentLevel);

            if (levelsInserted > 0) {
                updateLevel(currentContent, currentLevel + levelsInserted, transaction);
            }

            return levelsInserted * currentContent.amountPerLevel;
        }

        return 0;
    }

    @Override
    public long extract(FluidVariant fluidVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(fluidVariant, maxAmount);

        CopperCauldronFluidContent currentContent = getCurrentContent();

        if (fluidVariant.isOf(currentContent.fluid)) {
            int maxLevelsExtracted = Ints.saturatedCast(maxAmount / currentContent.amountPerLevel);
            int currentLevel = currentContent.currentLevel(createSnapshot());
            int levelsExtracted = Math.min(maxLevelsExtracted, currentLevel);

            if (levelsExtracted > 0) {
                if (levelsExtracted == currentLevel) {
                    // Fully extract -> back to empty copper cauldron
                    updateSnapshots(transaction);
                    world.setBlockState(pos, BlockInit.COPPER_CAULDRON_BLOCK.getDefaultState(), 0);
                } else {
                    // Otherwise just decrease levels
                    updateLevel(currentContent, currentLevel - levelsExtracted, transaction);
                }
            }

            return levelsExtracted * currentContent.amountPerLevel;
        }

        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return getResource().isBlank();
    }

    @Override
    public FluidVariant getResource() {
        return FluidVariant.of(getCurrentContent().fluid);
    }

    @Override
    public long getAmount() {
        CopperCauldronFluidContent currentContent = getCurrentContent();
        return currentContent.currentLevel(createSnapshot()) * currentContent.amountPerLevel;
    }

    @Override
    public long getCapacity() {
        CopperCauldronFluidContent currentContent = getCurrentContent();
        return currentContent.maxLevel * currentContent.amountPerLevel;
    }

    @Override
    public BlockState createSnapshot() {
        return world.getBlockState(pos);
    }

    @Override
    public void readSnapshot(BlockState savedState) {
        world.setBlockState(pos, savedState, 0);
    }

    @Override
    protected void releaseSnapshot(BlockState snapshot) {
        lastReleasedSnapshot = snapshot;
    }

    @Override
    public void onFinalCommit() {
        BlockState state = createSnapshot();
        BlockState originalState = lastReleasedSnapshot;

        if (originalState != state) {
            // Revert change
            world.setBlockState(pos, originalState, 0);
            // Then do the actual change with normal block updates
            world.setBlockState(pos, state);
        }
    }

    @Override
    public String toString() {
        return "CopperCauldronStorage[" + world + ", " + pos + "]";
    }

    private static class CopperCauldronFluidContent {

        final Block block;
        final Fluid fluid;
        final long amountPerLevel;
        final int maxLevel;
        final IntProperty levelProperty;

        private CopperCauldronFluidContent(Block block) {
            if (block == BlockInit.COPPER_WATER_CAULDRON_BLOCK) {
                this.block = block;
                fluid = Fluids.WATER;
                amountPerLevel = FluidConstants.BOTTLE;
                maxLevel = 3;
                levelProperty = LeveledCauldronBlock.LEVEL;
            } else if (block == BlockInit.COPPER_PURIFIED_WATER_CAULDRON_BLOCK) {
                this.block = block;
                fluid = FluidInit.PURIFIED_WATER;
                amountPerLevel = FluidConstants.BOTTLE;
                maxLevel = 3;
                levelProperty = LeveledCauldronBlock.LEVEL;
            } else {
                this.block = BlockInit.COPPER_CAULDRON_BLOCK;
                fluid = Fluids.EMPTY;
                amountPerLevel = FluidConstants.BUCKET;
                maxLevel = 1;
                levelProperty = null;
            }
        }

        private CopperCauldronFluidContent(Fluid fluid) {
            if (fluid == Fluids.WATER) {
                this.fluid = fluid;
                block = BlockInit.COPPER_WATER_CAULDRON_BLOCK;
                amountPerLevel = FluidConstants.BOTTLE;
                maxLevel = 3;
                levelProperty = LeveledCauldronBlock.LEVEL;
            } else if (fluid == FluidInit.PURIFIED_WATER) {
                this.fluid = fluid;
                block = BlockInit.COPPER_PURIFIED_WATER_CAULDRON_BLOCK;
                amountPerLevel = FluidConstants.BOTTLE;
                maxLevel = 3;
                levelProperty = LeveledCauldronBlock.LEVEL;
            } else {
                this.fluid = Fluids.EMPTY;
                block = BlockInit.COPPER_CAULDRON_BLOCK;
                amountPerLevel = FluidConstants.BUCKET;
                maxLevel = 1;
                levelProperty = null;
            }
        }

        private int currentLevel(BlockState state) {
            if (fluid == Fluids.EMPTY) {
                return 0;
            } else if (levelProperty == null) {
                return 1;
            } else {
                return state.get(levelProperty);
            }
        }

    }
}