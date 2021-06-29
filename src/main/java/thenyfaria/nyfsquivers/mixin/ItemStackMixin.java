package thenyfaria.nyfsquivers.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thenyfaria.nyfsquivers.item.QuiverItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ItemStack) {
            ItemStack thisStack = (ItemStack) (Object) this;
            ItemStack checkStack = (ItemStack) obj;

            Item thisStackItem = thisStack.getItem();
            Item checkStackItem = checkStack.getItem();

            if(thisStackItem instanceof QuiverItem && checkStackItem instanceof QuiverItem) {
                return true;
            }
        }

        return super.equals(obj);
    }
}
