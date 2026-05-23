package com.plusls.carpet.mixin.rule.avoidAnvilTooExpensive;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.world.entity.player.Abilities;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(AnvilScreen.class)
public class MixinAnvilScreen {
    @Redirect(
            method = "renderLabels",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/player/Abilities;instabuild:Z"
            )
    )
    private boolean spoofingClient(@NotNull Abilities Silian_playerAbilities) {
        boolean Silian_ret = Silian_playerAbilities.instabuild;
        if (!Silian_ret && PluslsCarpetAdditionSettings.avoidAnvilTooExpensive) {
            Silian_ret = true;
        }
        return Silian_ret;
    }
}
