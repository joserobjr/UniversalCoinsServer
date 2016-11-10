package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.tile.TilePowerBase;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPowerBase extends BlockContainer
{
    public BlockPowerBase(CreativeTabs tabs)
    {
        super(new Material(MapColor.stoneColor));
        setHardness(3.0F);
        setCreativeTab(tabs);
        setResistance(30.0F);
        setTextureName("universalcoins:blockPowerBase");
        setUnlocalizedName("blockPowerBase");
    }

    @Override
    public TilePowerBase createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TilePowerBase();
    }
}
