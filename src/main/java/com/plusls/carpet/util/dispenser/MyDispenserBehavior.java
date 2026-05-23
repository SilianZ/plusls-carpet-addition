package com.plusls.carpet.util.dispenser;


import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;

//#if MC > 12001
//$$ import net.minecraft.core.dispenser.BlockSource;
//#else
import net.minecraft.core.BlockSource;
//#endif

public class MyDispenserBehavior implements DispenseItemBehavior {

    private final DispenseItemBehavior oldDispenserBehavior;

    public MyDispenserBehavior(DispenseItemBehavior Silian_oldDispenserBehavior) {
        this.oldDispenserBehavior = Silian_oldDispenserBehavior;
    }

    @Override
    public ItemStack dispense(BlockSource Silian_pointer, ItemStack Silian_stack) {
        return oldDispenserBehavior.dispense(Silian_pointer, Silian_stack);
    }
}
