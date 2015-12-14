package br.com.gamemods.universalcoinsserver.container;

import br.com.gamemods.universalcoinsserver.CommonProxy;
import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SlotCoinInput extends Slot
{
    public SlotCoinInput(IInventory inventory, int slotIndex, int x, int y)
    {
        super(inventory, slotIndex, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        if(stack == null) return true;
        Item item = stack.getItem();
        CommonProxy proxy = UniversalCoinsServer.proxy;
        return proxy.itemCoin == item || proxy.itemSmallCoinStack == item || proxy.itemSmallCoinBag == item
                || proxy.itemLargeCoinStack == item || proxy.itemLargeCoinBag == item;
    }
}
