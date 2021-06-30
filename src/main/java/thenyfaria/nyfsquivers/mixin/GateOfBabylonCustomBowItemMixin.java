package thenyfaria.nyfsquivers.mixin;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import draylar.gateofbabylon.item.CustomBowItem;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thenyfaria.nyfsquivers.item.QuiverItem;
import thenyfaria.nyfsquivers.ui.QuiverScreenHandler;

import java.util.List;
import java.util.Optional;

@Pseudo
@Mixin(CustomBowItem.class)
public class GateOfBabylonCustomBowItemMixin {

    @Inject(method = "releaseUsing", at = @At(value="TAIL",shift = At.Shift.BEFORE),cancellable = true)
    public void boop(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo c) {
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(livingEntity);
        if(component.isPresent()) {
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
