package com.nyfaria.nyfsquiver.mixin;

import com.nyfaria.nyfsquiver.config.QuiverInfo;
import com.nyfaria.nyfsquiver.ui.ExtendedSimpleContainer;
import com.nyfaria.nyfsquiver.util.InventoryUtils;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.nyfaria.nyfsquiver.item.QuiverItem;
import com.nyfaria.nyfsquiver.ui.QuiverScreenHandler;

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
    private void putFloorArrowInQuiver(Player player, CallbackInfo ci) {
        if (!player.level.isClientSide && (inGround || isNoPhysics()) && shakeTime <= 0) {
            Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(player);

            if (trinketComponent.isPresent() && pickup == AbstractArrow.Pickup.ALLOWED && this.getPickupItem().getItem() instanceof ArrowItem) {
                List<Tuple<SlotReference, ItemStack>> equippedTrinkets = trinketComponent.get().getAllEquipped();

                for (Tuple<SlotReference, ItemStack> trinket : equippedTrinkets) {
                    ItemStack quiverItemStack = trinket.getB();
                    if (quiverItemStack.getItem() instanceof QuiverItem) {

                        //QuiverScreenHandler quiverContainer = new QuiverScreenHandler(0, player.getInventory(), quiverItemStack);
                        QuiverInfo quiverInfo = ((QuiverItem)quiverItemStack.getItem()).getTier();
                        ListTag tag = quiverItemStack.getOrCreateTag().getList("Inventory", NbtType.COMPOUND);
                        ExtendedSimpleContainer inventory = new ExtendedSimpleContainer(quiverItemStack, quiverInfo.getRowWidth() * quiverInfo.getNumberOfRows());
                        InventoryUtils.fromTag(tag, inventory);
                        int quiverSize = quiverInfo.getNumberOfRows() * quiverInfo.getRowWidth();
                        for (int i = 0; i < quiverSize; i++) {

                            if (inventory.getItem(i).getItem() == this.getPickupItem().getItem() && inventory.getItem(i).getCount() < 64) {
                                inventory.getItem(i).grow(1);
                                //slot.setChanged();
                                quiverItemStack.getOrCreateTag().put("Inventory", InventoryUtils.toTag(inventory));
                                discard();
                                ci.cancel();
                                break;
                            }
                            if (inventory.getItem(i).isEmpty()) {
                                inventory.setItem(i, this.getPickupItem());
                                //slot.set(this.getPickupItem());
                                quiverItemStack.getOrCreateTag().put("Inventory", InventoryUtils.toTag(inventory));
                                discard();
                                ci.cancel();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
