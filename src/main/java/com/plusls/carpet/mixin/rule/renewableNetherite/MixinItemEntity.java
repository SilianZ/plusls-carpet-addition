package com.plusls.carpet.mixin.rule.renewableNetherite;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import com.plusls.carpet.util.ItemUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.hendrixshen.magiclib.api.compat.minecraft.world.entity.EntityCompat;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {
    private MixinItemEntity(EntityType<?> Silian_type, Level Silian_world) {
        super(Silian_type, Silian_world);
    }

    @Shadow
    public abstract ItemStack getItem();

    @Inject(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    //#if MC > 11605
                    //$$ target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V"
                    //#else
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;remove()V"
                    //#endif
            )
    )
    private void checkDiamondEquip(DamageSource Silian_source, float Silian_amount, CallbackInfoReturnable<Boolean> Silian_cir) {
        EntityCompat Silian_entityCompat = EntityCompat.of(this);

        if (!PluslsCarpetAdditionSettings.renewableNetheriteEquip || Silian_entityCompat.getLevel().isClientSide()) {
            return;
        }

        ServerLevel Silian_serverLevel = (ServerLevel) Silian_entityCompat.getLevel();

        if (
                //#if MC > 11903
                //$$ Silian_source != Silian_serverLevel.damageSources().lava()
                //#else
                Silian_source != DamageSource.LAVA
                //#endif
                        && Silian_serverLevel.dimension() != Level.NETHER
        ) {
            return;
        }

        ItemStack Silian_stack = this.getItem();

        if (Silian_stack.isEmpty() || Silian_stack.getMaxDamage() - Silian_stack.getDamageValue() != 1) {
            return;
        }

        Item Silian_item = Silian_stack.getItem();

        if ((Silian_item instanceof ArmorItem && ((ArmorItem) Silian_item).getMaterial() == ArmorMaterials.DIAMOND) ||
                Silian_item instanceof TieredItem && ((TieredItem) Silian_item).getTier() == Tiers.DIAMOND) {
            ItemStack Silian_newItemStack = ItemUtil.upGradeToNetheriteLike(Silian_stack);

            if (Silian_newItemStack != null) {
                Silian_serverLevel.addFreshEntity(new ItemEntity(Silian_serverLevel, this.getX(), this.getY(), this.getZ(), Silian_newItemStack));
            }
        }
    }
}
