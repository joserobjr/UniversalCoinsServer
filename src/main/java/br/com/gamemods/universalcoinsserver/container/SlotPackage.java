package br.com.gamemods.universalcoinsserver.container;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotPackage extends Slot
{
    public SlotPackage(IInventory inventory, int slot, int x, int y)
    {
        super(inventory, slot, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return stack == null || UniversalCoinsServer.proxy.itemPackage == stack.getItem();
    }
}
