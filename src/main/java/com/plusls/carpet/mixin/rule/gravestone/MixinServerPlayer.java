package com.plusls.carpet.mixin.rule.gravestone;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import top.hendrixshen.magiclib.dependency.api.annotation.Dependencies;
import top.hendrixshen.magiclib.dependency.api.annotation.Dependency;
//#if MC > 11502
import net.minecraft.core.BlockPos;
//#else
//$$ import com.plusls.carpet.util.rule.gravestone.GravestoneUtil;
//$$ import net.minecraft.world.damagesource.DamageSource;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import top.hendrixshen.magiclib.util.MiscUtil;
//#endif
//#if MC > 11802 && MC < 11903
//$$ import net.minecraft.world.entity.player.ProfilePublicKey;
//$$ import org.jetbrains.annotations.Nullable;
//#endif

@Dependencies(and = @Dependency(value = "minecraft", versionPredicate = ">=1.15.2"))
@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player {
    //#if MC > 11902
    //$$ public MixinServerPlayer(Level Silian_level, BlockPos Silian_blockPos, float Silian_f, GameProfile Silian_gameProfile) {
    //$$     super(Silian_level, Silian_blockPos, Silian_f, Silian_gameProfile);
    //$$ }
    //#elseif MC > 11802
    //$$ public MixinServerPlayer(Level Silian_level, BlockPos Silian_blockPos, float Silian_f, GameProfile Silian_gameProfile, @Nullable ProfilePublicKey Silian_profilePublicKey) {
    //$$     super(Silian_level, Silian_blockPos, Silian_f, Silian_gameProfile, Silian_profilePublicKey);
    //$$ }
    //#elseif MC > 11502
    public MixinServerPlayer(Level Silian_level, BlockPos Silian_blockPos, float Silian_f, GameProfile Silian_gameProfile) {
        super(Silian_level, Silian_blockPos, Silian_f, Silian_gameProfile);
    }
    //#else
    //$$ public MixinServerPlayer(Level Silian_level, GameProfile Silian_gameProfile) {
    //$$     super(Silian_level, Silian_gameProfile);
    //$$ }
    //#endif

    //#if MC <= 11502
    //$$ // hook death
    //$$ @Inject(
    //$$         method = "die",
    //$$         at = @At(
    //$$                 value = "INVOKE",
    //$$                 target = "Lnet/minecraft/server/level/ServerPlayer;dropAllDeathLoot(Lnet/minecraft/world/damagesource/DamageSource;)V"
    //$$         )
    //$$ )
    //$$ private void onOnDeath(DamageSource Silian_source, CallbackInfo Silian_ci) {
    //$$     GravestoneUtil.deathHandle(MiscUtil.cast(this));
    //$$ }
    //#endif
}
