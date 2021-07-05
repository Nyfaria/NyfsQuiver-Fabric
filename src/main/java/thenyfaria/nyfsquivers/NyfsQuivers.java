package thenyfaria.nyfsquivers;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.Util;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Registry;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import thenyfaria.nyfsquivers.config.QuiverInfo;
import thenyfaria.nyfsquivers.config.NyfsQuiversConfig;
import thenyfaria.nyfsquivers.item.QuiverItem;
import thenyfaria.nyfsquivers.network.ServerNetworking;
import thenyfaria.nyfsquivers.ui.ExtendedSimpleContainer;
import thenyfaria.nyfsquivers.ui.QuiverScreenHandler;
import draylar.omegaconfig.OmegaConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thenyfaria.nyfsquivers.util.InventoryUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NyfsQuivers implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation CONTAINER_ID = id("quiver");
    public static final CreativeModeTab GROUP = FabricItemGroupBuilder.build(CONTAINER_ID, () -> new ItemStack(Registry.ITEM.get(id("basic_quiver"))));
    public static final NyfsQuiversConfig CONFIG = OmegaConfig.register(NyfsQuiversConfig.class);
    public static final MenuType<QuiverScreenHandler> CONTAINER_TYPE = ScreenHandlerRegistry.registerExtended(CONTAINER_ID, QuiverScreenHandler::new);
    public static final List<Item> QUIVERS = new ArrayList<>();
    public static final String BACKPACK_TRANSLATION_KEY = Util.makeDescriptionId("container", CONTAINER_ID);

    @Override
    public void onInitialize() {
        registerQuivers();
        ServerNetworking.init();
    }

    private void registerQuivers() {
        NyfsQuiversConfig defaultConfig = new NyfsQuiversConfig();

        for (QuiverInfo quiver : NyfsQuivers.CONFIG.quivers) {
            Item.Properties settings = new Item.Properties().tab(NyfsQuivers.GROUP).stacksTo(1);

            // setup fireproof item settings
            if(quiver.isFireImmune()) {
                settings.fireResistant();
            }

            // old config instances do not have the sound stuff
            if(quiver.getOpenSound() == null) {
                Optional<QuiverInfo> any = defaultConfig.quivers.stream().filter(info -> info.getName().equals(quiver.getName())).findAny();
                any.ifPresent(quiverInfo -> quiver.setOpenSound(quiverInfo.getOpenSound()));

                // if it is STILL null, log an error and set a default
                if(quiver.getOpenSound() == null) {
                    LOGGER.info(String.format("Could not find a sound event for %s in nyfsquivers.json config.", quiver.getName()));
                    LOGGER.info("Consider regenerating your config, or assigning the openSound value. Rolling with defaults for now.");
                    quiver.setOpenSound("minecraft:item.armor.equip_leather");
                }
            }


            QuiverItem registered = Registry.register(Registry.ITEM, new ResourceLocation("nyfsquivers", quiver.getName().toLowerCase() + "_quiver"), new QuiverItem(quiver, settings));

            FabricModelPredicateProviderRegistry.register(registered, new ResourceLocation("nyfsquivers","equipped"), (itemStack, clientWorld, livingEntity, i) -> {
                Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(livingEntity);
                if(component.isPresent()) {
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
            FabricModelPredicateProviderRegistry.register(registered, new ResourceLocation("nyfsquivers","arrows"), (itemStack, clientWorld, livingEntity, i) -> {
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
            QUIVERS.add(registered);
        }
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation("nyfsquivers", name);

    }
}
