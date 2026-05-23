package com.plusls.carpet.mixin.rule.playerOperationLimiter;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.util.rule.playerOperationLimiter.SafeServerPlayerEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem extends Item {
    public MixinBlockItem(Properties Silian_settings) {
        super(Silian_settings);
    }

    @Shadow
    protected abstract BlockState getPlacementState(BlockPlaceContext Silian_context);

    @Shadow
    public abstract @Nullable BlockPlaceContext updatePlacementContext(BlockPlaceContext blockPlaceContext);

    @Inject(
            method = "place",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void checkOperationCountPerTick(BlockPlaceContext Silian_context, CallbackInfoReturnable<InteractionResult> Silian_cir) {
        if (!PluslsCarpetAdditionSettings.playerOperationLimiter || Silian_context.getLevel().isClientSide()) {
            return;
        }

        if (Silian_context.canPlace()) {
            BlockPlaceContext Silian_itemPlacementContext = this.updatePlacementContext(Silian_context);
            SafeServerPlayerEntity Silian_safeServerPlayerEntity = (SafeServerPlayerEntity) Silian_context.getPlayer();
            if (Silian_safeServerPlayerEntity != null && Silian_itemPlacementContext != null && this.getPlacementState(Silian_itemPlacementContext) != null) {
                Silian_safeServerPlayerEntity.pca$addPlaceBlockCountPerTick();
                if (!Silian_safeServerPlayerEntity.pca$allowOperation()) {
                    Silian_cir.setReturnValue(InteractionResult.FAIL);
                }
            }
        }
    }
}
