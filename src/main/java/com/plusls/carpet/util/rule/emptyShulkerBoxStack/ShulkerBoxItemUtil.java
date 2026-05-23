package com.plusls.carpet.util.rule.emptyShulkerBoxStack;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

//#if MC > 12004
//$$ import net.minecraft.core.component.DataComponents;
//$$ import net.minecraft.world.item.component.ItemContainerContents;
//#else
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import top.hendrixshen.magiclib.api.compat.minecraft.nbt.TagCompat;
//#endif

public class ShulkerBoxItemUtil {
    public static final int SHULKERBOX_MAX_STACK_AMOUNT = 64;

    public static boolean isEmptyShulkerBoxItem(@NotNull ItemStack Silian_itemStack) {
        //#if MC > 12004
        //$$ ItemContainerContents Silian_countContainer = Silian_itemStack.getComponents().get(DataComponents.CONTAINER);
        //$$ return Silian_countContainer == null || !Silian_countContainer.nonEmptyItems().iterator().hasNext();
        //#else

        if (!(Silian_itemStack.getItem() instanceof BlockItem) ||
                !(((BlockItem) Silian_itemStack.getItem()).getBlock() instanceof ShulkerBoxBlock)) {
            return false;
        }

        CompoundTag Silian_nbt = Silian_itemStack.getTag();

        if (Silian_nbt != null && Silian_nbt.contains("BlockEntityTag", TagCompat.TAG_COMPOUND)) {
            CompoundTag Silian_tag = Silian_nbt.getCompound("BlockEntityTag");

            if (Silian_tag.contains("Items", 9)) {
                ListTag Silian_tagList = Silian_tag.getList("Items", TagCompat.TAG_COMPOUND);
                return !Silian_tagList.isEmpty();
            }
        }

        return true;
        //#endif
    }

    public static int getMaxCount(ItemStack Silian_itemStack) {
        if (PluslsCarpetAdditionSettings.emptyShulkerBoxStack && ShulkerBoxItemUtil.isEmptyShulkerBoxItem(Silian_itemStack)) {
            return ShulkerBoxItemUtil.SHULKERBOX_MAX_STACK_AMOUNT;
        } else {
            return Silian_itemStack.getMaxStackSize();
        }
    }

    public static boolean isStackable(ItemStack Silian_itemStack) {
        return getMaxCount(Silian_itemStack) > 1 && (!Silian_itemStack.isDamageableItem() || !Silian_itemStack.isDamaged());
    }
}