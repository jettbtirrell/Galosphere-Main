package net.orcinus.galosphere.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.orcinus.galosphere.entities.Sparkle;

public class PickaxeDispenseItemBehavior extends OptionalDispenseItemBehavior {

    @Override
    protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        ServerLevel level = blockSource.level();
        if (!level.isClientSide()) {
            BlockPos blockpos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
            this.setSuccess(extractItemFromEntity(level, blockpos, itemStack));
            if (this.isSuccess()) {
                itemStack.hurtAndBreak(1, level, null, item -> {});
            }
        }
        return itemStack;
    }

    private static boolean extractItemFromEntity(ServerLevel world, BlockPos blockPos, ItemStack stack) {
        for (Sparkle livingentity : world.getEntitiesOfClass(Sparkle.class, new AABB(blockPos), EntitySelector.NO_SPECTATORS)) {
            if (livingentity != null) {
                if (livingentity.hasCrystal()) {
                    livingentity.extractShard(stack);
                    world.gameEvent(null, GameEvent.SHEAR, blockPos);
                    return true;
                }
            }
        }

        return false;
    }

}
