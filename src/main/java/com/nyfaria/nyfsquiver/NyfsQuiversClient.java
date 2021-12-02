package com.nyfaria.nyfsquiver;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.nyfaria.nyfsquiver.client.NyfsQuiversKeybinds;
import com.nyfaria.nyfsquiver.config.QuiverInfo;
import com.nyfaria.nyfsquiver.item.QuiverItem;
import com.nyfaria.nyfsquiver.ui.ExtendedSimpleContainer;
import com.nyfaria.nyfsquiver.ui.QuiverHandledScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.nyfaria.nyfsquiver.util.InventoryUtils;

import java.util.List;
import java.util.Optional;

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
            FabricModelPredicateProviderRegistry.register(registered, new ResourceLocation("nyfsquiver", "equipped"), (itemStack, clientWorld, livingEntity, i) -> {
                Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(livingEntity);
                if (component.isPresent()) {
                    List<Tuple<SlotReference, ItemStack>> allEquipped = component.get().getAllEquipped();
                    for (Tuple<SlotReference, ItemStack> entry : allEquipped) {
                        ItemStack beep = entry.getB();
                        if (beep == itemStack) {
                            return 1.0f;
                        }
                    }
                }
                return 0.0f;
            });
            FabricModelPredicateProviderRegistry.register(registered, new ResourceLocation("nyfsquiver", "arrows"), (itemStack, clientWorld, livingEntity, i) -> {
                Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(livingEntity);
                if(component.isPresent()) {
                    List<Tuple<SlotReference, ItemStack>> allEquipped = component.get().getAllEquipped();
                    for (Tuple<SlotReference, ItemStack> entry : allEquipped) {
                        ItemStack beep = entry.getB();
                        if (entry.getB().getItem() instanceof QuiverItem) {
                            QuiverInfo meow = ((QuiverItem)beep.getItem()).getTier();
                            ListTag tag = beep.getOrCreateTag().getList("Inventory", NbtType.COMPOUND);

                            ExtendedSimpleContainer inventory = new ExtendedSimpleContainer(beep, meow.getRowWidth() * meow.getNumberOfRows());

                            InventoryUtils.fromTag(tag, inventory);
                            ItemStack itemStack4 = inventory.getItem(entry.getB().getOrCreateTag().getInt("current_slot"));
                            if(itemStack4.getCount() > 0)
                                return 0.0f;

                        }
                    }
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
