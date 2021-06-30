package thenyfaria.nyfsquivers.mixin;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thenyfaria.nyfsquivers.item.QuiverItem;
import thenyfaria.nyfsquivers.ui.QuiverScreenHandler;

import java.util.List;
import java.util.Optional;

@Mixin(AbstractArrow.class)
public abstract class ArrowEntityMixin extends Entity {
    @Shadow protected boolean inGround;

    @Shadow public int shakeTime;

    @Shadow public AbstractArrow.Pickup pickup;

    @Shadow protected abstract ItemStack getPickupItem();

    @Shadow public abstract boolean isNoPhysics();

    public ArrowEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
        throw new IllegalStateException("Mixin dummy constructor was called");
    }
    
    @Inject(at = @At("HEAD"), method = "playerTouch", cancellable = true)
    private void checkForQuiver(Player player, CallbackInfo ci) {
        if (!player.level.isClientSide && inGround && isNoPhysics() && shakeTime > 0) {
            Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(player);

            if (trinketComponent.isPresent() && pickup == AbstractArrow.Pickup.ALLOWED) {
                List<Tuple<SlotReference, ItemStack>> equippedTrinkets = trinketComponent.get().getAllEquipped();

                for (Tuple<SlotReference, ItemStack> trinket : equippedTrinkets) {
                    ItemStack stack = trinket.getB();
                    if (stack.getItem() instanceof QuiverItem) {
                        QuiverScreenHandler quiverContainer = new QuiverScreenHandler(0, player.getInventory(), stack);
                        for (Slot slot : quiverContainer.slots) {
                            if (slot.getItem().getItem() == this.getPickupItem().getItem() && slot.getItem().getCount() < 64) {
                                slot.getItem().grow(1);
                                slot.setChanged();
                                discard();
                                ci.cancel();
                            }
                            if (slot.getItem().isEmpty()) {
                                slot.set(this.getPickupItem());
                                discard();
                                ci.cancel();
                            }
                        }
                    }
                }
            }
        }
    }
}
