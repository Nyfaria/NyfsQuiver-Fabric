package com.nyfaria.nyfsquiver.ui;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import com.nyfaria.nyfsquiver.util.InventoryUtils;

public final class ExtendedSimpleContainer extends SimpleContainer {

    private final ItemStack stack;

    public ExtendedSimpleContainer(ItemStack stack, int size) {
        super(size);
        this.stack = stack;
    }

    @Override
    public void setChanged() {
        stack.getOrCreateTag().put("Inventory", InventoryUtils.toTag(this));
        super.setChanged();
    }
}
