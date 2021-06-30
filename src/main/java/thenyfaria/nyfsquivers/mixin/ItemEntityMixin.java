package thenyfaria.nyfsquivers.mixin;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
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
import thenyfaria.nyfsquivers.item.QuiverItem;
import thenyfaria.nyfsquivers.ui.QuiverScreenHandler;

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
    public void mip(Player player, CallbackInfo c) {
        if (!level.isClientSide) {
            ItemStack itemStack = getItem();
            Item item = itemStack.getItem();
            int i = itemStack.getCount();
            if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUUID()))) {
                Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(player);
                if (trinketComponent.isPresent() && (item instanceof ArrowItem || item instanceof FireworkRocketItem)) {
                    List<Tuple<SlotReference, ItemStack>> equippedTrinkets = trinketComponent.get().getAllEquipped();
                    for (Tuple<SlotReference, ItemStack> trinket : equippedTrinkets) {
                        ItemStack quiverStack = trinket.getB();
                        if (trinket.getB().getItem() instanceof QuiverItem) {
                            QuiverScreenHandler quiverContainer = new QuiverScreenHandler(0, player.getInventory(), quiverStack);
                            for (Slot slot : quiverContainer.slots) {
                                if(slot.getItem().getItem() == item && slot.getItem().getCount() < 64){
                                    if(slot.getItem().getCount() + itemStack.getCount() <= 64){
                                        slot.getItem().grow(itemStack.getCount());
                                        player.awardStat(Stats.ITEM_PICKED_UP.get(item), itemStack.getCount());
                                        slot.setChanged();
                                        itemStack.setCount(0);
                                        break;
                                    }
                                    if(slot.getItem().getCount() + itemStack.getCount() > 64){
                                        slot.getItem().setCount(64);
                                        player.awardStat(Stats.ITEM_PICKED_UP.get(item), 64-slot.getItem().getCount());
                                        itemStack.shrink(64-slot.getItem().getCount());
                                    }
                                }
                                if(slot.getItem().isEmpty()){

                                        slot.set(itemStack);
                                        itemStack.setCount(0);

                                }
                                if(itemStack.getCount() == 0)
                                    break;
                            }
                        }
                    }
                }
                if (player.getInventory().add(itemStack)) {

                    player.take(this, i);
                    if (itemStack.isEmpty()) {
                        discard();
                        itemStack.setCount(i);
                    }
                    player.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
                    player.onItemPickup((ItemEntity) (Object) this);
                }
                c.cancel();
            }
        }
    }

}
