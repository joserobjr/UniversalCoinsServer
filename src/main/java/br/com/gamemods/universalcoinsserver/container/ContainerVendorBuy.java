package br.com.gamemods.universalcoinsserver.container;

import br.com.gamemods.universalcoinsserver.tile.TileVendor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerVendorBuy extends Container
{
    private TileVendor tile;
    private int lastPrice, lastUserCoins, lastOwnerCoins;

    public ContainerVendorBuy(InventoryPlayer inventoryPlayer, TileVendor tile)
    {
        this.tile = tile;

        addSlotToContainer(new SlotReadOnly(tile, TileVendor.SLOT_TRADE, 8, 24));
        addSlotToContainer(new Slot(tile, TileVendor.SLOT_SELL, 26, 24));
        addSlotToContainer(new SlotOutput(tile, TileVendor.SLOT_COIN_OUTPUT, 152, 64));
        addSlotToContainer(new SlotCard(tile, TileVendor.SLOT_USER_CARD, 43, 64));

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 117 + i * 18));

        for (int i = 0; i < 9; i++)
            addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 175));
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if(lastPrice != tile.price || lastUserCoins != tile.userCoins || lastOwnerCoins != tile.ownerCoins)
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
            if (slot < 4)
            {
                if (!this.mergeItemStack(stackInSlot, 4, 40, true))
                {
                    return null;
                }
            }
            // places it into the tileEntity is possible since its in the player
            // inventory
            else
            {
                boolean foundSlot = false;
                for (int i = 0; i < 4; i++)
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
