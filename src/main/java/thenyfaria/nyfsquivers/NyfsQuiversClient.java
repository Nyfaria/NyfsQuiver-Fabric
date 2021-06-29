package thenyfaria.nyfsquivers;

import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import thenyfaria.nyfsquivers.client.NyfsQuiversKeybinds;
import thenyfaria.nyfsquivers.ui.QuiverHandledScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry;
import thenyfaria.nyfsquivers.ui.QuiverScreenHandler;

@Environment(EnvType.CLIENT)
public class NyfsQuiversClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        ScreenRegistry.register(NyfsQuivers.CONTAINER_TYPE, QuiverHandledScreen::new);
        for(Item boop : NyfsQuivers.QUIVERS){
            TrinketRendererRegistry.registerRenderer(boop, (TrinketRenderer) boop);
        }
        NyfsQuiversKeybinds.initialize();
    }
}
