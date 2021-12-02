package com.nyfaria.nyfsquiver.mixin;

import com.nyfaria.nyfsquiver.NyfsQuivers;
import com.nyfaria.nyfsquiver.config.QuiverInfo;
import com.nyfaria.nyfsquiver.ui.ExtendedSimpleContainer;
import com.nyfaria.nyfsquiver.util.InventoryUtils;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.ListTag;
import net.minecraft.stats.Stats;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
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
import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Shadow
    private UUID owner;

    @Shadow
    private int pickupDelay;

    @Shadow public abstract ItemStack getItem();

    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
        throw new IllegalStateException("Mixin dummy constructor was called");
    }

    @Inject(method = "playerTouch", at = @At(value="HEAD"),cancellable = true)
    public void tryInsertQuiver(Player player, CallbackInfo c) {
        if (!level.isClientSide) {
            ItemStack itemStack = getItem();
            Item item = itemStack.getItem();
            if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUUID()))) {
                Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(player);
                if (trinketComponent.isPresent() && (itemStack.is(NyfsQuivers.QUIVER_ITEMS))) {
                    List<Tuple<SlotReference, ItemStack>> equippedTrinkets = trinketComponent.get().getAllEquipped();
                    for (Tuple<SlotReference, ItemStack> trinket : equippedTrinkets) {
                        ItemStack quiverItemStack = trinket.getB();
                        if (trinket.getB().getItem() instanceof QuiverItem) {

                            QuiverInfo quiverInfo = ((QuiverItem)quiverItemStack.getItem()).getTier();
                            ListTag tag = quiverItemStack.getOrCreateTag().getList("Inventory", NbtType.COMPOUND);
                            ExtendedSimpleContainer inventory = new ExtendedSimpleContainer(quiverItemStack, quiverInfo.getRowWidth() * quiverInfo.getNumberOfRows());
                            InventoryUtils.fromTag(tag, inventory);
                            int quiverSize = quiverInfo.getNumberOfRows() * quiverInfo.getRowWidth();
                            for (int i = 0; i < quiverSize; i++) {
                                if(inventory.getItem(i).getItem() == item && inventory.getItem(i).getCount() < 64){
                                    if(inventory.getItem(i).getCount() + itemStack.getCount() <= 64){
                                        inventory.getItem(i).grow(itemStack.getCount());
                                        quiverItemStack.getOrCreateTag().put("Inventory", InventoryUtils.toTag(inventory));
                                        player.awardStat(Stats.ITEM_PICKED_UP.get(item), itemStack.getCount());
                                        itemStack.setCount(0);
                                        break;
                                    }
                                    if(inventory.getItem(i).getCount() + itemStack.getCount() > 64){
                                        inventory.getItem(i).setCount(64);
                                        quiverItemStack.getOrCreateTag().put("Inventory", InventoryUtils.toTag(inventory));
                                        player.awardStat(Stats.ITEM_PICKED_UP.get(item), 64-inventory.getItem(i).getCount());
                                        itemStack.shrink(64-inventory.getItem(i).getCount());
                                    }
                                }
                                if(inventory.getItem(i).isEmpty()) {
                                    inventory.setItem(i,itemStack);
                                    quiverItemStack.getOrCreateTag().put("Inventory", InventoryUtils.toTag(inventory));
                                    itemStack.setCount(0);
                                    c.cancel();
                                    break;

                                }
                                if(itemStack.getCount() == 0) {
                                    c.cancel();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
