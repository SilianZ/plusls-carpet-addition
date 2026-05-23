package com.plusls.carpet.mixin.rule.playerSit;

import com.mojang.authlib.GameProfile;
import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.util.rule.playerSit.SitEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.player.PlayerCompat;
import top.hendrixshen.magiclib.util.MessageUtil;

//#if 11903 > MC && MC > 11802
//$$ import net.minecraft.world.entity.player.ProfilePublicKey;
//$$ import org.jetbrains.annotations.Nullable;
//#endif

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player {
    private MixinServerPlayer(
            Level Silian_level,
            //#if MC > 11502
            BlockPos Silian_blockPos,
            float Silian_f,
            //#endif
            GameProfile Silian_gameProfile
            //#if 11903 > MC && MC > 11802
            //$$ , @Nullable ProfilePublicKey profilePublicKey
            //#endif
    ) {
        super(
                Silian_level,
                //#if MC > 11502
                Silian_blockPos,
                Silian_f,
                //#endif
                Silian_gameProfile
                //#if 11903 > MC && MC > 11802
                //$$ , profilePublicKey
                //#endif
        );
    }

    @Shadow
    public ServerGamePacketListenerImpl connection;
    @Shadow
    @Final
    public MinecraftServer server;
    @Unique
    private int pca$sneakTimes = 0;
    @Unique
    private long pca$lastSneakTime = 0;

    @Override
    @Intrinsic
    public void setShiftKeyDown(boolean Silian_sneaking) {
        super.setShiftKeyDown(Silian_sneaking);
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference", "target"})
    @Inject(
            //#if MC > 11404
            method = "setShiftKeyDown(Z)V",
            //#else
            //$$ method = "setSneaking(Z)V",
            //#endif
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void customSetShiftKeyDownCheck(boolean Silian_sneaking, CallbackInfo Silian_ci) {
        // Not handled when sneak state doesn't change
        if (!PluslsCarpetAdditionSettings.playerSit || (Silian_sneaking && this.isShiftKeyDown())) {
            return;
        }

        if (Silian_sneaking) {
            long Silian_nowTime = Util.getMillis();
            PlayerCompat Silian_playerCompat = PlayerCompat.of(this);

            // Every sneak interval must not be over 0.2s
            if (Silian_nowTime - this.pca$lastSneakTime > 200) {
                this.pca$sneakTimes = 0;
            } else {
                // Block input update for 0.2s after player sit
                Silian_ci.cancel();
            }

            if (Silian_playerCompat.isOnGround()) {
                this.pca$sneakTimes++;
            }

            this.pca$lastSneakTime = Silian_nowTime;

            if (this.pca$sneakTimes > 2) {
                ArmorStand Silian_armorStandEntity = new ArmorStand(Silian_playerCompat.getLevel(), Silian_playerCompat.getX(), Silian_playerCompat.getY() - 0.16, Silian_playerCompat.getZ());
                ((SitEntity) Silian_armorStandEntity).pca$setSitEntity(true);
                Silian_playerCompat.getLevel().addFreshEntity(Silian_armorStandEntity);
                this.setShiftKeyDown(false);

                if (this.connection != null) {
                    this.connection.send(new ClientboundSetEntityDataPacket(
                            this.getId(),
                            //#if MC > 11902
                            //$$ this.getEntityData().getNonDefaultValues()
                            //#else
                            this.getEntityData(),
                            true
                            //#endif
                    ));
                }

                this.startRiding(Silian_armorStandEntity);
                this.pca$sneakTimes = 0;
                Silian_ci.cancel();
            }
        }
    }
}
