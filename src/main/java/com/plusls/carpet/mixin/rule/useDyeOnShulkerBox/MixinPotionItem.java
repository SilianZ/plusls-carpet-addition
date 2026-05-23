package com.plusls.carpet.mixin.rule.useDyeOnShulkerBox;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.player.PlayerCompat;

//#if MC > 12004
//$$ import com.plusls.carpet.mixin.accessor.AccessorBaseContainerBlockEntity;
//$$ import net.minecraft.core.component.DataComponents;
//#endif

//#if MC < 11900
import org.spongepowered.asm.mixin.Intrinsic;
//#endif

//#if MC < 11800
import net.minecraft.nbt.CompoundTag;
//#endif

@Mixin(PotionItem.class)
public abstract class MixinPotionItem extends Item {
    public MixinPotionItem(Properties Silian_settings) {
        super(Silian_settings);
    }

    //#if MC < 11900
    @Override
    @Intrinsic
    public @NotNull InteractionResult useOn(UseOnContext Silian_useOnContext) {
        return super.useOn(Silian_useOnContext);
    }
    //#endif

    //#if MC < 11900
    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    //#endif
    @Inject(
            //#if MC > 11802
            //$$ method = "useOn",
            //#else
            method = {"useOn" ,"method_7884"},
            remap = false,
            //#endif
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void preUseOn(@NotNull UseOnContext Silian_useOnContext, CallbackInfoReturnable<InteractionResult> Silian_cir) {
        ItemStack Silian_itemStack = Silian_useOnContext.getItemInHand();
        Player Silian_player = Silian_useOnContext.getPlayer();

        if (!PluslsCarpetAdditionSettings.useDyeOnShulkerBox ||
                Silian_player == null ||
                Silian_itemStack.getItem() != Items.POTION ||
                //#if MC > 12004
                //$$ !Silian_itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)
                //#else
                PotionUtils.getPotion(Silian_itemStack) != Potions.WATER
            //#endif
        ) {
            return;
        }

        Level Silian_level = Silian_useOnContext.getLevel();
        BlockPos Silian_pos = Silian_useOnContext.getClickedPos();
        BlockState Silian_blockState = Silian_level.getBlockState(Silian_pos);
        Block Silian_block = Silian_blockState.getBlock();

        if (Silian_block instanceof ShulkerBoxBlock &&
                ((ShulkerBoxBlock) Silian_block).getColor() != null) {
            if (!Silian_level.isClientSide()) {
                ShulkerBoxBlockEntity Silian_blockEntity = (ShulkerBoxBlockEntity) Silian_level.getBlockEntity(Silian_pos);
                BlockState Silian_newBlockState = Blocks.SHULKER_BOX.defaultBlockState().
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

                    if (!Silian_player.isCreative()) {
                        Silian_useOnContext.getItemInHand().shrink(1);
                        PlayerCompat Silian_playerCompat = PlayerCompat.of(Silian_useOnContext.getPlayer());
                        Silian_playerCompat.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
                    }
                }
            }

            Silian_cir.setReturnValue(
                    //#if MC > 11802
                    //$$ InteractionResult.sidedSuccess(Silian_level.isClientSide)
                    //#else
                    Silian_level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS
                    //#endif
            );
        }
    }
}
