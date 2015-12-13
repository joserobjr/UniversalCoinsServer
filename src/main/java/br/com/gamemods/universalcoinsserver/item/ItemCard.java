package br.com.gamemods.universalcoinsserver.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemCard extends Item
{
    public ItemCard(CreativeTabs creativeTabs)
    {
        setMaxStackSize(1);
        setCreativeTab(creativeTabs);
        setUnlocalizedName("itemUCCard");
    }
}
