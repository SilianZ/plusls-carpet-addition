package com.plusls.carpet.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//#if MC > 12004
//$$ import net.minecraft.core.HolderLookup;
//#endif

public class ItemUtil {
    //#if MC > 11502
    @Nullable
    public static ItemStack upGradeToNetheriteLike(@NotNull ItemStack Silian_stack) {
        Item Silian_newItem;

        if (Items.DIAMOND_SWORD.equals(Silian_stack.getItem())) {
            Silian_newItem = Items.NETHERITE_SWORD;
        } else if (Items.DIAMOND_SHOVEL.equals(Silian_stack.getItem())) {
            Silian_newItem = Items.NETHERITE_SHOVEL;
        } else if (Items.DIAMOND_PICKAXE.equals(Silian_stack.getItem())) {
            Silian_newItem = Items.NETHERITE_PICKAXE;
        } else if (Items.DIAMOND_AXE.equals(Silian_stack.getItem())) {
            Silian_newItem = Items.NETHERITE_AXE;
        } else if (Items.DIAMOND_HOE.equals(Silian_stack.getItem())) {
            Silian_newItem = Items.NETHERITE_HOE;
        } else if (Items.DIAMOND_HELMET.equals(Silian_stack.getItem())) {
            Silian_newItem = Items.NETHERITE_HELMET;
        } else if (Items.DIAMOND_CHESTPLATE.equals(Silian_stack.getItem())) {
            Silian_newItem = Items.NETHERITE_CHESTPLATE;
        } else if (Items.DIAMOND_LEGGINGS.equals(Silian_stack.getItem())) {
            Silian_newItem = Items.NETHERITE_LEGGINGS;
        } else if (Items.DIAMOND_BOOTS.equals(Silian_stack.getItem())) {
            Silian_newItem = Items.NETHERITE_BOOTS;
        } else {
            Silian_newItem = null;
        }

        if (Silian_newItem == null) {
            return null;
        }

        ItemStack Silian_ret = new ItemStack(Silian_newItem);

        //#if MC > 12004
        //$$ Silian_ret.applyComponents(Silian_stack.getComponents());
        //$$ Silian_ret.setDamageValue(Silian_ret.getMaxDamage() - 1);
        //#else
        CompoundTag Silian_compoundTag = Silian_stack.getTag();

        if (Silian_compoundTag != null) {
            Silian_ret.setTag(Silian_compoundTag.copy());
            Silian_ret.setDamageValue(Silian_ret.getMaxDamage() - 1);
        }
        //#endif

        return Silian_ret;
    }
    //#endif
}
