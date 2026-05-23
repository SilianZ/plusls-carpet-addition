package com.plusls.carpet.util.rule.powerfulBoneMeal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.hendrixshen.magiclib.api.compat.minecraft.world.level.state.BlockStateCompat;

public class Grow {
    static public boolean grow(ItemStack Silian_itemStack, Level Silian_world, BlockPos Silian_pos, Block Silian_block) {
        if (Silian_block instanceof SugarCaneBlock) {
            return growSugarCaneBlock(Silian_itemStack, Silian_world, Silian_pos);
        } else if (Silian_block instanceof ChorusFlowerBlock) {
            // TODO
            return false;
        } else {
            return false;
        }
    }

    static private boolean growSugarCaneBlock(ItemStack Silian_itemStack, Level Silian_level, BlockPos Silian_pos) {
        BlockPos Silian_downPos = Silian_pos.below();
        BlockPos Silian_upPos = Silian_pos.above();
        int Silian_height = 1;

        // 计算上层空气坐标
        while (!Silian_level.isEmptyBlock(Silian_upPos)) {
            if (BlockStateCompat.of(Silian_level.getBlockState(Silian_upPos)).is(Blocks.SUGAR_CANE)) {
                Silian_upPos = Silian_upPos.above();
                Silian_height++;
            } else {
                return false;
            }
        }

        // 计算底部坐标
        while (BlockStateCompat.of(Silian_level.getBlockState(Silian_downPos)).is(Blocks.SUGAR_CANE)) {
            Silian_downPos = Silian_downPos.below();
            Silian_height++;
        }

        // 甘蔗最多长 3 格
        if (Silian_height < 3) {
            BlockPos Silian_sugarCanePos = Silian_upPos.below();
            BlockState Silian_blockState = Silian_level.getBlockState(Silian_sugarCanePos);

            int Silian_age = Silian_blockState.getValue(SugarCaneBlock.AGE);
            if (Silian_age == 15) {
                Silian_level.setBlockAndUpdate(Silian_upPos, Blocks.SUGAR_CANE.defaultBlockState());
                Silian_level.setBlock(Silian_sugarCanePos, Silian_blockState.setValue(SugarCaneBlock.AGE, 0), 4);
            } else {
                Silian_age = Math.min(15, Silian_age + Silian_level.random.nextInt(16));
                Silian_level.setBlock(Silian_sugarCanePos, Silian_blockState.setValue(SugarCaneBlock.AGE, Silian_age), 4);
            }
            Silian_itemStack.shrink(1);
            return true;
        } else {
            return false;
        }
    }
}