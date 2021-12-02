package com.nyfaria.nyfsquiver.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.nyfaria.nyfsquiver.NyfsQuivers;
import com.nyfaria.nyfsquiver.item.QuiverItem;
import com.nyfaria.nyfsquiver.ui.QuiverScreenHandler;

import java.util.List;
import java.util.Optional;

@Mixin(Gui.class)
public abstract class GuiMixin extends GuiComponent {

    @Shadow protected abstract void renderSlot(int i, int j, float f, Player player, ItemStack itemStack, int k);

    @Shadow protected abstract Player getCameraPlayer();

    @Unique private ItemStack quiverStack;
    @Unique private ItemStack stackInQuiver;



    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getOffhandItem()Lnet/minecraft/world/item/ItemStack;",
                    shift = At.Shift.BY,
                    by = 2
            ),
            method = "renderHotbar"
    )
    private void getQuiverStacksFromPlayer(float f, PoseStack poseStack, CallbackInfo ci) {
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(getCameraPlayer());
        if (component.isPresent()) {
            List<Tuple<SlotReference, ItemStack>> allEquipped = component.get().getAllEquipped();
            for (Tuple<SlotReference, ItemStack> entry : allEquipped) {

                if (entry.getB().getItem() instanceof QuiverItem) {
                    quiverStack = entry.getB();
                }
            }
            if (quiverStack != null) {
                QuiverScreenHandler quiverContainer = new QuiverScreenHandler(0, getCameraPlayer().getInventory(), quiverStack);
                if (!quiverStack.isEmpty()) {
                    if (!quiverStack.getOrCreateTag().contains("current_slot")) {
                        quiverStack.getOrCreateTag().putInt("current_slot", 0);
                    }
                    int curSlot = quiverStack.getOrCreateTag().getInt("current_slot");
                    stackInQuiver = quiverContainer.getSlot(curSlot).getItem();

                }
            }
        }
    }

    @Inject(
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/Gui;setBlitOffset(I)V",
                ordinal = 1
            ),
            method = "renderHotbar"
    )
    private void renderQuiverCurSlotInHotbar(float f, PoseStack poseStack, CallbackInfo ci) {
        if(quiverStack != null && !quiverStack.isEmpty()) {
            if(quiverStack.getItem() instanceof QuiverItem) {
                int x = NyfsQuivers.CONFIG.xpos;
                int y = NyfsQuivers.CONFIG.ypos;
                int currentslot = quiverStack.getOrCreateTag().getInt("current_slot");
                blit(poseStack, x, y, 24, 22, 29, 24);

                Font font = Minecraft.getInstance().font;
                poseStack.scale(.7f, .7f, .7f);
                font.draw(poseStack, String.valueOf(currentslot + 1), (x * 1.42857142857f) + 5, (y * 1.42857142857f) + 5, 0xFFFFFFFF);
                poseStack.scale(1.42857142857f, 1.42857142857f, 1.42857142857f);
            }
        }
    }

    @Inject(
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/Options;attackIndicator:Lnet/minecraft/client/AttackIndicatorStatus;"
            ),
            method = "renderHotbar"
    )
    private void renderQuiverSlotAfter(float f, PoseStack poseStack, CallbackInfo ci) {
        if (stackInQuiver != null && !stackInQuiver.isEmpty()) {
            renderSlot(NyfsQuivers.CONFIG.xpos + 3, NyfsQuivers.CONFIG.ypos + 3, f, getCameraPlayer(), stackInQuiver, 12);
        }
        //quiverStack = null;
        stackInQuiver = null;
    }
}
