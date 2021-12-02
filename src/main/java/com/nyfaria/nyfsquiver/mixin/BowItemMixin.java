package com.nyfaria.nyfsquiver.mixin;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.nyfaria.nyfsquiver.item.QuiverItem;
import com.nyfaria.nyfsquiver.ui.QuiverScreenHandler;

import java.util.List;
import java.util.Optional;

@Mixin(BowItem.class)
public class BowItemMixin {

    @Inject(method = "releaseUsing", at = @At(value="TAIL",shift = At.Shift.BEFORE),cancellable = true)
    public void boop(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo c) {
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(livingEntity);
        if(component.isPresent() &&  !(((Player)livingEntity).getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, itemStack) > 0)) {
            List<Tuple<SlotReference, ItemStack>> allEquipped = component.get().getAllEquipped();
            for (Tuple<SlotReference, ItemStack> entry : allEquipped) {
                ItemStack beep = entry.getB();
                if (entry.getB().getItem() instanceof QuiverItem) {
                    QuiverScreenHandler quiverContainer = new QuiverScreenHandler(0, ((Player) livingEntity).getInventory(), beep);
                    if (!quiverContainer.getSlot(beep.getOrCreateTag().getInt("current_slot")).getItem().isEmpty()) {
                        quiverContainer.getSlot(beep.getOrCreateTag().getInt("current_slot")).getItem().shrink(1);
                        quiverContainer.getSlot(beep.getOrCreateTag().getInt("current_slot")).setChanged();
                    }
                }
            }
        }
    }
}
