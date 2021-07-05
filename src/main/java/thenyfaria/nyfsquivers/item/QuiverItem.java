package thenyfaria.nyfsquivers.item;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import thenyfaria.nyfsquivers.NyfsQuivers;
import thenyfaria.nyfsquivers.config.QuiverInfo;
import thenyfaria.nyfsquivers.ui.QuiverScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;


public class QuiverItem extends Item implements TrinketRenderer {

    private final QuiverInfo quiver;
    private HumanoidModel<LivingEntity> model;

    public QuiverItem(QuiverInfo quiver, Item.Properties settings) {
        super(settings);
        this.quiver = quiver;

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        player.startUsingItem(interactionHand);

        if(NyfsQuivers.CONFIG.playSound) {
            if (level.isClientSide) {
                level.playSound(player, player.getOnPos(), Registry.SOUND_EVENT.get(new ResourceLocation(quiver.getOpenSound())), SoundSource.PLAYERS, 1, 1);
            }
        }

        openScreen(player, player.getItemInHand(interactionHand),interactionHand);
        return InteractionResultHolder.success(player.getItemInHand(interactionHand));
    }

    public static void openScreen(Player player, ItemStack quiverItemStack, InteractionHand hand) {
        if(player.level != null && !player.level.isClientSide) {
            player.openMenu(new ExtendedScreenHandlerFactory() {

                @Override
                public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                    buf.writeItem(quiverItemStack);
                }

                @Override
                public Component getDisplayName() {
                    return new TranslatableComponent(quiverItemStack.getItem().getDescriptionId());
                }

                @Override
                public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                    return new QuiverScreenHandler(syncId, inv, quiverItemStack);
                }
            });
        }
    }

    public int getWidth(){
        return quiver.getRowWidth();
    }
    public int getHeight(){
        return quiver.getNumberOfRows();
    }



    public QuiverInfo getTier() {
        return quiver;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> contextModel, PoseStack matrices, MultiBufferSource vertexConsumers, int light, LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        ItemRenderer blip = Minecraft.getInstance().getItemRenderer();
        float translate = 0f;
        if (entity.hasItemInSlot(EquipmentSlot.CHEST)) {
            translate = 0.06f;
        }

        matrices.pushPose();
        TrinketRenderer.translateToChest(matrices, (PlayerModel<AbstractClientPlayer>) contextModel,(AbstractClientPlayer)entity);
        matrices.translate(0, .5, translate);
        blip.renderStatic(entity, stack, ItemTransforms.TransformType.HEAD, true, matrices, vertexConsumers, entity.level,0xF000F0,0xF000F0, 0);
        matrices.popPose();
    }

}
