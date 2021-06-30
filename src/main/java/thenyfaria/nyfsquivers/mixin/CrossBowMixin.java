package thenyfaria.nyfsquivers.mixin;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thenyfaria.nyfsquivers.item.QuiverItem;
import thenyfaria.nyfsquivers.ui.QuiverScreenHandler;

import java.util.List;
import java.util.Optional;

@Mixin(CrossbowItem.class)
public class CrossBowMixin {

    @Inject(at = @At("TAIL"), method = "loadProjectile", cancellable = true)
    private static void loadFromQuiver(LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl, boolean bl2, CallbackInfoReturnable<Boolean> cir) {
        if(livingEntity instanceof Player) {
            Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(livingEntity);
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
    }
}
