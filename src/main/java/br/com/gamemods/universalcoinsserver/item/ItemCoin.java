package br.com.gamemods.universalcoinsserver.item;

import net.minecraft.item.Item;

public class ItemCoin extends Item
{
    private int value;
    public ItemCoin(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    @Deprecated
    public int getMaxStackSize()
    {
        return maxStackSize;
    }
}
