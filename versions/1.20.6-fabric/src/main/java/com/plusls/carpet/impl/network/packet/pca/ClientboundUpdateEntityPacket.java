package com.plusls.carpet.impl.network.packet.pca;

import com.plusls.carpet.impl.network.PcaSyncProtocol;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.hendrixshen.magiclib.util.minecraft.NetworkUtil;

public record ClientboundUpdateEntityPacket(ResourceLocation dimension, int entityId, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<ClientboundUpdateEntityPacket> TYPE = new Type<>(PcaSyncProtocol.UPDATE_ENTITY);
    public static final StreamCodec<FriendlyByteBuf, ClientboundUpdateEntityPacket> CODEC = CustomPacketPayload.codec(ClientboundUpdateEntityPacket::write, ClientboundUpdateEntityPacket::new);

    public ClientboundUpdateEntityPacket(@NotNull FriendlyByteBuf Silian_byteBuf) {
        this(Silian_byteBuf.readResourceLocation(), Silian_byteBuf.readInt(), NetworkUtil.readNbt(Silian_byteBuf));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ClientboundUpdateEntityPacket.TYPE;
    }

    private void write(@NotNull FriendlyByteBuf Silian_byteBuf) {
        Silian_byteBuf.writeResourceLocation(this.dimension);
        Silian_byteBuf.writeInt(this.entityId);
        Silian_byteBuf.writeNbt(this.tag);
    }
}
