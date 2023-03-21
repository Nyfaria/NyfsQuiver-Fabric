package com.nyfaria.nyfsquiver.network;

import com.nyfaria.nyfsquiver.NyfsQuivers;
import com.nyfaria.nyfsquiver.config.QuiverInfo;
import com.nyfaria.nyfsquiver.item.QuiverItem;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public class ServerNetworking {

    public static ResourceLocation OPEN_QUIVER = NyfsQuivers.id("open_quiver");
    public static ResourceLocation INCREASE_SLOT = NyfsQuivers.id("increase_slot");
    public static ResourceLocation DECREASE_SLOT = NyfsQuivers.id("decrease_slot");


    public static void init() {
        registerOpenQuiverPacketHandler();
    }

    private static void registerOpenQuiverPacketHandler() {
        ServerPlayNetworking.registerGlobalReceiver(OPEN_QUIVER, ServerNetworking::receiveOpenQuiverPacket);
        ServerPlayNetworking.registerGlobalReceiver(INCREASE_SLOT, ServerNetworking::IncreaseSlot);
        ServerPlayNetworking.registerGlobalReceiver(DECREASE_SLOT, ServerNetworking::DecreaseSlot);
    }

    private static void receiveOpenQuiverPacket(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        ItemStack equippedQuiver = QuiverItem.getEquippedQuiver(player);

        // TODO: HOW DOES THIS WORK WHEN TRINKETS IS NOT INSTALLED???
        if (!equippedQuiver.isEmpty()) {
            QuiverItem.openScreen(player, equippedQuiver, player.getUsedItemHand());
            return;
        }


        ItemStack firstQuiverItemStack = Stream.concat(player.getInventory().offhand.stream(), player.getInventory().items.stream())
                .filter((itemStack) -> itemStack.getItem() instanceof QuiverItem)
                .findFirst()
                .orElse(ItemStack.EMPTY);
        if (firstQuiverItemStack != ItemStack.EMPTY) {
            QuiverItem.openScreen(player, firstQuiverItemStack, null);
        }
    }

    public static void IncreaseSlot(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        ItemStack equippedQuiver = QuiverItem.getEquippedQuiver(player);

        if (!equippedQuiver.isEmpty()) {
            QuiverInfo boop = ((QuiverItem) equippedQuiver.getItem()).getTier();
            int current_slot = equippedQuiver.getOrCreateTag().getInt("current_slot");
            int next_slot = 0;
            if (current_slot == (boop.getNumberOfRows() * boop.getRowWidth()) - 1) {

            } else {
                next_slot = current_slot + 1;
            }
            equippedQuiver.getOrCreateTag().putInt("current_slot", next_slot);
        }

    }

    public static void DecreaseSlot(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        ItemStack equippedQuiver = QuiverItem.getEquippedQuiver(player);

        if (!equippedQuiver.isEmpty()) {
            QuiverInfo boop = ((QuiverItem) equippedQuiver.getItem()).getTier();
            int current_slot = equippedQuiver.getOrCreateTag().getInt("current_slot");
            int next_slot = 0;
            if (current_slot == 0) {
                next_slot = (boop.getNumberOfRows() * boop.getRowWidth()) - 1;
            } else {
                next_slot = current_slot - 1;
            }
            equippedQuiver.getOrCreateTag().putInt("current_slot", next_slot);

        }
    }
}
