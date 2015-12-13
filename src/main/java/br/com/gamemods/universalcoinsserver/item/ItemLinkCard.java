package br.com.gamemods.universalcoinsserver.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemLinkCard extends Item
{
    public ItemLinkCard(CreativeTabs tabs)
    {
        setCreativeTab(tabs);
        setMaxStackSize(1);
        setUnlocalizedName("itemLinkCard");
    }
}
