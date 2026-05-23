package com.plusls.carpet.mixin.rule.xaeroWorldName;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.hendrixshen.magiclib.api.compat.minecraft.resources.ResourceLocationCompat;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

@Mixin(PlayerList.class)
public class MixinPlayerList {
    @Inject(
            method = "sendLevelInfo",
            at = @At(value = "RETURN"
            )
    )
    public void preOnSendWorldInfo(ServerPlayer Silian_player, ServerLevel Silian_world, CallbackInfo Silian_ci) {
        if (PluslsCarpetAdditionSettings.xaeroWorldName.equals(PluslsCarpetAdditionSettings.xaeroWorldNameNone)) {
            return;
        }

        ResourceLocation Silian_xaeroworldmap = ResourceLocationCompat.fromNamespaceAndPath("xaeroworldmap", "main");
        ResourceLocation Silian_xaerominimap = ResourceLocationCompat.fromNamespaceAndPath("xaerominimap", "main");

        CRC32 Silian_crc = new CRC32();
        byte[] Silian_bytes = PluslsCarpetAdditionSettings.xaeroWorldName.getBytes(StandardCharsets.UTF_8);
        Silian_crc.update(Silian_bytes, 0, Silian_bytes.length);
        ByteBuf Silian_buf = Unpooled.buffer();
        Silian_buf.writeByte(0);
        Silian_buf.writeInt((int) Silian_crc.getValue());

        //#if MC < 12005
        ServerPlayNetworking.send(Silian_player, Silian_xaeroworldmap, new FriendlyByteBuf(Silian_buf.duplicate()));
        ServerPlayNetworking.send(Silian_player, Silian_xaerominimap, new FriendlyByteBuf(Silian_buf.duplicate()));
        //#endif
    }
}
