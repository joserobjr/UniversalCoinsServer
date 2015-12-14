package br.com.gamemods.universalcoinsserver.container;

import br.com.gamemods.universalcoinsserver.tile.TileVendor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerVendor extends Container
{
    private TileVendor tile;

    public ContainerVendor(InventoryPlayer playerInventory, TileVendor tile)
    {
        this.tile = tile;

        addSlotToContainer(new SlotGhost(tile, TileVendor.SLOT_TRADE, 9, 17));
        addSlotToContainer(new SlotCoinInput(tile, TileVendor.SLOT_COIN_INPUT, 35, 55));
        addSlotToContainer(new SlotCard(tile, TileVendor.SLOT_CARD, 17, 55));
        addSlotToContainer(new SlotOutput(tile, TileVendor.SLOT_COIN_OUTPUT, 152, 55));

        for(int i = 0; i < 9; i++)
            addSlotToContainer(new Slot(tile, i, 8 + i * 18, 96));

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 119 + i * 18));

        for (int i = 0; i < 9; i++)
            addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 177));
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
            if (slot < 13)
            {
                if (!this.mergeItemStack(stackInSlot, 13, 49, true))
                {
                    return null;
                }
            }
            // places it into the tileEntity is possible since its in the player
            // inventory
            else
            {
                boolean foundSlot = false;
                for (int i = 1; i < 13; i++) // we start at 1 to avoid shift
                {
                    // clicking into trade slot
                    if (((Slot) inventorySlots.get(i)).isItemValid(stackInSlot)
                            && this.mergeItemStack(stackInSlot, i, i + 1, false)) {
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
            }
            else
            {
                slotObject.onSlotChanged();
            }

            if (stackInSlot.stackSize == stack.stackSize) {
                return null;
            }
            slotObject.onPickupFromSlot(player, stackInSlot);
        }

        return stack;
    }
}
