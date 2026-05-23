package com.plusls.carpet.mixin.rule.useDyeOnShulkerBox;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.hendrixshen.magiclib.api.compat.minecraft.world.level.state.BlockStateCompat;

//#if MC > 12004
//$$ import com.plusls.carpet.mixin.accessor.AccessorBaseContainerBlockEntity;
//#endif

//#if MC < 11800
import net.minecraft.nbt.CompoundTag;
//#endif

@Mixin(DyeItem.class)
public abstract class MixinDyeItem extends Item {
    public MixinDyeItem(Properties Silian_settings) {
        super(Silian_settings);
    }

    @Shadow
    public abstract DyeColor getDyeColor();

    @Override
    @Intrinsic
    public @NotNull InteractionResult useOn(UseOnContext Silian_useOnContext) {
        return super.useOn(Silian_useOnContext);
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference", "target"})
    @Inject(
            method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void preUseOn(UseOnContext Silian_context, CallbackInfoReturnable<InteractionResult> Silian_cir) {
        if (!PluslsCarpetAdditionSettings.useDyeOnShulkerBox) {
            return;
        }

        Level Silian_level = Silian_context.getLevel();
        BlockPos Silian_pos = Silian_context.getClickedPos();
        BlockState Silian_blockState = Silian_level.getBlockState(Silian_pos);
        BlockStateCompat Silian_blockStateCompat = BlockStateCompat.of(Silian_blockState);

        if (Silian_blockStateCompat.is(Blocks.SHULKER_BOX)) {
            return;
        }

        if (!Silian_level.isClientSide()) {
            ShulkerBoxBlockEntity Silian_blockEntity = (ShulkerBoxBlockEntity) Silian_level.getBlockEntity(Silian_pos);
            BlockState Silian_newBlockState = ShulkerBoxBlock.getBlockByColor(this.getDyeColor()).defaultBlockState().
                    setValue(ShulkerBoxBlock.FACING, Silian_blockState.getValue(ShulkerBoxBlock.FACING));

            if (Silian_level.setBlockAndUpdate(Silian_pos, Silian_newBlockState)) {
                ShulkerBoxBlockEntity Silian_newBlockEntity = (ShulkerBoxBlockEntity) Silian_level.getBlockEntity(Silian_pos);
                assert Silian_blockEntity != null;
                assert Silian_newBlockEntity != null;
                Silian_newBlockEntity.loadFromTag(
                        //#if MC > 11701
                        //$$ Silian_blockEntity.saveWithoutMetadata(
                        //#if MC > 12004
                        //$$         Silian_level.registryAccess()
                        //#endif
                        //$$ )
                        //#else
                        new CompoundTag()
                        //#endif
                        //#if MC > 12004
                        //$$ , Silian_level.registryAccess()
                        //#endif
                );
                //#if MC > 12004
                //$$ ((AccessorBaseContainerBlockEntity) Silian_newBlockEntity).pca$setName(Silian_blockEntity.getCustomName());
                //#else
                Silian_newBlockEntity.setCustomName(Silian_blockEntity.getCustomName());
                //#endif
                Silian_newBlockEntity.setChanged();
                Silian_context.getItemInHand().shrink(1);
            }
        }

        Silian_cir.setReturnValue(
                //#if MC > 11502
                InteractionResult.sidedSuccess(Silian_level.isClientSide)
                //#else
                //$$ Silian_level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS
                //#endif
        );
    }
}
