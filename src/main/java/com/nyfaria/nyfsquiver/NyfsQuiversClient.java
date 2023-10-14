package com.nyfaria.nyfsquiver;

import com.nyfaria.nyfsquiver.client.NyfsQuiversKeybinds;
import com.nyfaria.nyfsquiver.config.QuiverInfo;
import com.nyfaria.nyfsquiver.item.QuiverItem;
import com.nyfaria.nyfsquiver.ui.ExtendedSimpleContainer;
import com.nyfaria.nyfsquiver.ui.QuiverHandledScreen;
import com.nyfaria.nyfsquiver.util.InventoryUtils;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public class NyfsQuiversClient implements ClientModInitializer {

    public static final List<Item> QUIVERS = NyfsQuivers.QUIVERS;

    @Override
    public void onInitializeClient() {

        ScreenRegistry.register(NyfsQuivers.CONTAINER_TYPE, QuiverHandledScreen::new);
        registerQuivers();
        NyfsQuiversKeybinds.initialize();
    }

    private void registerQuivers() {
        for (Item registered : QUIVERS) {
            ItemProperties.register(registered, new ResourceLocation("nyfsquiver", "equipped"), (itemStack, clientWorld, livingEntity, i) -> {
                ItemStack equippedQuiver = QuiverItem.getEquippedQuiver(livingEntity);
                if (equippedQuiver.isEmpty()) return 0.0f;
                if (equippedQuiver == itemStack) {
                    return 1.0f;
                }
                return 0.0f;
            });
            ItemProperties.register(registered, new ResourceLocation("nyfsquiver", "arrows"), (itemStack, clientWorld, livingEntity, i) -> {
                if (itemStack.hasTag()) {
                    QuiverInfo meow = ((QuiverItem) itemStack.getItem()).getTier();
                    CompoundTag compoundTag = itemStack.getTag();
                    ListTag tag = compoundTag.getList("Inventory", NbtType.COMPOUND);
                    ExtendedSimpleContainer inventory = new ExtendedSimpleContainer(itemStack, meow.getRowWidth() * meow.getNumberOfRows());
                    InventoryUtils.fromTag(tag, inventory);
                    ItemStack itemStack4 = inventory.getItem(compoundTag.getInt("current_slot"));
                    if (itemStack4.getCount() > 0)
                        return 0.0f;
                }
                return 1.0f;

            });

            TrinketRendererRegistry.registerRenderer(registered, (TrinketRenderer) registered);
        }

    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation("nyfsquiver", name);

    }

}
