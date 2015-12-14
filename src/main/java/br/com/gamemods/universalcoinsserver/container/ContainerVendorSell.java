package br.com.gamemods.universalcoinsserver.container;

import br.com.gamemods.universalcoinsserver.tile.TileVendor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerVendorSell extends Container
{
    private TileVendor tile;
    private int lastPrice, lastUserCoins, lastOwnerCoins;

    public ContainerVendorSell(InventoryPlayer inventoryPlayer, TileVendor tile)
    {
        this.tile = tile;

        addSlotToContainer(new SlotReadOnly(tile, TileVendor.SLOT_TRADE, 35, 24));
        addSlotToContainer(new SlotOutput(tile, TileVendor.SLOT_OUTPUT, 152, 24));
        addSlotToContainer(new SlotCard(tile, TileVendor.SLOT_USER_CARD, 17, 57));
        addSlotToContainer(new SlotCoinInput(tile, TileVendor.SLOT_USER_COIN_INPUT, 35, 57));
        addSlotToContainer(new SlotOutput(tile, TileVendor.SLOT_COIN_OUTPUT, 152, 57));

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 108 + i * 18));

        for (int i = 0; i < 9; i++)
            addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 166));
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if (lastPrice != tile.price || lastUserCoins != tile.userCoins || lastOwnerCoins != tile.ownerCoins)
        {
            tile.scheduleUpdate();
            lastPrice = tile.price;
            lastUserCoins = tile.userCoins;
            lastOwnerCoins = tile.ownerCoins;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot)
    {
        ItemStack stack = null;
        Slot slotObject = (Slot) inventorySlots.get(slot);
        // null checks and checks if the item can be stacked (maxStackSize > 1)
        if (slotObject != null && slotObject.getHasStack())
        {
            ItemStack stackInSlot = slotObject.getStack();
            stack = stackInSlot.copy();

            // merges the item into player inventory since its in the tileEntity
            if (slot < 5)
            {
                if (!this.mergeItemStack(stackInSlot, 5, 41, true))
                {
                    return null;
                }
            }
            // places it into the tileEntity is possible since its in the player
            // inventory
            else
            {
                boolean foundSlot = false;
                for (int i = 0; i < 5; i++)
                {
                    if (((Slot) inventorySlots.get(i)).isItemValid(stackInSlot)
                            && this.mergeItemStack(stackInSlot, i, i + 1, false))
                    {
                        foundSlot = true;
                        break;
                    }
                }
                if (!foundSlot)
                {
                    return null;
                }
            }

            if (stackInSlot.stackSize == 0)
            {
                slotObject.putStack(null);
            } else
            {
                slotObject.onSlotChanged();
            }

            if (stackInSlot.stackSize == stack.stackSize)
            {
                return null;
            }
            slotObject.onPickupFromSlot(player, stackInSlot);
        }

        return stack;
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);
        tile.onContainerClosed(player);
    }
}
