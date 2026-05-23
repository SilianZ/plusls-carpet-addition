package com.plusls.carpet.impl.network.packet.pca;

import com.plusls.carpet.impl.network.PcaSyncProtocol;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.hendrixshen.magiclib.util.minecraft.NetworkUtil;

public record ClientboundUpdateBlockEntityPacket(ResourceLocation dimension, BlockPos blockPos, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<ClientboundUpdateBlockEntityPacket> TYPE = new Type<>(PcaSyncProtocol.UPDATE_BLOCK_ENTITY);
    public static final StreamCodec<FriendlyByteBuf, ClientboundUpdateBlockEntityPacket> CODEC = CustomPacketPayload.codec(ClientboundUpdateBlockEntityPacket::write, ClientboundUpdateBlockEntityPacket::new);

    public ClientboundUpdateBlockEntityPacket(@NotNull FriendlyByteBuf Silian_byteBuf) {
        this(Silian_byteBuf.readResourceLocation(), Silian_byteBuf.readBlockPos(), NetworkUtil.readNbt(Silian_byteBuf));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ClientboundUpdateBlockEntityPacket.TYPE;
    }

    private void write(@NotNull FriendlyByteBuf Silian_byteBuf) {
        Silian_byteBuf.writeResourceLocation(this.dimension);
        Silian_byteBuf.writeBlockPos(this.blockPos);
        Silian_byteBuf.writeNbt(this.tag);
    }
}
