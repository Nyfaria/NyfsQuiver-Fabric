package thenyfaria.nyfsquivers.ui;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import thenyfaria.nyfsquivers.NyfsQuivers;
import thenyfaria.nyfsquivers.api.Dimension;
import thenyfaria.nyfsquivers.api.Point;
import thenyfaria.nyfsquivers.config.QuiverInfo;
import thenyfaria.nyfsquivers.item.QuiverItem;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import thenyfaria.nyfsquivers.util.InventoryUtils;

public class QuiverScreenHandler extends AbstractContainerMenu {

    ItemStack quiverStack;
    private final int padding = 8;
    private final int titleSpace = 10;

    //private ItemStack quiver;
    public QuiverScreenHandler(int synchronizationID, Inventory playerInventory, FriendlyByteBuf packetByteBuf) {
        this(synchronizationID, playerInventory, packetByteBuf.readItem());
    }
    public QuiverScreenHandler(int synchronizationID, Inventory playerInventory, ItemStack quiverStack) {
        super(NyfsQuivers.CONTAINER_TYPE, synchronizationID);
        this.quiverStack = quiverStack;

        if (quiverStack.getItem() instanceof QuiverItem) {
            setupContainer(playerInventory, quiverStack);
        } else {
            Player player = playerInventory.player;
            this.removed(player);
        }
    }
    private void setupContainer(Inventory playerInventory, ItemStack quiverStack) {
        Dimension dimension = getDimension();
        QuiverInfo tier = getItem().getTier();
        int rowWidth = tier.getRowWidth();
        int numberOfRows = tier.getNumberOfRows();

        ListTag tag = quiverStack.getOrCreateTag().getList("Inventory", NbtType.COMPOUND);
        SimpleContainer inventory = new SimpleContainer(rowWidth * numberOfRows) {
            @Override
            public void setChanged() {
                quiverStack.getOrCreateTag().put("Inventory", InventoryUtils.toTag(this));
                super.setChanged();
            }
        };

        InventoryUtils.fromTag(tag, inventory);

        for (int y = 0; y < numberOfRows; y++) {
            for (int x = 0; x < rowWidth; x++) {
                Point quiverSlotPosition = getQuiverSlotPosition(dimension, x, y);
                addSlot(new QuiverLockedSlot(inventory, y * rowWidth + x, quiverSlotPosition.x + 1, quiverSlotPosition.y + 1));
            }
        }

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                Point playerInvSlotPosition = getPlayerInvSlotPosition(dimension, x, y);
                this.addSlot(new QuiverLockedSlot(playerInventory, x + y * 9 + 9, playerInvSlotPosition.x + 1, playerInvSlotPosition.y + 1));
            }
        }

        for (int x = 0; x < 9; ++x) {
            Point playerInvSlotPosition = getPlayerInvSlotPosition(dimension, x, 3);
            this.addSlot(new QuiverLockedSlot(playerInventory, x, playerInvSlotPosition.x + 1, playerInvSlotPosition.y + 1));
        }
    }

    public QuiverItem getItem() {
        return (QuiverItem) quiverStack.getItem();
    }

    public Dimension getDimension() {
        QuiverInfo tier = getItem().getTier();
        return new Dimension(padding * 2 + Math.max(tier.getRowWidth(), 9) * 18, padding * 2 + titleSpace * 2 + 8 + (tier.getNumberOfRows() + 4) * 18);
    }

    public Point getQuiverSlotPosition(Dimension dimension, int x, int y) {
        QuiverInfo tier = getItem().getTier();
        return new Point(dimension.getWidth() / 2 - tier.getRowWidth() * 9 + x * 18, padding + titleSpace + y * 18);
    }

    public Point getPlayerInvSlotPosition(Dimension dimension, int x, int y) {
        QuiverInfo tier = getItem().getTier();
        return new Point(dimension.getWidth() / 2 - 9 * 9 + x * 18, dimension.getHeight() - padding - 4 * 18 - 3 + y * 18 + (y == 3 ? 4 : 0));
    }

    @Override
    public boolean stillValid(Player player) {
        return quiverStack.getItem() instanceof QuiverItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack toInsert = slot.getItem();
            itemStack = toInsert.copy();
            QuiverInfo tier = getItem().getTier();
            if (index < tier.getNumberOfRows() * tier.getRowWidth()) {
                if (!this.moveItemStackTo(toInsert, tier.getNumberOfRows() * tier.getRowWidth(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(toInsert, 0, tier.getNumberOfRows() * tier.getRowWidth(), false)) {
                return ItemStack.EMPTY;
            }

            if (toInsert.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    private class QuiverLockedSlot extends Slot {

        public QuiverLockedSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPickup(Player playerEntity) {
            return stackMovementIsAllowed(getItem());
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            //if(stack.getItem() instanceof FireworkRocketItem || stack.getItem() instanceof ArrowItem)
                return stackMovementIsAllowed(stack);

            //return false;
        }

        private boolean stackMovementIsAllowed(ItemStack stack) {
            return !(stack.getItem() instanceof QuiverItem) && stack != quiverStack &&(stack.getItem() instanceof FireworkRocketItem || stack.getItem() instanceof ArrowItem);
        }
    }
   
}
