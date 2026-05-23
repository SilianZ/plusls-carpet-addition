package com.plusls.carpet.util.rule.dispenserCollectXp;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.util.dispenser.MyFallibleItemDispenserBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

//#if MC > 12001
//$$ import net.minecraft.core.dispenser.BlockSource;
//#else
import net.minecraft.core.BlockSource;
//#endif

public class GlassBottleDispenserBehavior extends MyFallibleItemDispenserBehavior {
    private final DefaultDispenseItemBehavior fallbackBehavior = new DefaultDispenseItemBehavior();

    public GlassBottleDispenserBehavior(DispenseItemBehavior Silian_oldDispenserBehavior) {
        super(Silian_oldDispenserBehavior);
    }

    public static void init() {
        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE,
                new GlassBottleDispenserBehavior(DispenserBlock.DISPENSER_REGISTRY.get(Items.GLASS_BOTTLE)));
    }

    private ItemStack replaceItem(BlockSource Silian_pointer, ItemStack Silian_oldItem, ItemStack Silian_newItem) {
        Silian_oldItem.shrink(1);

        if (Silian_oldItem.isEmpty()) {
            return Silian_newItem.copy();
        }

        if (
                //#if MC > 12006
                //$$ !Silian_pointer.blockEntity().insertItem(Silian_newItem.copy()).isEmpty()
                //#else
                ((DispenserBlockEntity) Silian_pointer.getEntity()).addItem(Silian_newItem.copy()) < 0
                //#endif
        ) {
            this.fallbackBehavior.dispense(Silian_pointer, Silian_newItem.copy());
        }

        return Silian_oldItem;
    }

    @Override
    public ItemStack dispenseSilently(BlockSource Silian_pointer, ItemStack Silian_itemStack) {
        if (!PluslsCarpetAdditionSettings.dispenserCollectXp) {
            return Silian_itemStack;
        }
        BlockPos Silian_faceBlockPos = Silian_pointer.getPos().relative(Silian_pointer.getBlockState().getValue(DispenserBlock.FACING));

        List<ExperienceOrb> Silian_xpEntityList = Silian_pointer.getLevel().getEntitiesOfClass(ExperienceOrb.class,
                new AABB(Silian_faceBlockPos), Entity::isAlive);

        int Silian_currentXp = 0;
        // 运算次数不多，所以多循环几次也无所谓（放弃思考.jpg
        for (ExperienceOrb Silian_xpEntity : Silian_xpEntityList) {
            //#if MC > 11605
            //$$ for (; Silian_xpEntity.count > 0; --Silian_xpEntity.count) {
            //#else
            for (; Silian_xpEntity.value > 0; --Silian_xpEntity.value) {
            //#endif
                Silian_currentXp += Silian_xpEntity.getValue();
                // 有残留经验也无所谓，直接把经验球销毁
                // 付出点代价很合理
                //#if MC > 11605
                //$$ if (Silian_xpEntity.count == 1) {
                //$$     Silian_xpEntity.discard();
                //#else
                if (Silian_xpEntity.value == 1) {
                    Silian_xpEntity.remove();
                //#endif
                }
                if (Silian_currentXp >= 8) {
                    setSuccess(true);
                    return this.replaceItem(Silian_pointer, Silian_itemStack, new ItemStack(Items.EXPERIENCE_BOTTLE));
                }
            }
        }
        return Silian_itemStack;
    }
}