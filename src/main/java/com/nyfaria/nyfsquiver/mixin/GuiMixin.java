package com.nyfaria.nyfsquiver.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nyfaria.nyfsquiver.NyfsQuivers;
import com.nyfaria.nyfsquiver.item.QuiverItem;
import com.nyfaria.nyfsquiver.ui.QuiverScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {


    @Shadow
    protected abstract Player getCameraPlayer();

    @Shadow protected abstract void renderSlot(GuiGraphics guiGraphics, int i, int j, float f, Player player, ItemStack itemStack, int k);

    @Unique
    private ItemStack quiverStack;
    @Unique
    private ItemStack stackInQuiver;


    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getOffhandItem()Lnet/minecraft/world/item/ItemStack;",
                    shift = At.Shift.BY,
                    by = 2
            ),
            method = "renderHotbar"
    )
    private void getQuiverStacksFromPlayer(float f, GuiGraphics guiGraphics, CallbackInfo ci) {
        quiverStack = QuiverItem.getEquippedQuiver(getCameraPlayer());
        if (quiverStack.isEmpty()) return;
        if (!(getCameraPlayer().getMainHandItem().getItem() instanceof ProjectileWeaponItem)) return;
        QuiverScreenHandler quiverContainer = new QuiverScreenHandler(0, getCameraPlayer().getInventory(), quiverStack);
        if (!quiverStack.isEmpty()) {
            if (!quiverStack.getOrCreateTag().contains("current_slot")) {
                quiverStack.getOrCreateTag().putInt("current_slot", 0);
            }
            int curSlot = quiverStack.getOrCreateTag().getInt("current_slot");
            stackInQuiver = quiverContainer.getSlot(curSlot).getItem();

        }
    }


    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"
            ),
            method = "renderHotbar"
    )
    private void renderQuiverCurSlotInHotbar(float f, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (!(getCameraPlayer().getMainHandItem().getItem() instanceof ProjectileWeaponItem)) return;
        if (quiverStack != null && !quiverStack.isEmpty()) {
            int x = NyfsQuivers.getInstance().CONFIG.xpos;
            int y = NyfsQuivers.getInstance().CONFIG.ypos;
            int currentslot = quiverStack.getOrCreateTag().getInt("current_slot");
            guiGraphics.blit(WIDGETS_LOCATION, x, y, 24, 22, 29, 24);

            Font font = Minecraft.getInstance().font;
            guiGraphics.pose().scale(.7f, .7f, .7f);
            guiGraphics.drawString(font, String.valueOf(currentslot + 1), Mth.floor((x * 1.42857142857f) + 5), Mth.floor((y * 1.42857142857f) + 5), 0xFFFFFFFF);
            guiGraphics.pose().scale(1.42857142857f, 1.42857142857f, 1.42857142857f);
        }
    }

    @Inject(
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/AttackIndicatorStatus;HOTBAR:Lnet/minecraft/client/AttackIndicatorStatus;"
            ),
            method = "renderHotbar"
    )
    private void renderQuiverSlotAfter(float f, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (!(getCameraPlayer().getMainHandItem().getItem() instanceof ProjectileWeaponItem)) return;
        if (stackInQuiver != null && !stackInQuiver.isEmpty()) {
            renderSlot(guiGraphics,NyfsQuivers.getInstance().CONFIG.xpos + 3, NyfsQuivers.getInstance().CONFIG.ypos + 3, f, getCameraPlayer(), stackInQuiver, 12);
        }
        //quiverStack = null;
        stackInQuiver = null;
    }
}
