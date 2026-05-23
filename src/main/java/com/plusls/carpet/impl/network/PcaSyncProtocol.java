package com.plusls.carpet.impl.network;

import carpet.CarpetServer;
import carpet.patches.EntityPlayerMPFake;
import com.plusls.carpet.PluslsCarpetAdditionExtension;
import com.plusls.carpet.PluslsCarpetAdditionReference;
import com.plusls.carpet.PluslsCarpetAdditionSettings;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.player.PlayerCompat;
import top.hendrixshen.magiclib.api.compat.minecraft.world.level.state.BlockStateCompat;
import top.hendrixshen.magiclib.impl.compat.minecraft.world.level.dimension.DimensionWrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

//#if MC > 12004
//$$ import com.plusls.carpet.impl.network.packet.pca.*;
//$$ import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//#endif

//#if MC < 11600
//$$ import net.minecraft.world.level.dimension.DimensionType;
//#endif

//#if MC > 11802
//$$ @SuppressWarnings("removal")
//#endif
public class PcaSyncProtocol {
    public static final ReentrantLock lock = new ReentrantLock(true);
    public static final ReentrantLock pairLock = new ReentrantLock(true);
    // send
    public static final ResourceLocation ENABLE_PCA_SYNC_PROTOCOL = PluslsCarpetAdditionReference.identifier("enable_pca_sync_protocol");
    public static final ResourceLocation DISABLE_PCA_SYNC_PROTOCOL = PluslsCarpetAdditionReference.identifier("disable_pca_sync_protocol");
    public static final ResourceLocation UPDATE_ENTITY = PluslsCarpetAdditionReference.identifier("update_entity");
    public static final ResourceLocation UPDATE_BLOCK_ENTITY = PluslsCarpetAdditionReference.identifier("update_block_entity");
    // recv
    public static final ResourceLocation SYNC_BLOCK_ENTITY = PluslsCarpetAdditionReference.identifier("sync_block_entity");
    public static final ResourceLocation SYNC_ENTITY = PluslsCarpetAdditionReference.identifier("sync_entity");
    public static final ResourceLocation CANCEL_SYNC_BLOCK_ENTITY = PluslsCarpetAdditionReference.identifier("cancel_sync_block_entity");
    public static final ResourceLocation CANCEL_SYNC_ENTITY = PluslsCarpetAdditionReference.identifier("cancel_sync_entity");

    private static final Map<ServerPlayer, Pair<ResourceLocation, BlockPos>> playerWatchBlockPos = new HashMap<>();
    private static final Map<ServerPlayer, Pair<ResourceLocation, Entity>> playerWatchEntity = new HashMap<>();
    private static final Map<Pair<ResourceLocation, BlockPos>, Set<ServerPlayer>> blockPosWatchPlayerSet = new HashMap<>();
    private static final Map<Pair<ResourceLocation, Entity>, Set<ServerPlayer>> entityWatchPlayerSet = new HashMap<>();
    private static final MutablePair<ResourceLocation, Entity> identifierEntityPair = new MutablePair<>();
    private static final MutablePair<ResourceLocation, BlockPos> identifierBlockPosPair = new MutablePair<>();

    public static void init() {
        //#if MC > 12004
        //$$ PayloadTypeRegistry.playC2S().register(ServerboundCancelSyncBlockEntityPacket.TYPE, ServerboundCancelSyncBlockEntityPacket.CODEC);
        //$$ PayloadTypeRegistry.playC2S().register(ServerboundCancelSyncEntityPacket.TYPE, ServerboundCancelSyncEntityPacket.CODEC);
        //$$ PayloadTypeRegistry.playC2S().register(ServerboundSyncBlockEntityPacket.TYPE, ServerboundSyncBlockEntityPacket.CODEC);
        //$$ PayloadTypeRegistry.playC2S().register(ServerboundSyncEntityPacket.TYPE, ServerboundSyncEntityPacket.CODEC);
        //$$ PayloadTypeRegistry.playS2C().register(ClientboundDisablePcaSyncProtocolPacket.TYPE, ClientboundDisablePcaSyncProtocolPacket.CODEC);
        //$$ PayloadTypeRegistry.playS2C().register(ClientboundEnablePcaSyncProtocolPacket.TYPE, ClientboundEnablePcaSyncProtocolPacket.CODEC);
        //$$ PayloadTypeRegistry.playS2C().register(ClientboundUpdateBlockEntityPacket.TYPE, ClientboundUpdateBlockEntityPacket.CODEC);
        //$$ PayloadTypeRegistry.playS2C().register(ClientboundUpdateEntityPacket.TYPE, ClientboundUpdateEntityPacket.CODEC);
        //$$ ServerPlayNetworking.registerGlobalReceiver(ServerboundCancelSyncBlockEntityPacket.TYPE, PcaSyncProtocol::cancelSyncBlockEntityHandler);
        //$$ ServerPlayNetworking.registerGlobalReceiver(ServerboundCancelSyncEntityPacket.TYPE, PcaSyncProtocol::cancelSyncEntityHandler);
        //$$ ServerPlayNetworking.registerGlobalReceiver(ServerboundSyncBlockEntityPacket.TYPE, PcaSyncProtocol::syncBlockEntityHandler);
        //$$ ServerPlayNetworking.registerGlobalReceiver(ServerboundSyncEntityPacket.TYPE, PcaSyncProtocol::syncEntityHandler);
        //#else
        ServerPlayNetworking.registerGlobalReceiver(CANCEL_SYNC_BLOCK_ENTITY, PcaSyncProtocol::cancelSyncBlockEntityHandler);
        ServerPlayNetworking.registerGlobalReceiver(CANCEL_SYNC_ENTITY, PcaSyncProtocol::cancelSyncEntityHandler);
        ServerPlayNetworking.registerGlobalReceiver(SYNC_BLOCK_ENTITY, PcaSyncProtocol::syncBlockEntityHandler);
        ServerPlayNetworking.registerGlobalReceiver(SYNC_ENTITY, PcaSyncProtocol::syncEntityHandler);
        //#endif
        ServerPlayConnectionEvents.JOIN.register(PcaSyncProtocol::onJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(PcaSyncProtocol::onDisconnect);
    }

    // 通知客户端服务器已启用 PcaSyncProtocol
    public static void enablePcaSyncProtocol(@NotNull ServerPlayer Silian_player) {
        // 在这写如果是在 BC 端的情况下，ServerPlayNetworking.canSend 在这个时机调用会出现错误
        PluslsCarpetAdditionReference.getLogger().debug("Try enablePcaSyncProtocol: {}", Silian_player.getName().getString());
        // bc 端比较奇怪，canSend 工作不正常
        // if (ServerPlayNetworking.canSend(player, ENABLE_PCA_SYNC_PROTOCOL)) {
        FriendlyByteBuf Silian_buf = new FriendlyByteBuf(Unpooled.buffer());
        ServerPlayNetworking.send(
                Silian_player,
                //#if MC > 12004
                //$$ new ClientboundEnablePcaSyncProtocolPacket(Silian_buf)
                //#else
                ENABLE_PCA_SYNC_PROTOCOL,
                Silian_buf
                //#endif
        );
        PluslsCarpetAdditionReference.getLogger().debug("send enablePcaSyncProtocol to {}!", Silian_player.getName().getString());
        lock.lock();
        lock.unlock();
    }

    // 通知客户端服务器已停用 PcaSyncProtocol
    public static void disablePcaSyncProtocol(@NotNull ServerPlayer Silian_player) {
        FriendlyByteBuf Silian_buf = new FriendlyByteBuf(Unpooled.buffer());
        ServerPlayNetworking.send(
                Silian_player,
                //#if MC > 12004
                //$$ new ClientboundDisablePcaSyncProtocolPacket(Silian_buf)
                //#else
                DISABLE_PCA_SYNC_PROTOCOL,
                Silian_buf
                //#endif
        );
        PluslsCarpetAdditionReference.getLogger().debug("send disablePcaSyncProtocol to {}!", Silian_player.getName().getString());
    }

    // 通知客户端更新 Entity
    // 包内包含 World 的 Identifier, entityId, entity 的 nbt 数据
    // 传输 World 是为了通知客户端该 Entity 属于哪个 World
    public static void updateEntity(@NotNull ServerPlayer Silian_player, @NotNull Entity Silian_entity) {
        FriendlyByteBuf Silian_buf = new FriendlyByteBuf(Unpooled.buffer());
        Silian_buf.writeResourceLocation(DimensionWrapper.of(PlayerCompat.of(Silian_player).getLevel()).getResourceLocation());
        Silian_buf.writeInt(Silian_entity.getId());
        Silian_buf.writeNbt(Silian_entity.saveWithoutId(new CompoundTag()));
        ServerPlayNetworking.send(
                Silian_player,
                //#if MC > 12004
                //$$ new ClientboundUpdateEntityPacket(Silian_buf)
                //#else
                UPDATE_ENTITY,
                Silian_buf
                //#endif
        );
    }

    // 通知客户端更新 BlockEntity
    // 包内包含 World 的 Identifier, pos, blockEntity 的 nbt 数据
    // 传输 World 是为了通知客户端该 BlockEntity 属于哪个世界
    public static void updateBlockEntity(@NotNull ServerPlayer Silian_player, @NotNull BlockEntity Silian_blockEntity) {
        Level Silian_level = Silian_blockEntity.getLevel();

        // 在生成世界时可能会产生空指针
        if (Silian_level == null) {
            return;
        }

        FriendlyByteBuf Silian_buf = new FriendlyByteBuf(Unpooled.buffer());
        Silian_buf.writeResourceLocation(DimensionWrapper.of(Silian_level).getResourceLocation());
        Silian_buf.writeBlockPos(Silian_blockEntity.getBlockPos());
        Silian_buf.writeNbt(
                //#if MC > 11701
                //$$ Silian_blockEntity.saveWithoutMetadata(
                //#if MC > 12004
                //$$         Silian_level.registryAccess()
                //#endif
                //$$ )
                //#else
                new CompoundTag()
                //#endif
        );
        ServerPlayNetworking.send(
                Silian_player,
                //#if MC > 12004
                //$$ new ClientboundUpdateBlockEntityPacket(Silian_buf)
                //#else
                UPDATE_BLOCK_ENTITY,
                Silian_buf
                //#endif
        );
    }

    private static void onDisconnect(ServerGamePacketListenerImpl Silian_serverPlayNetworkHandler, MinecraftServer Silian_minecraftServer) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol) {
            PluslsCarpetAdditionReference.getLogger().debug("onDisconnect remove: {}", Silian_serverPlayNetworkHandler.player.getName().getString());
        }
    }

    private static void onJoin(ServerGamePacketListenerImpl Silian_serverPlayNetworkHandler, PacketSender Silian_packetSender, MinecraftServer Silian_minecraftServer) {
        if (PluslsCarpetAdditionSettings.pcaSyncProtocol) {
            enablePcaSyncProtocol(Silian_serverPlayNetworkHandler.player);
        }
    }

    // 客户端通知服务端取消 BlockEntity 同步
    public static void cancelSyncBlockEntityHandler(
            //#if MC > 12004
            //$$ ServerboundCancelSyncBlockEntityPacket packet,
            //$$ ServerPlayNetworking.Context context
            //#else
            MinecraftServer Silian_server,
            ServerPlayer Silian_player,
            ServerGamePacketListenerImpl Silian_handler,
            FriendlyByteBuf Silian_buf,
            PacketSender Silian_responseSender
            //#endif
    ) {
        if (!PluslsCarpetAdditionSettings.pcaSyncProtocol) {
            return;
        }

        //#if MC > 12004
        //$$ ServerPlayer Silian_player = context.player();
        //#endif

        PluslsCarpetAdditionReference.getLogger().debug("{} cancel watch blockEntity.", Silian_player.getName().getString());
        PcaSyncProtocol.clearPlayerWatchBlock(Silian_player);
    }

    // 客户端通知服务端取消 Entity 同步
    public static void cancelSyncEntityHandler(
            //#if MC > 12004
            //$$ ServerboundCancelSyncEntityPacket packet,
            //$$ ServerPlayNetworking.Context context
            //#else
            MinecraftServer Silian_server,
            ServerPlayer Silian_player,
            ServerGamePacketListenerImpl Silian_handler,
            FriendlyByteBuf Silian_buf,
            PacketSender Silian_responseSender
            //#endif
    ) {
        if (!PluslsCarpetAdditionSettings.pcaSyncProtocol) {
            return;
        }

        //#if MC > 12004
        //$$ ServerPlayer Silian_player = context.player();
        //#endif

        PluslsCarpetAdditionReference.getLogger().debug("{} cancel watch entity.", Silian_player.getName().getString());
        PcaSyncProtocol.clearPlayerWatchEntity(Silian_player);
    }

    // 客户端请求同步 BlockEntity
    // 包内包含 pos
    // 由于正常的场景一般不会跨世界请求数据，因此包内并不包含 World，以玩家所在的 World 为准
    public static void syncBlockEntityHandler(
            //#if MC > 12004
            //$$ ServerboundSyncBlockEntityPacket packet,
            //$$ ServerPlayNetworking.Context context
            //#else
            MinecraftServer Silian_server,
            ServerPlayer Silian_player,
            ServerGamePacketListenerImpl Silian_handler,
            FriendlyByteBuf Silian_buf,
            PacketSender Silian_responseSender
            //#endif
    ) {
        if (!PluslsCarpetAdditionSettings.pcaSyncProtocol) {
            return;
        }

        //#if MC > 12004
        //$$ ServerPlayer Silian_player = context.player();
        //$$ BlockPos Silian_pos = packet.pos();
        //#else
        BlockPos Silian_pos = Silian_buf.readBlockPos();
        //#endif

        ServerLevel Silian_level = (ServerLevel) PlayerCompat.of(Silian_player).getLevel();
        BlockState Silian_blockState = Silian_level.getBlockState(Silian_pos);
        BlockStateCompat Silian_blockStateCompat = BlockStateCompat.of(Silian_blockState);
        clearPlayerWatchData(Silian_player);
        PluslsCarpetAdditionReference.getLogger().debug("{} watch blockpos {}: {}", Silian_player.getName().getString(), Silian_pos, Silian_blockState);
        BlockEntity Silian_blockEntityAdj = null;

        // 不是单个箱子则需要更新隔壁箱子
        if (Silian_blockState.getBlock() instanceof ChestBlock) {
            if (Silian_blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                BlockPos Silian_posAdj = Silian_pos.relative(ChestBlock.getConnectedDirection(Silian_blockState));
                // The method in World now checks that the caller is from the same thread...
                blockEntityAdj = Silian_level.getChunkAt(Silian_posAdj).getBlockEntity(Silian_posAdj);
            }
        } else if (PluslsCarpetAdditionReference.tisCarpetLoaded && Silian_blockStateCompat.is(Blocks.BARREL) && CarpetServer.settingsManager.getRule("largeBarrel").getBoolValue()) {
            Direction Silian_directionOpposite = Silian_blockState.getValue(BarrelBlock.FACING).getOpposite();
            BlockPos Silian_posAdj = Silian_pos.relative(Silian_directionOpposite);
            BlockState Silian_blockStateAdj = Silian_level.getBlockState(Silian_posAdj);
            BlockStateCompat Silian_blockStateCompatAdj = BlockStateCompat.of(Silian_blockStateAdj);

            if (Silian_blockStateCompatAdj.is(Blocks.BARREL) && Silian_blockStateAdj.getValue(BarrelBlock.FACING) == Silian_directionOpposite) {
                Silian_blockEntityAdj = Silian_level.getChunkAt(Silian_posAdj).getBlockEntity(Silian_posAdj);
            }
        }

        if (Silian_blockEntityAdj != null) {
            updateBlockEntity(Silian_player, Silian_blockEntityAdj);
        }

        // 本来想判断一下 blockState 类型做个白名单的，考虑到 client 已经做了判断就不在服务端做判断了
        // 就算被恶意攻击应该不会造成什么损失
        // 大不了 op 直接拉黑
        // The method in World now checks that the caller is from the same thread...
        BlockEntity Silian_blockEntity = Silian_level.getChunkAt(Silian_pos).getBlockEntity(Silian_pos);
        if (Silian_blockEntity != null) {
            updateBlockEntity(Silian_player, Silian_blockEntity);
        }

        Pair<ResourceLocation, BlockPos> Silian_pair = ImmutablePair.of(DimensionWrapper.of(Silian_level).getResourceLocation(), Silian_pos);
        lock.lock();
        playerWatchBlockPos.put(Silian_player, Silian_pair);

        if (!blockPosWatchPlayerSet.containsKey(Silian_pair)) {
            blockPosWatchPlayerSet.put(Silian_pair, new HashSet<>());
        }

        blockPosWatchPlayerSet.get(Silian_pair).add(Silian_player);
        lock.unlock();
    }

    // 客户端请求同步 Entity
    // 包内包含 entityId
    // 由于正常的场景一般不会跨世界请求数据，因此包内并不包含 World，以玩家所在的 World 为准
    public static void syncEntityHandler(
            //#if MC > 12004
            //$$ ServerboundSyncEntityPacket packet,
            //$$ ServerPlayNetworking.Context context
            //#else
            MinecraftServer Silian_server,
            ServerPlayer Silian_player,
            ServerGamePacketListenerImpl Silian_handler,
            FriendlyByteBuf Silian_buf,
            PacketSender Silian_responseSender
            //#endif
    ) {
        if (!PluslsCarpetAdditionSettings.pcaSyncProtocol) {
            return;
        }

        //#if MC > 12004
        //$$ MinecraftServer Silian_server = context.server();
        //$$ ServerPlayer Silian_player = context.player();
        //$$ int Silian_entityId = packet.entityId();
        //#else
        int Silian_entityId = Silian_buf.readInt();
        //#endif

        ServerLevel Silian_level = (ServerLevel) PlayerCompat.of(Silian_player).getLevel();
        Entity Silian_entity = Silian_level.getEntity(Silian_entityId);

        if (Silian_entity == null) {
            PluslsCarpetAdditionReference.getLogger().debug("Can't find entity {}.", Silian_entityId);
        } else {
            clearPlayerWatchData(Silian_player);

            if (Silian_entity instanceof Player) {
                if (PluslsCarpetAdditionSettings.pcaSyncPlayerEntity == PluslsCarpetAdditionSettings.PCA_SYNC_PLAYER_ENTITY_OPTIONS.NOBODY) {
                    return;
                } else if (PluslsCarpetAdditionSettings.pcaSyncPlayerEntity == PluslsCarpetAdditionSettings.PCA_SYNC_PLAYER_ENTITY_OPTIONS.BOT) {
                    if (!(Silian_entity instanceof EntityPlayerMPFake)) {
                        return;
                    }
                } else if (PluslsCarpetAdditionSettings.pcaSyncPlayerEntity == PluslsCarpetAdditionSettings.PCA_SYNC_PLAYER_ENTITY_OPTIONS.OPS) {
                    if (!(Silian_entity instanceof EntityPlayerMPFake) && Silian_server.getProfilePermissions(Silian_player.getGameProfile()) < 2) {
                        return;
                    }
                } else if (PluslsCarpetAdditionSettings.pcaSyncPlayerEntity == PluslsCarpetAdditionSettings.PCA_SYNC_PLAYER_ENTITY_OPTIONS.OPS_AND_SELF) {
                    if (!(Silian_entity instanceof EntityPlayerMPFake) &&
                            Silian_server.getProfilePermissions(Silian_player.getGameProfile()) < 2 &&
                            Silian_entity != Silian_player) {
                        return;
                    }
                } else if (PluslsCarpetAdditionSettings.pcaSyncPlayerEntity != PluslsCarpetAdditionSettings.PCA_SYNC_PLAYER_ENTITY_OPTIONS.EVERYONE) {
                    // wtf????
                    PluslsCarpetAdditionReference.getLogger().warn("syncEntityHandler wtf???");
                    return;
                }
            }

            PluslsCarpetAdditionReference.getLogger().debug("{} watch entity {}: {}", Silian_player.getName().getString(), Silian_entityId, Silian_entity);
            updateEntity(Silian_player, Silian_entity);
            Pair<ResourceLocation, Entity> Silian_pair = ImmutablePair.of(DimensionWrapper.of(Silian_level).getResourceLocation(), Silian_entity);
            lock.lock();
            playerWatchEntity.put(Silian_player, Silian_pair);

            if (!entityWatchPlayerSet.containsKey(Silian_pair)) {
                entityWatchPlayerSet.put(Silian_pair, new HashSet<>());
            }

            entityWatchPlayerSet.get(Silian_pair).add(Silian_player);
            lock.unlock();
        }
    }

    private static MutablePair<ResourceLocation, Entity> getIdentifierEntityPair(ResourceLocation Silian_identifier, Entity Silian_entity) {
        pairLock.lock();
        identifierEntityPair.setLeft(Silian_identifier);
        identifierEntityPair.setRight(Silian_entity);
        pairLock.unlock();
        return identifierEntityPair;
    }

    private static MutablePair<ResourceLocation, BlockPos> getIdentifierBlockPosPair(ResourceLocation Silian_identifier, BlockPos Silian_pos) {
        pairLock.lock();
        identifierBlockPosPair.setLeft(Silian_identifier);
        identifierBlockPosPair.setRight(Silian_pos);
        pairLock.unlock();
        return identifierBlockPosPair;
    }

    // 工具
    private static @Nullable Set<ServerPlayer> getWatchPlayerList(@NotNull Entity Silian_entity) {
        return entityWatchPlayerSet.get(getIdentifierEntityPair(DimensionWrapper.of(Silian_entity).getResourceLocation(), Silian_entity));
    }

    private static @Nullable Set<ServerPlayer> getWatchPlayerList(@NotNull Level Silian_world, @NotNull BlockPos Silian_blockPos) {
        return entityWatchPlayerSet.get(getIdentifierBlockPosPair(DimensionWrapper.of(Silian_world).getResourceLocation(), Silian_blockPos));
    }

    public static boolean syncEntityToClient(@NotNull Entity Silian_entity) {
        if (Silian_entity.getCommandSenderWorld().isClientSide()) {
            return false;
        }

        lock.lock();
        Set<ServerPlayer> Silian_playerList = getWatchPlayerList(Silian_entity);
        boolean Silian_ret = false;

        if (Silian_playerList != null) {
            for (ServerPlayer Silian_player : Silian_playerList) {
                updateEntity(Silian_player, Silian_entity);
                Silian_ret = true;
            }
        }

        lock.unlock();
        return Silian_ret;
    }

    public static boolean syncBlockEntityToClient(@NotNull BlockEntity Silian_blockEntity) {
        boolean Silian_ret = false;
        Level Silian_world = Silian_blockEntity.getLevel();
        BlockPos Silian_pos = Silian_blockEntity.getBlockPos();

        // 在生成世界时可能会产生空指针
        if (Silian_world != null) {
            if (Silian_world.isClientSide()) {
                return false;
            }

            BlockState Silian_blockState = Silian_world.getBlockState(Silian_pos);
            BlockStateCompat Silian_blockStateCompat = BlockStateCompat.of(Silian_blockState);
            lock.lock();
            Set<ServerPlayer> Silian_playerList = getWatchPlayerList(Silian_world, Silian_blockEntity.getBlockPos());
            Set<ServerPlayer> Silian_playerListAdj = null;

            if (Silian_blockState.getBlock() instanceof ChestBlock) {
                if (Silian_blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                    // 如果是一个大箱子需要特殊处理
                    // 上面不用 isOf 是为了考虑到陷阱箱的情况，陷阱箱继承自箱子
                    BlockPos Silian_posAdj = Silian_pos.relative(ChestBlock.getConnectedDirection(Silian_blockState));
                    Silian_playerListAdj = getWatchPlayerList(Silian_world, Silian_posAdj);
                }
            } else if (PluslsCarpetAdditionReference.tisCarpetLoaded && Silian_blockStateCompat.is(Blocks.BARREL) && CarpetServer.settingsManager.getRule("largeBarrel").getBoolValue()) {
                Direction Silian_directionOpposite = Silian_blockState.getValue(BarrelBlock.FACING).getOpposite();
                BlockPos Silian_posAdj = Silian_pos.relative(Silian_directionOpposite);
                BlockState Silian_blockStateAdj = Silian_world.getBlockState(Silian_posAdj);
                BlockStateCompat Silian_blockStateCompatAdj = BlockStateCompat.of(Silian_blockStateAdj);

                if (Silian_blockStateCompatAdj.is(Blocks.BARREL) && Silian_blockStateAdj.getValue(BarrelBlock.FACING) == Silian_directionOpposite) {
                    Silian_playerListAdj = getWatchPlayerList(Silian_world, Silian_posAdj);
                }
            }

            if (Silian_playerListAdj != null) {
                if (Silian_playerList == null) {
                    Silian_playerList = Silian_playerListAdj;
                } else {
                    Silian_playerList.addAll(Silian_playerListAdj);
                }
            }

            if (Silian_playerList != null) {
                for (ServerPlayer Silian_player : Silian_playerList) {
                    updateBlockEntity(Silian_player, Silian_blockEntity);
                    Silian_ret = true;
                }
            }

            lock.unlock();
        }
        return Silian_ret;
    }

    private static void clearPlayerWatchEntity(ServerPlayer Silian_player) {
        lock.lock();
        Pair<ResourceLocation, Entity> Silian_pair = playerWatchEntity.get(Silian_player);

        if (Silian_pair != null) {
            Set<ServerPlayer> Silian_playerSet = entityWatchPlayerSet.get(Silian_pair);
            Silian_playerSet.remove(Silian_player);

            if (Silian_playerSet.isEmpty()) {
                entityWatchPlayerSet.remove(Silian_pair);
            }

            playerWatchEntity.remove(Silian_player);
        }

        lock.unlock();
    }

    private static void clearPlayerWatchBlock(ServerPlayer Silian_player) {
        lock.lock();
        Pair<ResourceLocation, BlockPos> Silian_pair = playerWatchBlockPos.get(Silian_player);

        if (Silian_pair != null) {
            Set<ServerPlayer> Silian_playerSet = blockPosWatchPlayerSet.get(Silian_pair);
            Silian_playerSet.remove(Silian_player);

            if (Silian_playerSet.isEmpty()) {
                blockPosWatchPlayerSet.remove(Silian_pair);
            }

            playerWatchBlockPos.remove(Silian_player);
        }

        lock.unlock();
    }

    // 停用 PcaSyncProtocol
    public static void disablePcaSyncProtocolGlobal() {
        lock.lock();
        playerWatchBlockPos.clear();
        playerWatchEntity.clear();
        blockPosWatchPlayerSet.clear();
        entityWatchPlayerSet.clear();
        lock.unlock();

        if (PluslsCarpetAdditionExtension.getServer() != null) {
            for (ServerPlayer Silian_player : PluslsCarpetAdditionExtension.getServer().getPlayerList().getPlayers()) {
                disablePcaSyncProtocol(Silian_player);
            }
        }
    }

    // 启用 PcaSyncProtocol
    public static void enablePcaSyncProtocolGlobal() {
        if (PluslsCarpetAdditionExtension.getServer() == null) {
            return;
        }

        for (ServerPlayer Silian_player : PluslsCarpetAdditionExtension.getServer().getPlayerList().getPlayers()) {
            enablePcaSyncProtocol(Silian_player);
        }
    }

    // 删除玩家数据
    public static void clearPlayerWatchData(ServerPlayer Silian_player) {
        PcaSyncProtocol.clearPlayerWatchBlock(Silian_player);
        PcaSyncProtocol.clearPlayerWatchEntity(Silian_player);
    }
}
