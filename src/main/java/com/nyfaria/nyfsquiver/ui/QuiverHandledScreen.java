package com.nyfaria.nyfsquiver.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import com.nyfaria.nyfsquiver.api.Dimension;
import com.nyfaria.nyfsquiver.api.Rectangle;

public class QuiverHandledScreen extends AbstractContainerScreen<QuiverScreenHandler> {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("nyfsquiver", "textures/gui/quiver_container.png");
    private final static int TOP_OFFSET = 24;
    private final static int SLOT_SIZE = 18;
    private final static int WIDTH_PADDING = 14;
    private final static int INVENTORY_LABEL_EXTRA = 8;
    
    public QuiverHandledScreen(QuiverScreenHandler handler, Inventory player, Component title) {
        super(handler, player, title);
        
        Dimension dimension = handler.getDimension();
        this.imageWidth = dimension.getWidth();
        this.imageHeight = dimension.getHeight();
        this.titleLabelY = 7;
        this.inventoryLabelX = handler.getPlayerInvSlotPosition(dimension, 0, 0).x;
        this.inventoryLabelY = this.imageHeight - 94;
    }
    
    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        renderBackgroundTexture(matrices, new Rectangle(x, y, imageWidth, imageHeight), delta, 0xFFFFFFFF);
        RenderSystem.setShaderTexture(0, new ResourceLocation("textures/gui/container/hopper.png"));
        for (Slot slot : getMenu().slots) {
            this.blit(matrices, x + slot.x - 1, y + slot.y - 1, 43, 19, 18, 18);
        }
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }
    
    public void renderBackgroundTexture(PoseStack matrices, Rectangle bounds, float delta, int color) {
        float alpha = ((color >> 24) & 0xFF) / 255f;
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;
        RenderSystem.clearColor(red, green, blue, alpha);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int x = bounds.x, y = bounds.y, width = bounds.width, height = bounds.height;
        int xTextureOffset = 0;
        int yTextureOffset = 66;
        
        // 9 Patch Texture
        
        // Four Corners
        this.blit(matrices, x, y, 106 + xTextureOffset, 124 + yTextureOffset, 8, 8);
        this.blit(matrices, x + width - 8, y, 248 + xTextureOffset, 124 + yTextureOffset, 8, 8);
        this.blit(matrices, x, y + height - 8, 106 + xTextureOffset, 182 + yTextureOffset, 8, 8);
        this.blit(matrices, x + width - 8, y + height - 8, 248 + xTextureOffset, 182 + yTextureOffset, 8, 8);
        
        Matrix4f matrix = matrices.last().pose();
        // Sides
        blitdQuad(matrix, x + 8, x + width - 8, y, y + 8, getBlitOffset(), (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f, (124 + yTextureOffset) / 256f, (132 + yTextureOffset) / 256f);
        blitdQuad(matrix, x + 8, x + width - 8, y + height - 8, y + height, getBlitOffset(), (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f, (182 + yTextureOffset) / 256f, (190 + yTextureOffset) / 256f);
        blitdQuad(matrix, x, x + 8, y + 8, y + height - 8, getBlitOffset(), (106 + xTextureOffset) / 256f, (114 + xTextureOffset) / 256f, (132 + yTextureOffset) / 256f, (182 + yTextureOffset) / 256f);
        blitdQuad(matrix, x + width - 8, x + width, y + 8, y + height - 8, getBlitOffset(), (248 + xTextureOffset) / 256f, (256 + xTextureOffset) / 256f, (132 + yTextureOffset) / 256f, (182 + yTextureOffset) / 256f);
        
        // Center
        blitdQuad(matrix, x + 8, x + width - 8, y + 8, y + height - 8, getBlitOffset(), (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f, (132 + yTextureOffset) / 256f, (182 + yTextureOffset) / 256f);
    }
    
    private static void blitdQuad(Matrix4f matrices, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrices, (float)x0, (float)y1, (float)z).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrices, (float)x1, (float)y1, (float)z).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrices, (float)x1, (float)y0, (float)z).uv(u1, v0).endVertex();
        bufferBuilder.vertex(matrices, (float)x0, (float)y0, (float)z).uv(u0, v0).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }
}
