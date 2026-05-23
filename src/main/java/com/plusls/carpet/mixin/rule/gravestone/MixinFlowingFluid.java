package com.plusls.carpet.mixin.rule.gravestone;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowingFluid.class)
public abstract class MixinFlowingFluid extends Fluid {
    @Inject(
            method = "canHoldFluid",
            at = @At(
                    value = "RETURN"
            ),
            cancellable = true
    )
    private void checkRail(BlockGetter Silian_blockGetter, BlockPos Silian_pos, BlockState Silian_state, Fluid Silian_fluid, @NotNull CallbackInfoReturnable<Boolean> Silian_cir) {
        if (!Silian_cir.getReturnValue() || !(Silian_state.getBlock() instanceof PlayerHeadBlock)) {
            return;
        }

        BlockEntity Silian_blockEntity = Silian_blockGetter.getBlockEntity(Silian_pos);

        if (Silian_blockEntity == null) {
            return;
        }

        //#if MC > 11701
        //$$ CompoundTag Silian_nbt = Silian_blockEntity.saveWithoutMetadata(
        //#if MC > 12004
        //$$         Silian_blockEntity.getLevel().registryAccess()
        //#endif
        //$$ );
        //#else
        CompoundTag Silian_nbt = Silian_blockEntity.save(new CompoundTag());
        //#endif

        if (Silian_nbt.contains("DeathInfo")) {
            Silian_cir.setReturnValue(false);
        }
    }
}