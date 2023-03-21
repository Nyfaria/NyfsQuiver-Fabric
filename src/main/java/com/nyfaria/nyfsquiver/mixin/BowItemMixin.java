package com.nyfaria.nyfsquiver.mixin;

import com.nyfaria.nyfsquiver.item.QuiverItem;
import com.nyfaria.nyfsquiver.ui.QuiverScreenHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public class BowItemMixin {

    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", shift = At.Shift.AFTER))
    public void boop(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo c) {
        ItemStack quiverStack = QuiverItem.getEquippedQuiver((Player) livingEntity);
        if (quiverStack.isEmpty()) return;
        QuiverScreenHandler quiverContainer = new QuiverScreenHandler(0, ((Player) livingEntity).getInventory(), quiverStack);
        int currentSlot = quiverStack.getOrCreateTag().getInt("current_slot");
        if (!quiverContainer.getSlot(currentSlot).getItem().isEmpty()) {
            quiverContainer.getSlot(currentSlot).getItem().shrink(1);
            quiverContainer.getSlot(currentSlot).setChanged();
        }
    }
}

