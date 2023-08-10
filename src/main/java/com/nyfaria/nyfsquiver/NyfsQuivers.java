package com.nyfaria.nyfsquiver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nyfaria.nyfsquiver.config.NyfsQuiversConfig;
import com.nyfaria.nyfsquiver.config.QuiverInfo;
import com.nyfaria.nyfsquiver.item.QuiverItem;
import com.nyfaria.nyfsquiver.network.ServerNetworking;
import com.nyfaria.nyfsquiver.ui.QuiverScreenHandler;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NyfsQuivers implements ModInitializer {

    public static final String MOD_ID = "nyfsquiver";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation CONTAINER_ID = id("quiver");
    public static final List<Item> QUIVERS = new ArrayList<>();
    public static final CreativeModeTab GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(id("leather_quiver"))))
            .title(Component.translatable("itemGroup.nyfsquiver.quiver"))
            .displayItems((a,b) -> {
                for (Item item : QUIVERS) {
                    b.accept(item);
                }
            })
            .build();
    public NyfsQuiversConfig CONFIG;
    public static final MenuType<QuiverScreenHandler> CONTAINER_TYPE = ScreenHandlerRegistry.registerExtended(CONTAINER_ID, QuiverScreenHandler::new);
    public static final String BACKPACK_TRANSLATION_KEY = Util.makeDescriptionId("container", CONTAINER_ID);
    public static final TagKey<Item> QUIVER_ITEMS = TagKey.create(Registries.ITEM,new ResourceLocation(MOD_ID,"quiver_items"));
    public static NyfsQuivers instance;

    public static NyfsQuivers getInstance(){
        return instance;
    }

    @Override
    public void onInitialize() {
        loadConfig();
        registerQuivers();
        ServerNetworking.init();
        instance = this;
    }

    private void registerQuivers() {
        NyfsQuiversConfig defaultConfig = new NyfsQuiversConfig();

        for (QuiverInfo quiver : CONFIG.quivers) {
            Item.Properties settings = new Item.Properties().stacksTo(1);

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


            QuiverItem registered = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("nyfsquiver", quiver.getName().toLowerCase() + "_quiver"), new QuiverItem(quiver, settings));

            TrinketRendererRegistry.registerRenderer(registered, (TrinketRenderer) registered);
            QUIVERS.add(registered);
        }
    }



    public static ResourceLocation id(String name) {
        return new ResourceLocation("nyfsquiver", name);

    }
    public void loadConfig() {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "nyfsquivers_config.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (configFile.exists()) {
            try {
                FileReader fileReader = new FileReader(configFile);
                CONFIG = gson.fromJson(fileReader, NyfsQuiversConfig.class);
                fileReader.close();
            } catch (IOException e) {

            }
        } else {
            CONFIG = new NyfsQuiversConfig();
            saveConfig();
        }
    }

    public void saveConfig() {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "nyfsquivers_config.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdir();
        }
        try {
            FileWriter fileWriter = new FileWriter(configFile);
            fileWriter.write(gson.toJson(CONFIG));
            fileWriter.close();
        } catch (IOException e) {

        }
    }
}
