package br.com.gamemods.universalcoinsserver.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotGhost extends Slot
{
    public SlotGhost(IInventory inventory, int slotIndex, int x, int y)
    {
        super(inventory, slotIndex, x, y);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player)
    {
        putStack(null);
        return false;
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        if(stack != null)
            putStack(stack.copy());
        else
            putStack(null);

        return false;
    }
}
