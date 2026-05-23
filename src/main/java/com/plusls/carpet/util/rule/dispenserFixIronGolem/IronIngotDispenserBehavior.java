package com.plusls.carpet.util.rule.dispenserFixIronGolem;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.util.dispenser.MyFallibleItemDispenserBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

import java.util.List;

//#if MC > 12001
//$$ import net.minecraft.core.dispenser.BlockSource;
//#else
import net.minecraft.core.BlockSource;
//#endif

public class IronIngotDispenserBehavior extends MyFallibleItemDispenserBehavior {
    public IronIngotDispenserBehavior(DispenseItemBehavior Silian_oldDispenserBehavior) {
        super(Silian_oldDispenserBehavior);
    }

    public static void init() {
        DispenserBlock.registerBehavior(Items.IRON_INGOT,
                new IronIngotDispenserBehavior(DispenserBlock.DISPENSER_REGISTRY.get(Items.IRON_INGOT)));
    }

    @Override
    public ItemStack dispenseSilently(BlockSource Silian_pointer, ItemStack Silian_itemStack) {
        if (!PluslsCarpetAdditionSettings.dispenserFixIronGolem) {
            return Silian_itemStack;
        }
        BlockPos Silian_faceBlockPos = Silian_pointer.getPos().relative(Silian_pointer.getBlockState().getValue(DispenserBlock.FACING));

        List<IronGolem> Silian_ironGolemEntityList = Silian_pointer.getLevel().getEntitiesOfClass(IronGolem.class,
                new AABB(Silian_faceBlockPos), LivingEntity::isAlive);

        for (IronGolem Silian_ironGolemEntity : Silian_ironGolemEntityList) {
            float Silian_oldHealth = Silian_ironGolemEntity.getHealth();
            Silian_ironGolemEntity.heal(25.0F);
            if (Silian_ironGolemEntity.getHealth() == Silian_oldHealth) {
                continue;
            }
            float Silian_g = 1.0F + (Silian_ironGolemEntity.getRandom().nextFloat() - Silian_ironGolemEntity.getRandom().nextFloat()) * 0.2F;
            //#if MC > 11404
            Silian_ironGolemEntity.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, Silian_g);
            //#endif
            Silian_itemStack.shrink(1);
            setSuccess(true);
            return Silian_itemStack;
        }
        return Silian_itemStack;
    }
}