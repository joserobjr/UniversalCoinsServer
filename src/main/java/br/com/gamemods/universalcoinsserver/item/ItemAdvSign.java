package br.com.gamemods.universalcoinsserver.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemSign;

public class ItemAdvSign extends ItemSign
{
    public ItemAdvSign(CreativeTabs tabs)
    {
        setMaxStackSize(16);
        setCreativeTab(tabs);
        setTextureName("sign");
        setUnlocalizedName("itemUCSign");
    }
}
