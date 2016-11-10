package br.com.gamemods.universalcoinsserver.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockBase extends Block
{
    public BlockBase(CreativeTabs creativeTabs)
    {
        super(new Material(MapColor.stoneColor));
        setHardness(3.0F);
        setCreativeTab(creativeTabs);
        setResistance(30.0F);
        setTextureName("universalcoins:blockBase");
        setUnlocalizedName("blockBase");
    }
}
