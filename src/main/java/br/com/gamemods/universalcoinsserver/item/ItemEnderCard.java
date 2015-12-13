package br.com.gamemods.universalcoinsserver.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemEnderCard extends Item
{
    public ItemEnderCard(CreativeTabs tabs)
    {
        setMaxStackSize(1);
        setCreativeTab(tabs);
        setUnlocalizedName("itemEnderCard");
    }
}
