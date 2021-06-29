package thenyfaria.nyfsquivers.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.FriendlyByteBuf;
import thenyfaria.nyfsquivers.network.ServerNetworking;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.lwjgl.glfw.GLFW;

public class NyfsQuiversKeybinds {

    private static final KeyMapping OPEN_QUIVER = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.nyfsquivers.open_quiver",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category.nyfsquivers.keybindings"));

    private static final KeyMapping INCREASE_SLOT = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.nyfsquivers.increase_slot",
            InputConstants.Type.KEYSYM,
            93,
            "category.nyfsquivers.keybindings"));

    private static final KeyMapping DECREASE_SLOT = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.nyfsquivers.decrease_slot",
            InputConstants.Type.KEYSYM,
            91,
            "category.nyfsquivers.keybindings"));


    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (OPEN_QUIVER.consumeClick()) {
                ClientPlayNetworking.send(ServerNetworking.OPEN_QUIVER, new FriendlyByteBuf(Unpooled.buffer()));
            }
            if(INCREASE_SLOT.consumeClick())
            ClientPlayNetworking.send(ServerNetworking.INCREASE_SLOT, new FriendlyByteBuf(Unpooled.buffer()));
            if(DECREASE_SLOT.consumeClick())
            ClientPlayNetworking.send(ServerNetworking.DECREASE_SLOT, new FriendlyByteBuf(Unpooled.buffer()));

        });
    }
}
