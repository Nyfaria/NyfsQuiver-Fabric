package com.nyfaria.nyfsquiver.mixin;

import com.nyfaria.nyfsquiver.config.QuiverInfo;
import com.nyfaria.nyfsquiver.item.QuiverItem;
import com.nyfaria.nyfsquiver.ui.ExtendedSimpleContainer;
import com.nyfaria.nyfsquiver.util.InventoryUtils;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class ArrowEntityMixin extends Entity {
    @Shadow
    protected boolean inGround;

    @Shadow
    public int shakeTime;

    @Shadow
    public AbstractArrow.Pickup pickup;

    @Shadow
    protected abstract ItemStack getPickupItem();

    @Shadow
    public abstract boolean isNoPhysics();

    public ArrowEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
        throw new IllegalStateException("Mixin dummy constructor was called");
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;tryPickup(Lnet/minecraft/world/entity/player/Player;)Z"), method = "playerTouch", cancellable = true)
    private void putFloorArrowInQuiver(Player player, CallbackInfo ci) {
        if (!player.level().isClientSide && (inGround || isNoPhysics()) && shakeTime <= 0) {
            ItemStack quiverItemStack = QuiverItem.getEquippedQuiver(player);
            if (quiverItemStack.isEmpty()) return;
            if (quiverItemStack.getItem() instanceof QuiverItem) {
                QuiverInfo quiverInfo = ((QuiverItem) quiverItemStack.getItem()).getTier();
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
