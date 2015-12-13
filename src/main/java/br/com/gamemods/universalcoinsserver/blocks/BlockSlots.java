package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.tile.TileSlots;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSlots extends BlockContainer
{
    public BlockSlots(CreativeTabs creativeTabs)
    {
        super(new Material(MapColor.stoneColor));
        setHardness(3.0F);
        setCreativeTab(creativeTabs);
        setResistance(30.0F);
        setBlockName("blockBandit");
    }

    @Override
    public TileSlots createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileSlots();
    }
}
