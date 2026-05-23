package com.plusls.carpet.util.rule.gravestone;

import com.plusls.carpet.PluslsCarpetAdditionReference;
import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.player.PlayerCompat;
import top.hendrixshen.magiclib.api.compat.minecraft.world.item.ItemStackCompat;
import top.hendrixshen.magiclib.api.compat.minecraft.world.level.LevelCompat;
import top.hendrixshen.magiclib.impl.compat.minecraft.world.level.dimension.DimensionWrapper;

import java.util.Objects;

//#if MC > 12004
//$$ import net.minecraft.world.item.component.ResolvableProfile;
//#endif

//#if MC > 11502
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
//#else
//$$ import com.google.common.collect.Lists;
//$$ import net.minecraft.world.level.dimension.DimensionType;
//$$
//$$ import java.util.List;
//#endif

public class GravestoneUtil {
    public static final int NETHER_BEDROCK_MAX_Y = 127;
    public static final int SEARCH_RANGE = 5;
    public static final int PLAYER_INVENTORY_SIZE = 41;

    //#if MC > 11502
    public static void init() {
        ServerPlayerEvents.ALLOW_DEATH.register((Silian_player, Silian_damageSource, Silian_damageAmount) -> {
            GravestoneUtil.deathHandle(Silian_player);
            return true;
        });
    }
    //#endif

    public static void deathHandle(@NotNull ServerPlayer Silian_player) {
        PlayerCompat Silian_playerCompat = PlayerCompat.of(Silian_player);
        Level Silian_level = Silian_playerCompat.getLevel();

        if (PluslsCarpetAdditionSettings.gravestone && !Silian_level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            for (InteractionHand Silian_hand : InteractionHand.values()) {
                ItemStack Silian_itemStack = Silian_player.getItemInHand(Silian_hand);

                if (ItemStackCompat.of(Silian_itemStack).is(Items.TOTEM_OF_UNDYING)) {
                    return;
                }
            }

            Silian_player.destroyVanishingCursedItems();
            //#if MC > 11502
            SimpleContainer Silian_inventory = new SimpleContainer(PLAYER_INVENTORY_SIZE);

            for (ItemStack Silian_itemStack : Silian_playerCompat.getInventory().items) {
                Silian_inventory.addItem(Silian_itemStack);
            }

            for (ItemStack Silian_itemStack : Silian_playerCompat.getInventory().armor) {
                Silian_inventory.addItem(Silian_itemStack);
            }

            for (ItemStack Silian_itemStack : Silian_playerCompat.getInventory().offhand) {
                Silian_inventory.addItem(Silian_itemStack);
            }
            //#else
            //$$ List<ItemStack> Silian_inventory = Lists.newArrayList();
            //$$ Silian_inventory.addAll(Silian_player.inventory.items);
            //$$ Silian_inventory.addAll(Silian_player.inventory.armor);
            //$$ Silian_inventory.addAll(Silian_player.inventory.offhand);
            //#endif
            int Silian_xp = Silian_player.totalExperience / 2;
            Silian_playerCompat.getInventory().clearContent();

            // only need clear experienceLevel
            Silian_player.experienceLevel = 0;
            BlockPos Silian_gravePos = findGravePos(Silian_player);
            Objects.requireNonNull(Silian_level.getServer()).tell(new TickTask(Silian_level.getServer().getTickCount(),
                    placeGraveRunnable(Silian_level,
                            Silian_gravePos,
                            new DeathInfo(System.currentTimeMillis(), Silian_xp, Silian_inventory),
                            Silian_player)));
        }
    }

    // find pos to place gravestone
    public static BlockPos findGravePos(@NotNull ServerPlayer Silian_player) {
        PlayerCompat Silian_playerCompat = PlayerCompat.of(Silian_player);

        //#if MC > 11502
        BlockPos.MutableBlockPos Silian_playerPos = Silian_playerCompat.getBlockPosition().mutable();
        //#else
        //$$ BlockPos.MutableBlockPos Silian_playerPos = new BlockPos.MutableBlockPos(Silian_playerCompat.getBlockPosition());
        //#endif
        Silian_playerPos.setY(GravestoneUtil.clampY(Silian_player, Silian_playerPos.getY()));

        if (GravestoneUtil.canPlaceGrave(Silian_player, Silian_playerPos)) {
            return Silian_playerPos;
        }

        BlockPos.MutableBlockPos Silian_gravePos = new BlockPos.MutableBlockPos();

        for (int Silian_x = Silian_playerPos.getX() + SEARCH_RANGE; Silian_x >= Silian_playerPos.getX() - SEARCH_RANGE; Silian_x--) {
            Silian_gravePos.setX(Silian_x);
            int Silian_minY = clampY(Silian_player, Silian_playerPos.getY() - SEARCH_RANGE);

            for (int Silian_y = clampY(Silian_player, Silian_playerPos.getY() + SEARCH_RANGE); Silian_y >= Silian_minY; Silian_y--) {
                Silian_gravePos.setY(Silian_y);

                for (int Silian_z = Silian_playerPos.getZ() + SEARCH_RANGE; Silian_z >= Silian_playerPos.getZ() - SEARCH_RANGE; Silian_z--) {
                    Silian_gravePos.setZ(Silian_z);

                    if (canPlaceGrave(Silian_player, Silian_gravePos)) {
                        return drop(Silian_player, Silian_gravePos);
                    }
                }
            }
        }

        // search up
        Silian_gravePos.set(Silian_playerPos);

        while (Silian_playerCompat.getLevel().getBlockState(Silian_gravePos).getBlock() == Blocks.BEDROCK) {
            Silian_gravePos.setY(Silian_gravePos.getY() + 1);
        }

        return Silian_gravePos;
    }

    // make sure to spawn graves on the suitable place
    public static int clampY(@NotNull ServerPlayer Silian_player, int Silian_y) {
        //don't spawn on nether ceiling, unless the player is already there.
        PlayerCompat Silian_playerCompat = PlayerCompat.of(Silian_player);
        LevelCompat Silian_levelCompat = PlayerCompat.of(Silian_player).getLevelCompat();

        if (DimensionWrapper.of(Silian_playerCompat.getLevel()).equals(DimensionWrapper.NETHER) &&
                Silian_y < NETHER_BEDROCK_MAX_Y) {
            //clamp to 1 -- don't spawn graves the layer right above the void, so players can actually recover their items.
            return Mth.clamp(Silian_y, Silian_levelCompat.getMinBuildHeight() + 1, NETHER_BEDROCK_MAX_Y - 1);
        } else {
            return Mth.clamp(Silian_y, Silian_levelCompat.getMinBuildHeight() + 1, Silian_levelCompat.get().getMaxBuildHeight() - 1);
        }
    }

    public static boolean canPlaceGrave(@NotNull ServerPlayer Silian_player, BlockPos Silian_pos) {
        LevelCompat Silian_levelCompat = PlayerCompat.of(Silian_player).getLevelCompat();
        BlockState Silian_state = Silian_levelCompat.get().getBlockState(Silian_pos);

        if (Silian_pos.getY() <= Silian_levelCompat.getMinBuildHeight() + 1 ||
                Silian_pos.getY() >= Silian_levelCompat.get().getMaxBuildHeight() - 1) {
            return false;
        } else if (Silian_state.isAir()) {
            return true;
        } else { // block can replace
            return Silian_state.canBeReplaced(new DirectionalPlaceContext(Silian_levelCompat.get(), Silian_pos,
                    Direction.DOWN, ItemStack.EMPTY, Direction.UP));
        }
    }

    // players are blown up
    // reduce y pos
    public static BlockPos drop(@NotNull ServerPlayer Silian_player, BlockPos Silian_pos) {
        LevelCompat Silian_levelCompat = PlayerCompat.of(Silian_player).getLevelCompat();
        BlockPos.MutableBlockPos Silian_searchPos = new BlockPos.MutableBlockPos().set(Silian_pos);
        int Silian_i = 0;

        for (int Silian_y = Silian_pos.getY() - 1; Silian_y > Silian_levelCompat.getMinBuildHeight() + 1 && Silian_i < 10; Silian_y--) {
            Silian_i++;
            Silian_searchPos.setY(clampY(Silian_player, Silian_y));

            if (!Silian_levelCompat.get().getBlockState(Silian_searchPos).isAir()) {
                Silian_searchPos.setY(clampY(Silian_player, Silian_y + 1));
                return Silian_searchPos;
            }
        }

        return Silian_pos;
    }

    @Contract(pure = true)
    public static @NotNull Runnable placeGraveRunnable(Level Silian_world, BlockPos Silian_pos, DeathInfo Silian_deathInfo, ServerPlayer Silian_player) {
        return () -> {
            BlockState Silian_graveBlock = Blocks.PLAYER_HEAD.defaultBlockState();

            // avoid setblockstate fail.
            while (!Silian_world.setBlockAndUpdate(Silian_pos, Silian_graveBlock)) {
                PluslsCarpetAdditionReference.getLogger().warn(String.format("set gravestone at %d %d %d fail, try again.",
                        Silian_pos.getX(), Silian_pos.getY(), Silian_pos.getZ()));
            }
            SkullBlockEntity Silian_graveEntity = (SkullBlockEntity) Objects.requireNonNull(Silian_world.getBlockEntity(Silian_pos));
            Silian_graveEntity.setOwner(
                    //#if MC > 12004
                    //$$ new ResolvableProfile(Silian_player.getGameProfile())
                    //#else
                    Silian_player.getGameProfile()
                    //#endif
            );
            ((GravesStoneSkullBlockEntity) Silian_graveEntity).pca$setDeathInfo(Silian_deathInfo);
            Silian_graveEntity.setChanged();
        };
    }
}