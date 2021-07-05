package thenyfaria.nyfsquivers;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thenyfaria.nyfsquivers.client.NyfsQuiversKeybinds;
import thenyfaria.nyfsquivers.config.NyfsQuiversConfig;
import thenyfaria.nyfsquivers.config.QuiverInfo;
import thenyfaria.nyfsquivers.item.QuiverItem;
import thenyfaria.nyfsquivers.ui.ExtendedSimpleContainer;
import thenyfaria.nyfsquivers.ui.QuiverHandledScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry;
import thenyfaria.nyfsquivers.ui.QuiverScreenHandler;
import thenyfaria.nyfsquivers.util.InventoryUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class NyfsQuiversClient implements ClientModInitializer {

    public static final List<Item> QUIVERS = NyfsQuivers.QUIVERS;

    @Override
    public void onInitializeClient() {

        ScreenRegistry.register(NyfsQuivers.CONTAINER_TYPE, QuiverHandledScreen::new);
        //registerQuivers();
        NyfsQuiversKeybinds.initialize();
    }
    private void registerQuivers() {
        for (Item registered : QUIVERS) {
            FabricModelPredicateProviderRegistry.register(registered, new ResourceLocation("nyfsquivers", "equipped"), (itemStack, clientWorld, livingEntity, i) -> {
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
            FabricModelPredicateProviderRegistry.register(registered, new ResourceLocation("nyfsquivers", "arrows"), (itemStack, clientWorld, livingEntity, i) -> {
                Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(livingEntity);
                if(component.isPresent()) {
                    List<Tuple<SlotReference, ItemStack>> allEquipped = component.get().getAllEquipped();
                    for (Tuple<SlotReference, ItemStack> entry : allEquipped) {
                        ItemStack beep = entry.getB();
                        if (entry.getB().getItem() instanceof QuiverItem) {
                            QuiverInfo meow = ((QuiverItem)beep.getItem()).getTier();
                            ListTag tag = beep.getOrCreateTag().getList("Inventory", NbtType.COMPOUND);

                            SimpleContainer inventory = new ExtendedSimpleContainer(beep, meow.getRowWidth() * meow.getNumberOfRows());

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
        return new ResourceLocation("nyfsquivers", name);

    }

}
