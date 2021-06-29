package thenyfaria.nyfsquivers.network;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import thenyfaria.nyfsquivers.NyfsQuivers;
import thenyfaria.nyfsquivers.config.QuiverInfo;
import thenyfaria.nyfsquivers.item.QuiverItem;
import thenyfaria.nyfsquivers.mixin.trinkets.TrinketsMixinPlugin;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import java.util.List;
import java.util.Optional;
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
        if (TrinketsMixinPlugin.isTrinketsLoaded) {
            Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);

            // Iterate over the player's Trinket inventory.
            // Once a quiver has been found, open it.
            // TODO: HOW DOES THIS WORK WHEN TRINKETS IS NOT INSTALLED???
            if(component.isPresent()) {
                List<Tuple<SlotReference, ItemStack>> allEquipped = component.get().getAllEquipped();
                for(Tuple<SlotReference, ItemStack> entry : allEquipped) {
                    if(entry.getB().getItem() instanceof QuiverItem) {
                        QuiverItem.openScreen(player, entry.getB(), player.getUsedItemHand());
                        return;
                    }
                }
            }
        }

        ItemStack firstQuiverItemStack = Stream.concat(player.getInventory().offhand.stream(), player.getInventory().items.stream())
                .filter((itemStack) -> itemStack.getItem() instanceof QuiverItem)
                .findFirst()
                .orElse(ItemStack.EMPTY);
        if (firstQuiverItemStack != ItemStack.EMPTY) {
            QuiverItem.openScreen(player, firstQuiverItemStack,null);
        }
    }
    public static void IncreaseSlot(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender){
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
        List<Tuple<SlotReference, ItemStack>> allEquipped = component.get().getAllEquipped();
        for(Tuple<SlotReference, ItemStack> entry : allEquipped) {
            if (entry.getB().getItem() instanceof QuiverItem) {
                ItemStack meep = entry.getB();
                QuiverInfo boop = ((QuiverItem)meep.getItem()).getTier();
                int current_slot = meep.getOrCreateTag().getInt("current_slot");
                int next_slot = 0;
                if(current_slot == (boop.getNumberOfRows() * boop.getRowWidth())-1){

                }else{
                    next_slot = current_slot + 1;
                }
                meep.getOrCreateTag().putInt("current_slot",next_slot);
            }
        }
    }
    public static void DecreaseSlot(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender){
            Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
            List<Tuple<SlotReference, ItemStack>> allEquipped = component.get().getAllEquipped();
            for(Tuple<SlotReference, ItemStack> entry : allEquipped) {
                if (entry.getB().getItem() instanceof QuiverItem) {
                    ItemStack meep = entry.getB();
                    QuiverInfo boop = ((QuiverItem)meep.getItem()).getTier();
                    int current_slot = meep.getOrCreateTag().getInt("current_slot");
                    int next_slot = 0;
                    if(current_slot == 0){
                        next_slot = (boop.getNumberOfRows() * boop.getRowWidth())-1;
                    }else{
                        next_slot = current_slot - 1;
                    }
                    meep.getOrCreateTag().putInt("current_slot",next_slot);
                }
            }
    }
}
