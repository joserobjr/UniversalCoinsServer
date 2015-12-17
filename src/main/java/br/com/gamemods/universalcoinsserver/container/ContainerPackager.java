package br.com.gamemods.universalcoinsserver.container;

import br.com.gamemods.universalcoinsserver.tile.TilePackager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPackager extends Container
{
    private TilePackager packager;

    public ContainerPackager(InventoryPlayer inventoryPlayer, TilePackager tilePackager)
    {
        this.packager = tilePackager;

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 2; j++)
                addSlotToContainer(new Slot(tilePackager, i * 2 + j, 8 + i * 18, 22 + j * 18));

        addSlotToContainer(new SlotCard(tilePackager, TilePackager.SLOT_CARD, 8, 73));
        addSlotToContainer(new SlotCoinInput(tilePackager, TilePackager.SLOT_COIN_INPUT, 26, 73));
        addSlotToContainer(new SlotOutput(tilePackager, TilePackager.SLOT_OUTPUT, 152, 73));
        addSlotToContainer(new SlotPackage(tilePackager, TilePackager.SLOT_PACKAGE_INPUT, Integer.MAX_VALUE, 26));

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 108 + i * 18));

        for (int i = 0; i < 9; i++)
            addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 166));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return packager.isUseableByPlayer(player);
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
            if (slot < 11)
            {
                if (!this.mergeItemStack(stackInSlot, 11, 47, true))
                {
                    return null;
                }
            }
            // places it into the tileEntity is possible since its in the player
            // inventory
            else
            {
                boolean foundSlot = false;
                for (int i = 4 - packager.packageSize * 2; i < 11; i++)
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
        packager.onContainerClosed(player);
    }
}
