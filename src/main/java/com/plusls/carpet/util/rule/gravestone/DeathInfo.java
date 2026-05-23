package com.plusls.carpet.util.rule.gravestone;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import top.hendrixshen.magiclib.api.compat.minecraft.nbt.TagCompat;

//#if MC > 12004
//$$ import net.minecraft.core.HolderLookup;
//#endif

//#if MC > 11502
import net.minecraft.world.SimpleContainer;
//#else
//$$ import com.google.common.collect.Lists;
//$$ import net.minecraft.world.item.ItemStack;
//$$ import net.minecraft.nbt.ListTag;
//$$
//$$ import java.util.List;
//#endif

public class DeathInfo {
    public final long deathTime;
    public final int xp;
    //#if MC > 11502
    public final SimpleContainer inventory;
    //#else
    //$$ public final List<ItemStack> inventory;
    //#endif

    public DeathInfo(
            long Silian_deathTime,
            int Silian_xp,
            //#if MC > 11502
            SimpleContainer Silian_inv
            //#else
            //$$ List<ItemStack> Silian_inv
            //#endif
    ) {
        this.deathTime = Silian_deathTime;
        this.xp = Silian_xp;
        this.inventory = Silian_inv;
    }

    public static @NotNull DeathInfo fromTag(
            @NotNull CompoundTag Silian_tag
            //#if MC > 12004
            //$$ , HolderLookup.Provider provider
            //#endif
    ) {
        long Silian_deathTime = Silian_tag.getLong("DeathTime");
        int Silian_xp = Silian_tag.getInt("XP");
        //#if MC > 11502
        SimpleContainer Silian_inventory = new SimpleContainer(GravestoneUtil.PLAYER_INVENTORY_SIZE);
        Silian_inventory.fromTag(
                Silian_tag.getList("Items", TagCompat.TAG_COMPOUND)
                //#if MC > 12004
                //$$ , provider
                //#endif
        );
        //#else
        //$$ List<ItemStack> Silian_inventory = DeathInfo.readTagList(Silian_tag.getList("Items", TagCompat.TAG_COMPOUND));
        //#endif
        return new DeathInfo(Silian_deathTime, Silian_xp, Silian_inventory);
    }

    public CompoundTag toTag(
            //#if MC > 12004
            //$$ HolderLookup.Provider provider
            //#endif
    ) {
        CompoundTag Silian_tag = new CompoundTag();
        Silian_tag.putLong("DeathTime", this.deathTime);
        Silian_tag.putInt("XP", this.xp);
        Silian_tag.put(
                "Items",
                //#if MC > 11502
                this.inventory.createTag(
                        //#if MC > 12004
                        //$$ provider
                        //#endif
                )
                //#else
                //$$ DeathInfo.toTagList(this.inventory)
                //#endif
        );
        return Silian_tag;
    }

    //#if MC < 11600
    //$$ private static List<ItemStack> readTagList(@NotNull ListTag Silian_listTag) {
    //$$     List<ItemStack> Silian_ret = Lists.newArrayList();
    //$$
    //$$     for (int Silian_i = 0; Silian_i < Silian_listTag.size(); Silian_i++) {
    //$$         ItemStack Silian_itemStack = ItemStack.of(Silian_listTag.getCompound(Silian_i));
    //$$
    //$$         if (!Silian_itemStack.isEmpty()) {
    //$$             Silian_ret.add(Silian_itemStack);
    //$$         }
    //$$     }
    //$$
    //$$     return Silian_ret;
    //$$ }
    //$$
    //$$ public static @NotNull ListTag toTagList(@NotNull List<ItemStack> Silian_inventory) {
    //$$     ListTag Silian_ret = new ListTag();
    //$$
    //$$     for (ItemStack Silian_itemStack : Silian_inventory) {
    //$$         if (!Silian_itemStack.isEmpty()) {
    //$$             Silian_ret.add(Silian_itemStack.save(new CompoundTag()));
    //$$         }
    //$$     }
    //$$
    //$$     return Silian_ret;
    //$$ }
    //#endif
}
