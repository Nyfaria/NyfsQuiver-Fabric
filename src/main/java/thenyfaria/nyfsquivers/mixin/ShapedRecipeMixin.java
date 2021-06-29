package thenyfaria.nyfsquivers.mixin;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import thenyfaria.nyfsquivers.item.QuiverItem;
import net.fabricmc.fabric.api.util.NbtType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {

    @Shadow public abstract ItemStack getResultItem();

    @Inject(method = "assemble",at = @At(value="HEAD"),cancellable = true)
    private void onCraft(CraftingContainer craftingInventory, CallbackInfoReturnable<ItemStack> cir) {
        // get both backpacks
        ItemStack centerSlot = craftingInventory.getItem(4);

        // only attempt to apply nbt if the center stack of the original recipe was a backpack
        if(centerSlot.getItem() instanceof QuiverItem) {
            ItemStack newBackpack = this.getResultItem().copy();

            if(newBackpack.getItem() instanceof QuiverItem) {
                ListTag oldTag = centerSlot.getOrCreateTag().getList("Inventory", NbtType.COMPOUND);
                newBackpack.getOrCreateTag().put("Inventory", oldTag);
                cir.setReturnValue(newBackpack);
            }
        }
    }
}
