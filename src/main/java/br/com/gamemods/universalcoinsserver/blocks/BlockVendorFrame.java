package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.tile.TileVendorFrame;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;

public class BlockVendorFrame extends BlockVendor
{
    public BlockVendorFrame(CreativeTabs creativeTabs)
    {
        super(new Material(MapColor.woodColor));
        setHardness(1.0f);
        setBlockName("blockVendorFrame");
        setBlockTextureName("minecraft:planks_oak");
        setResistance(6000.0F);
        setBlockBounds(0, 0, 0, 0, 0, 0);
        setCreativeTab(creativeTabs);
    }

    @Override
    public TileVendorFrame createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileVendorFrame();
    }
}
