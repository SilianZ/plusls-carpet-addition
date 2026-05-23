package com.plusls.carpet.mixin.rule.creativePlayerNoDirectKillArmorStand;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.EntityCompat;

@Mixin(ArmorStand.class)
public abstract class MixinArmorStand extends LivingEntity {
    protected MixinArmorStand(EntityType<? extends LivingEntity> Silian_entityType, Level Silian_world) {
        super(Silian_entityType, Silian_world);
    }

    @Redirect(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;isCreativePlayer()Z",
                    ordinal = 0
            )
    )
    private boolean redirectIsSourceCreativePlayer(DamageSource Silian_instance) {
        if (!EntityCompat.of(this).getLevel().isClientSide() && PluslsCarpetAdditionSettings.creativePlayerNoDirectKillArmorStand) {
            return false;
        }

        return Silian_instance.isCreativePlayer();
    }
}
