package com.nyfaria.nyfsquiver;

import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import com.nyfaria.nyfsquiver.config.QuiverInfo;
import com.nyfaria.nyfsquiver.config.NyfsQuiversConfig;
import com.nyfaria.nyfsquiver.item.QuiverItem;
import com.nyfaria.nyfsquiver.network.ServerNetworking;
import com.nyfaria.nyfsquiver.ui.QuiverScreenHandler;
import draylar.omegaconfig.OmegaConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NyfsQuivers implements ModInitializer {

    public static final String MOD_ID = "nyfsquiver";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation CONTAINER_ID = id("quiver");
    public static final CreativeModeTab GROUP = FabricItemGroupBuilder.build(CONTAINER_ID, () -> new ItemStack(Registry.ITEM.get(id("basic_quiver"))));
    public static final NyfsQuiversConfig CONFIG = OmegaConfig.register(NyfsQuiversConfig.class);
    public static final MenuType<QuiverScreenHandler> CONTAINER_TYPE = ScreenHandlerRegistry.registerExtended(CONTAINER_ID, QuiverScreenHandler::new);
    public static final List<Item> QUIVERS = new ArrayList<>();
    public static final String BACKPACK_TRANSLATION_KEY = Util.makeDescriptionId("container", CONTAINER_ID);
    public static final Tag<Item> QUIVER_ITEMS = TagFactory.ITEM.create(new ResourceLocation(MOD_ID,"quiver_items"));



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
            if (quiver.isFireImmune()) {
                settings.fireResistant();
            }

            // old config instances do not have the sound stuff
            if (quiver.getOpenSound() == null) {
                Optional<QuiverInfo> any = defaultConfig.quivers.stream().filter(info -> info.getName().equals(quiver.getName())).findAny();
                any.ifPresent(quiverInfo -> quiver.setOpenSound(quiverInfo.getOpenSound()));

                // if it is STILL null, log an error and set a default
                if (quiver.getOpenSound() == null) {
                    LOGGER.info(String.format("Could not find a sound event for %s in nyfsquiver.json config.", quiver.getName()));
                    LOGGER.info("Consider regenerating your config, or assigning the openSound value. Rolling with defaults for now.");
                    quiver.setOpenSound("minecraft:item.armor.equip_leather");
                }
            }


            QuiverItem registered = Registry.register(Registry.ITEM, new ResourceLocation("nyfsquiver", quiver.getName().toLowerCase() + "_quiver"), new QuiverItem(quiver, settings));

            TrinketRendererRegistry.registerRenderer(registered, (TrinketRenderer) registered);
            QUIVERS.add(registered);
        }
    }



    public static ResourceLocation id(String name) {
        return new ResourceLocation("nyfsquiver", name);

    }
}
