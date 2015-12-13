package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.tile.TileSignal;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSignal extends BlockContainer
{
    public BlockSignal(CreativeTabs tabs)
    {
        super(new Material(MapColor.stoneColor));
        setHardness(3.0F);
        setCreativeTab(tabs);
        setResistance(30.0F);
        setBlockTextureName("universalcoins:blockSignal");
        setBlockName("blockSignal");
    }

    @Override
    public TileSignal createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileSignal();
    }
}
