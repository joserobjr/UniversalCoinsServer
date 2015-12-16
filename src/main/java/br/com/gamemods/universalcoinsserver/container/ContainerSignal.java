package br.com.gamemods.universalcoinsserver.container;

import br.com.gamemods.universalcoinsserver.tile.TileSignal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerSignal extends Container
{
    private TileSignal tileSignal;

    public ContainerSignal(InventoryPlayer inventoryPlayer, TileSignal tileSignal)
    {
        this.tileSignal = tileSignal;

        addSlotToContainer(new SlotOutput(tileSignal, TileSignal.SLOT_COIN_OUTPUT, 148, 73));

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 108 + i * 18));

        for (int i = 0; i < 9; i++)
            addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 166));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return tileSignal.isUseableByPlayer(player);
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
            if (slot < 3)
            {
                if (!this.mergeItemStack(stackInSlot, 3, 39, true))
                {
                    return null;
                }
                else
                {
                    //tileSignal.coinsTaken(stack);
                }
            }
            // places it into the tileEntity is possible since its in the player
            // inventory
            else
            {
                boolean foundSlot = false;
                for (int i = 0; i < 3; i++)
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
}
