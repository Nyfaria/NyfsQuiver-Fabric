package thenyfaria.nyfsquivers.mixin;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thenyfaria.nyfsquivers.config.QuiverInfo;
import thenyfaria.nyfsquivers.item.QuiverItem;
import thenyfaria.nyfsquivers.ui.ExtendedSimpleContainer;
import thenyfaria.nyfsquivers.util.InventoryUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(Player.class)
public class PlayerEntityMixin {

    @Inject(method = "getProjectile", at = @At(value="HEAD"),cancellable = true)
    public void getProjectile(ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        if (!(itemStack.getItem() instanceof ProjectileWeaponItem)) {
            cir.setReturnValue(ItemStack.EMPTY);
        } else {

            Predicate<ItemStack> predicate = ((ProjectileWeaponItem)itemStack.getItem()).getSupportedHeldProjectiles();
            Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent((Player)(Object)this);
            if(component.isPresent()) {
                List<Tuple<SlotReference, ItemStack>> allEquipped = component.get().getAllEquipped();
                for (Tuple<SlotReference, ItemStack> entry : allEquipped) {
                    ItemStack beep = entry.getB();
                    if (entry.getB().getItem() instanceof QuiverItem) {
                        QuiverInfo meow = ((QuiverItem)beep.getItem()).getTier();
                        ListTag tag = beep.getOrCreateTag().getList("Inventory", NbtType.COMPOUND);

                        SimpleContainer inventory = new ExtendedSimpleContainer(beep, meow.getRowWidth() * meow.getNumberOfRows());

                        InventoryUtils.fromTag(tag, inventory);

                        ItemStack itemStack4 = inventory.getItem(entry.getB().getOrCreateTag().getInt("current_slot"));

                        if (predicate.test(itemStack4)) {
                            cir.setReturnValue(itemStack4);
                        }
                    }
                }
            }
        }
    }
}
