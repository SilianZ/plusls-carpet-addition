package com.plusls.carpet.mixin.rule.avoidAnvilTooExpensive;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

//#if MC > 12004
//$$ import net.minecraft.core.component.DataComponents;
//#endif

//#if MC > 11502
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
//#else
//$$ import net.minecraft.world.Container;
//$$ import org.spongepowered.asm.mixin.Final;
//#endif

@Mixin(AnvilMenu.class)
public abstract class MixinAnvilMenu
        //#if MC > 11502
        extends ItemCombinerMenu
        //#endif
{
    private MixinAnvilMenu(
            //#if MC > 11502
            @Nullable MenuType<?> Silian_type,
            int Silian_containerId,
            Inventory Silian_playerInventory,
            ContainerLevelAccess Silian_access
            //#endif
    ) {
        super(
                //#if MC > 11502
                Silian_type,
                Silian_containerId,
                Silian_playerInventory,
                Silian_access
                //#endif
        );
    }

    @Shadow
    private String itemName;

    //#if MC < 11600
    //$$ @Shadow
    //$$ @Final
    //$$ private Container repairSlots;
    //#endif

    @ModifyVariable(
            method = "createResult",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/inventory/DataSlot;get()I",
                            ordinal = 1
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
                    ordinal = 0
            ),
            ordinal = 1
    )
    private ItemStack setItemStack(ItemStack Silian_itemStack) {
        //#if MC > 11502
        ItemStack Silian_itemStackA = this.inputSlots.getItem(0);
        ItemStack Silian_itemStackB = this.inputSlots.getItem(1);
        //#else
        //$$ ItemStack Silian_itemStackA = this.repairSlots.getItem(0);
        //$$ ItemStack Silian_itemStackB = this.repairSlots.getItem(1);
        //#endif

        if (PluslsCarpetAdditionSettings.avoidAnvilTooExpensive && Silian_itemStack.isEmpty() && !Silian_itemStackA.isEmpty() &&
                (!Silian_itemStackB.isEmpty() ||
                        (StringUtils.isBlank(this.itemName) &&
                                //#if MC > 12004
                                //$$ Silian_itemStackA.has(DataComponents.CUSTOM_NAME)
                                //#else
                                Silian_itemStackA.hasCustomHoverName()
                                //#endif
                        ) ||
                        (!StringUtils.isBlank(this.itemName) && !this.itemName.equals(Silian_itemStackA.getHoverName().getString())))
        ) {
            return Silian_itemStackA.copy();
        } else {
            return Silian_itemStack;
        }
    }
}
