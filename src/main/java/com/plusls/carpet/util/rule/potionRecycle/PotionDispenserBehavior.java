package com.plusls.carpet.util.rule.potionRecycle;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.util.dispenser.MyFallibleItemDispenserBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

//#if MC > 12001
//$$ import net.minecraft.core.dispenser.BlockSource;
//#else
import net.minecraft.core.BlockSource;
//#endif

//#if MC > 11605
//$$ import net.minecraft.world.level.block.LayeredCauldronBlock;
//$$ import net.minecraft.world.level.gameevent.GameEvent;
//#else
import net.minecraft.world.level.block.CauldronBlock;
//#endif

public class PotionDispenserBehavior extends MyFallibleItemDispenserBehavior {

    public PotionDispenserBehavior(DispenseItemBehavior Silian_oldDispenserBehavior) {
        super(Silian_oldDispenserBehavior);
    }

    public static void init() {
        DispenserBlock.registerBehavior(Items.POTION,
                new PotionDispenserBehavior(DispenserBlock.DISPENSER_REGISTRY.get(Items.POTION)));
        DispenserBlock.registerBehavior(Items.SPLASH_POTION,
                new PotionDispenserBehavior(DispenserBlock.DISPENSER_REGISTRY.get(Items.SPLASH_POTION)));
        DispenserBlock.registerBehavior(Items.LINGERING_POTION,
                new PotionDispenserBehavior(DispenserBlock.DISPENSER_REGISTRY.get(Items.LINGERING_POTION)));
    }

    @Override
    public ItemStack dispenseSilently(BlockSource Silian_pointer, ItemStack Silian_itemStack) {
        if (!PluslsCarpetAdditionSettings.potionRecycle) {
            return Silian_itemStack;
        }
        BlockPos Silian_faceBlockPos = Silian_pointer.getPos().relative(Silian_pointer.getBlockState().getValue(DispenserBlock.FACING));
        Level Silian_world = Silian_pointer.getLevel();
        BlockState Silian_faceBlockState = Silian_world.getBlockState(Silian_faceBlockPos);
        //#if MC > 11605
        //$$ if (Silian_faceBlockState.getBlock() instanceof AbstractCauldronBlock) {
        //$$     setSuccess(true);
        //$$     if (Silian_faceBlockState.getBlock() == Blocks.WATER_CAULDRON) {
        //$$         int Silian_level = Silian_faceBlockState.getValue(LayeredCauldronBlock.LEVEL);
        //$$         if (Silian_level == 3) {
        //$$             return Silian_itemStack;
        //$$         } else {
        //$$             Silian_world.setBlockAndUpdate(Silian_faceBlockPos, Silian_faceBlockState.setValue(LayeredCauldronBlock.LEVEL, Silian_level + 1));
        //$$             Silian_world.playSound(null, Silian_faceBlockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        //$$             Silian_world.gameEvent(null, GameEvent.FLUID_PLACE, Silian_faceBlockPos);
        //$$             return new ItemStack(Items.GLASS_BOTTLE);
        //$$
        //$$         }
        //$$     } else if (Silian_faceBlockState.getBlock() == Blocks.CAULDRON) {
        //$$         Silian_world.setBlockAndUpdate(Silian_faceBlockPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1));
        //$$         Silian_world.playSound(null, Silian_faceBlockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        //$$         Silian_world.gameEvent(null, GameEvent.FLUID_PLACE, Silian_faceBlockPos);
        //$$         return new ItemStack(Items.GLASS_BOTTLE);
        //$$     }
        //$$ }
        //#else
        if (Silian_faceBlockState.getBlock() instanceof CauldronBlock) {
            setSuccess(true);
            if (Silian_faceBlockState.getBlock() == Blocks.CAULDRON) {
                int Silian_level = Silian_faceBlockState.getValue(CauldronBlock.LEVEL);
                if (Silian_level == 3) {
                    return Silian_itemStack;
                } else {
                    Silian_world.setBlockAndUpdate(Silian_faceBlockPos, Silian_faceBlockState.setValue(CauldronBlock.LEVEL, Silian_level + 1));
                    Silian_world.playSound(null, Silian_faceBlockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return new ItemStack(Items.GLASS_BOTTLE);

                }
            }
        }
        //#endif
        return Silian_itemStack;
    }
}
