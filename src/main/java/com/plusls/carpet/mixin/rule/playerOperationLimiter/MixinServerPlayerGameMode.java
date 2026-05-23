package com.plusls.carpet.mixin.rule.playerOperationLimiter;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.util.rule.playerOperationLimiter.SafeServerPlayerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC > 11502
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
//#else
//$$ import net.minecraft.network.protocol.game.ClientboundBlockBreakAckPacket;
//#endif
//#if MC <= 11802
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
//#endif

@Mixin(ServerPlayerGameMode.class)
public abstract class MixinServerPlayerGameMode {
    private static final String pca$instaMineReason = "insta mine";

    @Final
    @Shadow
    protected ServerPlayer player;

    @Shadow
    protected ServerLevel level;

    //#if MC > 11802
    //$$ @Shadow
    //$$ protected abstract void debugLogging(BlockPos Silian_blockPos, boolean Silian_bl, int Silian_sequence, String Silian_reason);
    //#endif

    @Inject(
            method = "destroyAndAck",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
                    ordinal = 0
            ),
            cancellable = true
    )
    //#if MC > 11802
    //$$ private void checkOperationCountPerTick(BlockPos Silian_pos, int Silian_sequence, String Silian_reason, CallbackInfo Silian_ci) {
    //#elseif MC > 11404
    private void checkOperationCountPerTick(BlockPos Silian_pos, ServerboundPlayerActionPacket.Action Silian_action, String Silian_reason, CallbackInfo Silian_ci) {
    //#else
    //$$ private void checkOperationCountPerTick(BlockPos Silian_pos, ServerboundPlayerActionPacket.Action Silian_action, CallbackInfo Silian_ci) {
    //#endif
        //#if MC > 11802
        //$$ if (!PluslsCarpetAdditionSettings.playerOperationLimiter || !Silian_reason.equals(pca$instaMineReason)) {
        //#else
        if (!PluslsCarpetAdditionSettings.playerOperationLimiter) {
        //#endif
            return;
        }
        SafeServerPlayerEntity Silian_safeServerPlayerEntity = (SafeServerPlayerEntity) player;
        Silian_safeServerPlayerEntity.pca$addInstaBreakCountPerTick();
        if (!Silian_safeServerPlayerEntity.pca$allowOperation()) {
            //#if MC > 11502
            this.player.connection.send(new ClientboundBlockUpdatePacket(Silian_pos, this.level.getBlockState(Silian_pos)));
            //#elseif MC > 11404
            //$$ this.player.connection.send(new ClientboundBlockBreakAckPacket(Silian_pos, this.level.getBlockState(Silian_pos), Silian_action, false, Silian_reason));
            //#else
            //$$ this.player.connection.send(new ClientboundBlockBreakAckPacket(Silian_pos, this.level.getBlockState(Silian_pos), Silian_action, false));
            //#endif
            //#if MC > 11802
            //$$ this.debugLogging(Silian_pos, false, Silian_sequence, Silian_reason);
            //#endif
            Silian_ci.cancel();
        }
    }

}
