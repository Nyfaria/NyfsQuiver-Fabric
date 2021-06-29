package thenyfaria.nyfsquivers.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import thenyfaria.nyfsquivers.NyfsQuivers;
import thenyfaria.nyfsquivers.config.NyfsQuiversConfig;
import thenyfaria.nyfsquivers.item.QuiverItem;
import thenyfaria.nyfsquivers.ui.QuiverScreenHandler;

import java.awt.*;
import java.util.List;
import java.util.Optional;

@Mixin(Gui.class)
public class GuiMixin {

    @Shadow
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    @Shadow
    private final Minecraft minecraft;
    @Shadow
    private final ItemRenderer itemRenderer;
    @Shadow
    private int screenWidth;
    @Shadow
    private int screenHeight;

    public GuiMixin(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.itemRenderer = minecraft.getItemRenderer();
    }

    @Shadow
    private void renderSlot(int i, int j, float f, Player player, ItemStack itemStack, int k) {
        if (!itemStack.isEmpty()) {
            PoseStack poseStack = RenderSystem.getModelViewStack();
            float g = (float)itemStack.getPopTime() - f;
            if (g > 0.0F) {
                float h = 1.0F + g / 5.0F;
                poseStack.pushPose();
                poseStack.translate((double)(i + 8), (double)(j + 12), 0.0D);
                poseStack.scale(1.0F / h, (h + 1.0F) / 2.0F, 1.0F);
                poseStack.translate((double)(-(i + 8)), (double)(-(j + 12)), 0.0D);
                RenderSystem.applyModelViewMatrix();
            }

            this.itemRenderer.renderAndDecorateItem(player, itemStack, i, j, k);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            if (g > 0.0F) {
                poseStack.popPose();
                RenderSystem.applyModelViewMatrix();
            }

            this.itemRenderer.renderGuiItemDecorations(this.minecraft.font, itemStack, i, j);
        }
    }



    @Overwrite
    private void renderHotbar(float f, PoseStack poseStack) {
        NyfsQuiversConfig defaultConfig = new NyfsQuiversConfig();
        int x = NyfsQuivers.CONFIG.xpos;
        int y = NyfsQuivers.CONFIG.ypos;
        Player player = this.getCameraPlayer();
        if (player != null) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
            ItemStack itemStack = player.getOffhandItem();
            ItemStack stackInQuiver = ItemStack.EMPTY;
            Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
            ItemStack beep = ItemStack.EMPTY;
            int currentslot = 0;
            if(component.isPresent()) {
                List<Tuple<SlotReference, ItemStack>> allEquipped = component.get().getAllEquipped();
                for (Tuple<SlotReference, ItemStack> entry : allEquipped) {
                    beep = entry.getB();
                    if (entry.getB().getItem() instanceof QuiverItem) {
                        QuiverScreenHandler quiverContainer = new QuiverScreenHandler(0, player.getInventory(), beep);
                        if (!beep.isEmpty()) {
                            if(!beep.getOrCreateTag().contains("current_slot")){
                                beep.getOrCreateTag().putInt("current_slot",0);
                            }
                            currentslot =beep.getOrCreateTag().getInt("current_slot");
                            stackInQuiver = quiverContainer.getSlot(currentslot).getItem();
                        }
                    }
                }
            }
            HumanoidArm humanoidArm = player.getMainArm().getOpposite();
            int i = this.screenWidth / 2;
            int j = ((Gui)(Object)this).getBlitOffset();
            ((Gui)(Object)this).setBlitOffset(-90);
            ((Gui)(Object)this).blit(poseStack, i - 91, this.screenHeight - 22, 0, 0, 182, 22);
            ((Gui)(Object)this).blit(poseStack, i - 91 - 1 + player.getInventory().selected * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
            if (!itemStack.isEmpty()) {
                if (humanoidArm == HumanoidArm.LEFT) {
                    ((Gui)(Object)this).blit(poseStack, i - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
                } else {
                    ((Gui)(Object)this).blit(poseStack, i + 91, this.screenHeight - 23, 53, 22, 29, 24);
                }
            }
            if(!beep.isEmpty())
            {
                //((Gui)(Object)this).blit(poseStack, i - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
                ((Gui)(Object)this).blit(poseStack, x, y, 24, 22, 29, 24);

                Font font = Minecraft.getInstance().font;
                poseStack.scale(.7f,.7f,.7f);
                font.draw(poseStack,String.valueOf(currentslot + 1),(x * 1.42857142857f) + 5,(y * 1.42857142857f) + 5, 0xFFFFFFFF);
                poseStack.scale(1.42857142857f,1.42857142857f,1.42857142857f);

            }

            ((Gui)(Object)this).setBlitOffset(j);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int m = 1;

            int q;
            int r;
            int s;
            for(q = 0; q < 9; ++q) {
                r = i - 90 + q * 20 + 2;
                s = this.screenHeight - 16 - 3;
                this.renderSlot(r, s, f, player, (ItemStack)player.getInventory().items.get(q), m++);
            }

            if (!itemStack.isEmpty()) {
                q = this.screenHeight - 16 - 3;
                if (humanoidArm == HumanoidArm.LEFT) {
                    this.renderSlot(i - 91 - 26, q, f, player, itemStack, m++);
                } else {
                    this.renderSlot(i + 91 + 10, q, f, player, itemStack, m++);
                }
            }
            if(!stackInQuiver.isEmpty())
            {
                this.renderSlot( x + 3, y + 3, f, player, stackInQuiver, m++);
            }

            if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.HOTBAR) {
                float g = this.minecraft.player.getAttackStrengthScale(0.0F);
                if (g < 1.0F) {
                    r = this.screenHeight - 20;
                    s = i + 91 + 6;
                    if (humanoidArm == HumanoidArm.RIGHT) {
                        s = i - 91 - 22;
                    }

                    RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
                    int t = (int)(g * 19.0F);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    ((Gui)(Object)this).blit(poseStack, s, r, 0, 94, 18, 18);
                    ((Gui)(Object)this).blit(poseStack, s, r + 18 - t, 18, 112 - t, 18, t);
                }
            }

            RenderSystem.disableBlend();
        }
    }


    @Shadow
    private Player getCameraPlayer() {
        return null;
    }
}
