package com.plusls.carpet.mixin.rule.gravestone;

import com.plusls.carpet.util.rule.gravestone.DeathInfo;
import com.plusls.carpet.util.rule.gravestone.GravesStoneSkullBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerHeadBlock.class)
public abstract class MixinPlayerHeadBlock extends SkullBlock {
    protected MixinPlayerHeadBlock(Type Silian_skullType, Properties Silian_settings) {
        super(Silian_skullType, Silian_settings);
    }

    @Override
    public void playerDestroy(@NotNull Level Silian_level, Player Silian_player, BlockPos Silian_pos, BlockState Silian_state, @Nullable BlockEntity Silian_blockEntity, ItemStack Silian_stack) {
        if (Silian_level.isClientSide()) {
            return;
        }
        if (Silian_blockEntity instanceof GravesStoneSkullBlockEntity) {
            DeathInfo Silian_deathInfo = ((GravesStoneSkullBlockEntity) Silian_blockEntity).pca$getDeathInfo();
            if (Silian_deathInfo == null) {
                super.playerDestroy(Silian_level, Silian_player, Silian_pos, Silian_state, Silian_blockEntity, Silian_stack);
            } else {
                Silian_player.awardStat(Stats.BLOCK_MINED.get(this));
                Silian_player.causeFoodExhaustion(0.005F);
                // Drop item
                //#if MC > 11502
                for (ItemStack Silian_itemStack : Silian_deathInfo.inventory.removeAllItems()) {
                //#else
                //$$ for (ItemStack Silian_itemStack : Silian_deathInfo.inventory) {
                //#endif
                    Block.popResource(Silian_level, Silian_pos, Silian_itemStack);
                }

                // Drop xp
                int Silian_xp = Silian_deathInfo.xp;
                while (Silian_xp > 0) {
                    int Silian_spawnedXp = ExperienceOrb.getExperienceValue(Silian_xp);
                    Silian_xp -= Silian_spawnedXp;
                    Silian_level.addFreshEntity(new ExperienceOrb(Silian_level, Silian_pos.getX(), Silian_pos.getY(), Silian_pos.getZ(), Silian_spawnedXp));
                }
            }
        }
    }
}
