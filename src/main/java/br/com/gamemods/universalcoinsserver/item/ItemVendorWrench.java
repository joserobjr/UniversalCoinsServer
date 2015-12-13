package br.com.gamemods.universalcoinsserver.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class ItemVendorWrench extends Item
{
    public ItemVendorWrench(CreativeTabs tabs)
    {
        setFull3D();
        setMaxStackSize(1);
        setCreativeTab(tabs);
        setUnlocalizedName("itemVendorWrench");
    }

    @Override
    public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
    {
        return true;
    }
}
