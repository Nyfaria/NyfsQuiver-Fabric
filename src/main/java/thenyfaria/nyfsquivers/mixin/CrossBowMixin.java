package thenyfaria.nyfsquivers.mixin;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import thenyfaria.nyfsquivers.item.QuiverItem;
import thenyfaria.nyfsquivers.ui.QuiverScreenHandler;

import java.util.List;
import java.util.Optional;

@Mixin(CrossbowItem.class)
public class CrossBowMixin {
    @Overwrite
    private static boolean loadProjectile(LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl, boolean bl2) {
        if (itemStack2.isEmpty()) {
            return false;
        } else {
            boolean bl3 = bl2 && itemStack2.getItem() instanceof ArrowItem;
            ItemStack itemStack4;
            if (!bl3 && !bl2 && !bl) {
                itemStack4 = itemStack2.split(1);
                if (itemStack2.isEmpty() && livingEntity instanceof Player) {
                    ((Player)livingEntity).getInventory().removeItem(itemStack2);

                }
            } else {
                itemStack4 = itemStack2.copy();
            }

            addChargedProjectile(itemStack, itemStack4);
            if(livingEntity instanceof Player) {
                Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent((Player) livingEntity);
                if (component.isPresent()) {
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
            return true;
        }
    }
    @Shadow
    private static void addChargedProjectile(ItemStack itemStack, ItemStack itemStack4) {
    }
}
