package thenyfaria.nyfsquivers.mixin.trinkets;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thenyfaria.nyfsquivers.item.QuiverItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(QuiverItem.class)
public abstract class QuiverItemTrinketMixin extends Item implements Trinket {

    public QuiverItemTrinketMixin(Properties settings) {
        super(settings);
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        Trinket.super.onEquip(stack, slot, entity);

        if(stack.getItem() instanceof QuiverItem){

        }
    }
}
