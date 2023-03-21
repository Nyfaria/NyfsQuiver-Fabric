package com.nyfaria.nyfsquiver.mixin;

import com.nyfaria.nyfsquiver.config.QuiverInfo;
import com.nyfaria.nyfsquiver.item.QuiverItem;
import com.nyfaria.nyfsquiver.ui.ExtendedSimpleContainer;
import com.nyfaria.nyfsquiver.util.InventoryUtils;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Player.class)
public class PlayerEntityMixin {

    @Inject(method = "getProjectile", at = @At(value = "HEAD"), cancellable = true)
    public void getProjectile(ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        if (!(itemStack.getItem() instanceof ProjectileWeaponItem)) {
            cir.setReturnValue(ItemStack.EMPTY);
        } else {

            Predicate<ItemStack> predicate = ((ProjectileWeaponItem) itemStack.getItem()).getSupportedHeldProjectiles();
            ItemStack equippedQuiver = QuiverItem.getEquippedQuiver((Player) (Object) this);
            if(equippedQuiver.isEmpty()) return;
            QuiverInfo meow = ((QuiverItem) equippedQuiver.getItem()).getTier();
            ListTag tag = equippedQuiver.getOrCreateTag().getList("Inventory", NbtType.COMPOUND);

            SimpleContainer inventory = new ExtendedSimpleContainer(equippedQuiver, meow.getRowWidth() * meow.getNumberOfRows());

            InventoryUtils.fromTag(tag, inventory);

            ItemStack itemStack4 = inventory.getItem(equippedQuiver.getOrCreateTag().getInt("current_slot"));

            if (predicate.test(itemStack4)) {
                cir.setReturnValue(itemStack4);
            }

        }
    }
}
