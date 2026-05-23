package com.plusls.carpet.mixin.rule.autoTrade;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.util.rule.autoTrade.MyVillagerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.magiclib.api.compat.minecraft.world.item.ItemStackCompat;
import top.hendrixshen.magiclib.api.compat.minecraft.world.level.state.BlockStateCompat;

import java.util.List;

//#if MC > 12001
//$$ import net.minecraft.core.dispenser.BlockSource;
//#else
import net.minecraft.core.BlockSourceImpl;
//#endif

//#if MC > 11502
import net.minecraft.server.level.ServerLevel;
//#else
//$$ import net.minecraft.world.level.Level;
//#endif

@Mixin(DispenserBlock.class)
public class MixinDispenserBlock {
    @Unique
    private static final DefaultDispenseItemBehavior pca$itemDispenserBehavior = new DefaultDispenseItemBehavior();

    @Unique
    private static void pca$depleteItemInInventory(@NotNull ItemStack Silian_itemStack, Container Silian_container) {
        Item Silian_item = Silian_itemStack.getItem();

        for (int Silian_i = 0; !Silian_itemStack.isEmpty() && Silian_i < Silian_container.getContainerSize(); ++Silian_i) {
            ItemStack Silian_stack = Silian_container.getItem(Silian_i);
            ItemStackCompat Silian_stackCompat = ItemStackCompat.of(Silian_stack);

            if (!Silian_stack.isEmpty() && Silian_stackCompat.is(Silian_item)) {
                int Silian_count = Math.min(Silian_itemStack.getCount(), Silian_stack.getCount());
                Silian_itemStack.setCount(Silian_itemStack.getCount() - Silian_count);
                Silian_stack.setCount(Silian_stack.getCount() - Silian_count);
            }
        }
    }

    @Unique
    private static ItemStack pca$getItemFromInventory(@NotNull ItemStack Silian_itemStack, Container Silian_container) {
        if (Silian_itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        Item Silian_item = Silian_itemStack.getItem();
        ItemStack Silian_ret = new ItemStack(Silian_item, 0);

        for (int Silian_i = 0; Silian_i < Silian_container.getContainerSize(); ++Silian_i) {
            ItemStack Silian_stack = Silian_container.getItem(Silian_i);
            ItemStackCompat Silian_stackCompat = ItemStackCompat.of(Silian_stack);

            if (!Silian_stack.isEmpty() && Silian_stackCompat.is(Silian_item)) {
                Silian_ret.setCount(Math.min(Silian_stack.getCount() + Silian_ret.getCount(), Silian_ret.getMaxStackSize()));

                if (Silian_ret.getCount() == Silian_ret.getMaxStackSize()) {
                    break;
                }
            }
        }

        return Silian_ret;
    }

    @Inject(method = "dispenseFrom", at = @At(value = "HEAD"), cancellable = true)
    private void autoTrade(
            //#if MC > 11502
            ServerLevel Silian_level,
            //#if MC > 12001
            //$$ BlockState blockState,
            //#endif
            //#else
            //$$ Level Silian_level,
            //#endif
            BlockPos Silian_blockPos,
            CallbackInfo Silian_ci
    ) {
        if (!PluslsCarpetAdditionSettings.autoTrade) {
            return;
        }

        BlockState Silian_state = Silian_level.getBlockState(Silian_blockPos.below());
        BlockStateCompat Silian_blockStateCompat = BlockStateCompat.of(Silian_state);
        boolean Silian_tradeAll;

        if (Silian_blockStateCompat.is(Blocks.EMERALD_BLOCK)) {
            Silian_tradeAll = false;
        } else if (Silian_blockStateCompat.is(Blocks.DIAMOND_BLOCK)) {
            Silian_tradeAll = true;
        } else {
            return;
        }

        BlockPos Silian_faceBlockPos = Silian_blockPos.relative(Silian_level.getBlockState(Silian_blockPos).getValue(DispenserBlock.FACING));
        List<AbstractVillager> Silian_villagerList = Silian_level.getEntitiesOfClass(AbstractVillager.class,
                new AABB(Silian_faceBlockPos), Entity::isAlive);

        if (Silian_villagerList.isEmpty()) {
            return;
        }

        AbstractVillager Silian_merchantEntity = Silian_villagerList.get(0);
        MerchantOffers Silian_offerList = Silian_merchantEntity.getOffers();

        if (Silian_offerList.isEmpty()) {
            return;
        }

        int Silian_tradeId = Silian_level.getBestNeighborSignal(Silian_blockPos);

        if (Silian_tradeId == 0) {
            return;
        }

        MerchantOffer Silian_offer = Silian_offerList.get(Silian_tradeId > Silian_offerList.size() ? Silian_offerList.size() - 1 : Silian_tradeId - 1);
        ItemStack Silian_firstItemStack = Silian_offer.getCostA();
        ItemStack Silian_secondItemStack = Silian_offer.getCostB();
        ItemStack Silian_firstDepleteItem = Silian_firstItemStack.copy();
        ItemStack Silian_secondDepleteItem = Silian_secondItemStack.copy();
        BlockEntity Silian_blockEntity = Silian_level.getBlockEntity(Silian_blockPos);

        if (!(Silian_blockEntity instanceof DispenserBlockEntity)) {
            return;
        }

        //#if MC > 12001
        //$$ BlockSource Silian_blockPointer = new BlockSource(Silian_level, Silian_blockPos, blockState, (DispenserBlockEntity) Silian_blockEntity);
        //#else
        BlockSourceImpl Silian_blockPointer = new BlockSourceImpl(Silian_level, Silian_blockPos);
        //#endif
        DispenserBlockEntity Silian_dispenserBlockEntity = (DispenserBlockEntity) Silian_blockEntity;
        boolean Silian_success = false;

        while (!Silian_offer.isOutOfStock()) {
            ItemStack Silian_firstInventoryItemStack = pca$getItemFromInventory(Silian_firstItemStack, Silian_dispenserBlockEntity);
            ItemStack Silian_secondInventoryItemStack = pca$getItemFromInventory(Silian_secondItemStack, Silian_dispenserBlockEntity);
            int Silian_firstItemCount = Silian_firstInventoryItemStack.getCount();
            int Silian_secondItemCount = Silian_secondInventoryItemStack.getCount();

            if (Silian_offer.take(Silian_firstInventoryItemStack, Silian_secondInventoryItemStack)) {
                Silian_firstDepleteItem.setCount(Silian_firstItemCount - Silian_firstInventoryItemStack.getCount());
                Silian_secondDepleteItem.setCount(Silian_secondItemCount - Silian_secondInventoryItemStack.getCount());
                pca$depleteItemInInventory(Silian_firstDepleteItem, Silian_dispenserBlockEntity);
                pca$depleteItemInInventory(Silian_secondDepleteItem, Silian_dispenserBlockEntity);
                Silian_offer.increaseUses();
                ItemStack Silian_outputItemStack = Silian_offer.assemble();
                pca$itemDispenserBehavior.dispense(Silian_blockPointer, Silian_outputItemStack);
                // make villager happy ~
                Silian_level.broadcastEntityEvent(Silian_merchantEntity, (byte) 14);
                if (Silian_merchantEntity instanceof MyVillagerEntity) {
                    ((MyVillagerEntity) (Silian_merchantEntity)).pca$tradeWithoutPlayer(Silian_offer);
                }
                Silian_success = true;
            } else {
                break;
            }

            if (!Silian_tradeAll) {
                break;
            }
        }

        if (Silian_success) {
            Silian_ci.cancel();
        }
    }
}
