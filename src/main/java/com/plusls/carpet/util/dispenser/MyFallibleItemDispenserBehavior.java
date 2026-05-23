package com.plusls.carpet.util.dispenser;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

//#if MC > 12001
//$$ import net.minecraft.core.dispenser.BlockSource;
//#else
import net.minecraft.core.BlockSource;
//#endif

public abstract class MyFallibleItemDispenserBehavior extends MyDispenserBehavior {
    private boolean success = false;

    public MyFallibleItemDispenserBehavior(DispenseItemBehavior Silian_oldDispenserBehavior) {
        super(Silian_oldDispenserBehavior);
    }

    @Override
    public final ItemStack dispense(BlockSource Silian_blockPointer, ItemStack Silian_itemStack) {
        setSuccess(false);
        ItemStack Silian_itemStack2 = this.dispenseSilently(Silian_blockPointer, Silian_itemStack);
        if (!isSuccess()) {
            return super.dispense(Silian_blockPointer, Silian_itemStack);
        }
        this.playSound(Silian_blockPointer);
        this.spawnParticles(Silian_blockPointer, Silian_blockPointer.getBlockState().getValue(DispenserBlock.FACING));
        return Silian_itemStack2;
    }

    public ItemStack dispenseSilently(BlockSource Silian_pointer, ItemStack Silian_stack) {
        Direction Silian_direction = Silian_pointer.getBlockState().getValue(DispenserBlock.FACING);
        Position Silian_position = DispenserBlock.getDispensePosition(Silian_pointer);
        ItemStack Silian_itemStack = Silian_stack.split(1);
        DefaultDispenseItemBehavior.spawnItem(Silian_pointer.getLevel(), Silian_itemStack, 6, Silian_direction, Silian_position);
        return Silian_stack;
    }

    protected void spawnParticles(BlockSource Silian_pointer, Direction Silian_side) {
        Silian_pointer.getLevel().levelEvent(2000, Silian_pointer.getPos(), Silian_side.get3DDataValue());
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean Silian_success) {
        this.success = Silian_success;
    }

    protected void playSound(BlockSource Silian_pointer) {
        Silian_pointer.getLevel().levelEvent(this.isSuccess() ? 1000 : 1001, Silian_pointer.getPos(), 0);
    }
}
