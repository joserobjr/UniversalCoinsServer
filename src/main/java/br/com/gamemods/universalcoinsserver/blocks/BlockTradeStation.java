package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.CommonProxy;
import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.tile.TileTradeStation;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockTradeStation extends BlockContainer
{
    public BlockTradeStation(CreativeTabs creativeTabs)
    {
        super(new Material(MapColor.stoneColor));
        setHardness(3.0f);
        setCreativeTab(creativeTabs);
        setResistance(6000.0F);
        setBlockName("blockTradeStation");
    }

    @Override
    public TileTradeStation createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileTradeStation();
    }
}
