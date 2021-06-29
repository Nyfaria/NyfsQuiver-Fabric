package thenyfaria.nyfsquivers.compat;

//import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
//import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import thenyfaria.nyfsquivers.item.QuiverItem;
import net.fabricmc.fabric.api.util.NbtType;

import java.util.ArrayList;
import java.util.List;

public class NyfsQuiversPreviewProvider /*implements PreviewProvider*/ {
/*
    @Override
    public boolean shouldDisplay(PreviewContext context) {
        return !getInventory(context).stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public List<ItemStack> getInventory(PreviewContext context) {
       List<ItemStack> stacks = new ArrayList<>();
       ListTag inventoryTag = context.getStack().getOrCreateTag().getList("Inventory", NbtType.COMPOUND);

        inventoryTag.forEach(element -> {
            CompoundTag stackTag = (CompoundTag) element;
            int slot = stackTag.getInt("Slot");
            ItemStack stack = ItemStack.of(stackTag.getCompound("Stack"));
            stacks.add(slot, stack);
        });

        return stacks;
    }

    @Override
    public int getInventoryMaxSize(PreviewContext context) {
        return getInventory(context).size();
    }

    @Override
    public int getMaxRowSize(PreviewContext context) {
        return ((QuiverItem) context.getStack().getItem()).getTier().getRowWidth();
    }*/
}
