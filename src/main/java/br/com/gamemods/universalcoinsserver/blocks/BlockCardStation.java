package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.tile.TileCardStation;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockCardStation extends BlockContainer
{
    public BlockCardStation(CreativeTabs creativeTabs)
    {
        super(new Material(MapColor.stoneColor));
        setHardness(3.0F);
        setCreativeTab(creativeTabs);
        setBlockTextureName("universalcoins:blockTradeStation1");
        setResistance(30.0F);
        setBlockName("blockCardStation");
    }

    @Override
    public TileCardStation createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileCardStation();
    }
}
