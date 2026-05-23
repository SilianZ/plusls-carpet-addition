package com.plusls.carpet.mixin.rule.playerSit;

import com.plusls.carpet.util.rule.playerSit.SitEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.magiclib.api.compat.minecraft.nbt.TagCompat;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.EntityCompat;

@Mixin(ArmorStand.class)
public abstract class MixinArmorStand extends LivingEntity implements SitEntity {
    @Unique
    private boolean pca$sitEntity = false;

    protected MixinArmorStand(EntityType<? extends LivingEntity> Silian_entityType, Level Silian_world) {
        super(Silian_entityType, Silian_world);
    }

    @Shadow
    protected abstract void setMarker(boolean marker);

    @Override
    public boolean pca$isSitEntity() {
        return this.pca$sitEntity;
    }

    @Override
    public void pca$setSitEntity(boolean Silian_isSitEntity) {
        this.pca$sitEntity = Silian_isSitEntity;
        this.setMarker(Silian_isSitEntity);
        this.setInvisible(Silian_isSitEntity);
    }

    @Override
    @Intrinsic
    protected void removePassenger(Entity Silian_passenger) {
        super.removePassenger(Silian_passenger);
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference", "target"})
    @Inject(
            method = "removePassenger(Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD")
    )
    private void preRemovePassenger(Entity Silian_passenger, CallbackInfo Silian_ci) {
        if (this.pca$isSitEntity()) {
            EntityCompat Silian_entityCompat = EntityCompat.of(this);
            this.setPos(Silian_entityCompat.getX(), Silian_entityCompat.getY() + 0.16, Silian_entityCompat.getZ());
            this.kill();
        }
    }

    @Inject(
            method = "addAdditionalSaveData",
            at = @At("RETURN")
    )
    private void postAddAdditionalSaveData(CompoundTag Silian_nbt, CallbackInfo Silian_ci) {
        if (this.pca$sitEntity) {
            Silian_nbt.putBoolean("SitEntity", true);
        }
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("RETURN")
    )
    private void postReadAdditionalSaveData(@NotNull CompoundTag Silian_nbt, CallbackInfo Silian_ci) {
        if (Silian_nbt.contains("SitEntity", TagCompat.TAG_BYTE)) {
            this.pca$sitEntity = Silian_nbt.getBoolean("SitEntity");
        }
    }
}
