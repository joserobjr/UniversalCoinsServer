package br.com.gamemods.universalcoinsserver.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPowerReceiver extends BlockContainer
{
    public BlockPowerReceiver(CreativeTabs creativeTabs)
    {
        super(new Material(MapColor.stoneColor));
        setHardness(3.0F);
        setCreativeTab(creativeTabs);
        setResistance(30.0F);
        setTextureName("universalcoins:blockPowerReceiver");
        setUnlocalizedName("blockPowerReceiver");
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return null;
    }
}
