package thenyfaria.nyfsquivers.mixin;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import thenyfaria.nyfsquivers.item.QuiverItem;
import thenyfaria.nyfsquivers.ui.QuiverScreenHandler;

import java.util.List;
import java.util.Optional;

@Mixin(AbstractArrow.class)
public abstract class ArrowEntityMixin {

    @Shadow
    protected boolean inGround;


    @Shadow
    protected abstract ItemStack getPickupItem();



    @Overwrite
    public void playerTouch(Player player) {
        AbstractArrow arrow = ((AbstractArrow)(Object)this);
        if (arrow.level.isClientSide) return;
        if (!this.inGround && !this.isNoPhysics()) return;
        if (arrow.shakeTime > 0) return;
        Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(player);
        if (trinketComponent.isPresent() && arrow.pickup == AbstractArrow.Pickup.ALLOWED) {
            List<Tuple<SlotReference, ItemStack>> equippedTrinkets = trinketComponent.get().getAllEquipped();
            for (Tuple<SlotReference, ItemStack> trinket : equippedTrinkets) {
                ItemStack itemStack = trinket.getB();
                if (trinket.getB().getItem() instanceof QuiverItem) {
                    QuiverScreenHandler quiverContainer = new QuiverScreenHandler(0, player.getInventory(), itemStack);
                    for (Slot slot : quiverContainer.slots) {
                        if (slot.getItem().getItem() == this.getPickupItem().getItem() && slot.getItem().getCount() < 64) {
                            slot.getItem().grow(1);
                            slot.setChanged();
                            (arrow).discard();
                            return;
                        }
                        if (slot.getItem().isEmpty()) {
                            slot.set(this.getPickupItem());
                            (arrow).discard();
                            return;
                        }
                    }
                }
            }

        }
        if (this.tryPickup(player)) {
            player.take((AbstractArrow)(Object)this, 1);
            ((AbstractArrow)(Object)this).discard();
        }
    }

    @Shadow
    public boolean isNoPhysics(){return false;};


    @Shadow
    protected boolean tryPickup(Player player) {
        return true;
    }

}
