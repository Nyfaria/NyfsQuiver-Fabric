package com.nyfaria.nyfsquiver.mixin;

import com.nyfaria.nyfsquiver.item.QuiverItem;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(ArrowItem.class)
public class ArrowItemMixin {
    @Inject(method = "createArrow", at = @At("HEAD"), cancellable = true)
    public void customEntity(Level pLevel, ItemStack pStack, LivingEntity pShooter, CallbackInfoReturnable<AbstractArrow> cir) {
        if (pShooter instanceof Player) {
            Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(pShooter);
            if (trinketComponent.isPresent()) {
                List<Tuple<SlotReference, ItemStack>> equippedTrinkets = trinketComponent.get().getAllEquipped();

                for (Tuple<SlotReference, ItemStack> trinket : equippedTrinkets) {
                    ItemStack quiverItemStack = trinket.getB();
                    if (quiverItemStack.getItem() instanceof QuiverItem) {

                        Arrow arrow = new Arrow(pLevel, pShooter);
                        arrow.setEffectsFromItem(pStack);
                        cir.setReturnValue(((QuiverItem) quiverItemStack.getItem()).modifyArrow(arrow));

                    }
                }
            }
        }
    }
}
