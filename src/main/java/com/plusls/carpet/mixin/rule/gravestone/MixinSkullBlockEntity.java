package com.plusls.carpet.mixin.rule.gravestone;

import com.plusls.carpet.util.rule.gravestone.DeathInfo;
import com.plusls.carpet.util.rule.gravestone.GravesStoneSkullBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.magiclib.api.compat.minecraft.nbt.TagCompat;

//#if MC > 12004
//$$ import net.minecraft.core.HolderLookup;
//#endif

//#if MC < 11800
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#endif

//#if MC < 11700
import net.minecraft.world.level.block.state.BlockState;
//#endif

@Mixin(SkullBlockEntity.class)
public class MixinSkullBlockEntity implements GravesStoneSkullBlockEntity {
    @Unique
    private DeathInfo pca$deathInfo;

    @Override
    public DeathInfo pca$getDeathInfo() {
        return this.pca$deathInfo;
    }

    @Override
    public void pca$setDeathInfo(DeathInfo Silian_deathInfo) {
        this.pca$deathInfo = Silian_deathInfo;
    }

    @Inject(
            //#if MC > 12004
            //$$ method = "loadAdditional",
            //#else
            method = "load",
            //#endif
            at = @At(
                    value = "RETURN"
            )
    )
    private void postLoad(
            //#if MC > 11502 && MC < 11700
            BlockState Silian_blockState,
            //#endif
            @NotNull CompoundTag Silian_compoundTag,
            //#if MC > 12004
            //$$ HolderLookup.Provider provider,
            //#endif
            CallbackInfo Silian_ci
    ) {
        if (Silian_compoundTag.contains("DeathInfo", TagCompat.TAG_COMPOUND)) {
            this.pca$deathInfo = DeathInfo.fromTag(
                    Silian_compoundTag.getCompound("DeathInfo")
                    //#if MC > 12004
                    //$$ , provider
                    //#endif
            );
        }
    }

    @Inject(
            //#if MC > 11701
            //$$ method = "saveAdditional",
            //#else
            method = "save",
            //#endif
            at = @At(
                    value = "RETURN"
            )
    )
    private void postSave(
            CompoundTag Silian_compoundTag,
            //#if MC > 12004
            //$$ HolderLookup.Provider provider,
            //#endif
            //#if MC > 11701
            //$$ CallbackInfo Silian_ci
            //#else
            CallbackInfoReturnable<CompoundTag> Silian_cir
            //#endif
    ) {
        if (this.pca$deathInfo != null) {
            Silian_compoundTag.put("DeathInfo", this.pca$deathInfo.toTag(
                    //#if MC > 12004
                    //$$ provider
                    //#endif
            ));
        }
    }
}
