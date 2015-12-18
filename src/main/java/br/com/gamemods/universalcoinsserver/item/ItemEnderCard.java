package br.com.gamemods.universalcoinsserver.item;

import net.minecraft.creativetab.CreativeTabs;

public class ItemEnderCard extends ItemCard
{
    public ItemEnderCard(CreativeTabs tabs)
    {
        setMaxStackSize(1);
        setCreativeTab(tabs);
        setUnlocalizedName("itemEnderCard");
    }
}
