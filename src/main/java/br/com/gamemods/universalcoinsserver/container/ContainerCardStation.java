package br.com.gamemods.universalcoinsserver.container;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.tile.TileCardStation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCardStation extends Container
{
    private TileCardStation tile;
    private Integer lastHash;

    public ContainerCardStation(InventoryPlayer inventoryPlayer, TileCardStation tile)
    {
        this.tile = tile;

        addSlotToContainer(new SlotCard(tile, TileCardStation.SLOT_CARD, 152, 60));
        addSlotToContainer(new SlotCoinInput(tile, TileCardStation.SLOT_COIN, 152, 40));

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 119 + i * 18));

        for (int i = 0; i < 9; i++)
            addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 177));
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
            if (slot < 2)
            {
                if (!this.mergeItemStack(stackInSlot, 2, 38, true))
                {
                    return null;
                }
            }
            // places it into the tileEntity if possible since its in the player
            // inventory
            else
            {
                boolean foundSlot = false;
                for (int i = 0; i < 2; i++)
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
            tile.fillCoinSlot();
        }

        return stack;
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);
        tile.onContainerClosed(player);
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
        int hashCode = tile.state.hashCode();
        if(lastHash == null || lastHash != hashCode)
        {
            UniversalCoinsServer.logger.info("CardStation update. last:"+lastHash+" current:"+hashCode);
            tile.scheduleUpdate();
            lastHash = hashCode;
        }
    }
}
