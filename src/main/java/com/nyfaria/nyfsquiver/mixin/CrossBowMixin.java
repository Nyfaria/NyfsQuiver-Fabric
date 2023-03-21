package com.nyfaria.nyfsquiver.mixin;

import com.nyfaria.nyfsquiver.item.QuiverItem;
import com.nyfaria.nyfsquiver.ui.QuiverScreenHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossBowMixin {

    @Inject(at = @At("TAIL"), method = "loadProjectile", cancellable = true)
    private static void loadFromQuiver(LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl, boolean bl2, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity instanceof Player player) {
            ItemStack quiverStack = QuiverItem.getEquippedQuiver(player);
            if (quiverStack.isEmpty()) return;
            QuiverScreenHandler quiverContainer = new QuiverScreenHandler(0, player.getInventory(), quiverStack);
            if (!quiverContainer.getSlot(quiverStack.getOrCreateTag().getInt("current_slot")).getItem().isEmpty()) {
                quiverContainer.getSlot(quiverStack.getOrCreateTag().getInt("current_slot")).getItem().shrink(1);
                quiverContainer.getSlot(quiverStack.getOrCreateTag().getInt("current_slot")).setChanged();
            }
        }
    }
}

