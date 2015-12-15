package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.tile.TileSignal;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSignal extends BlockRotary
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

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack)
    {
        setFourWayRotation(world, x, y, z, player);
    }

    @Override
    public boolean canProvidePower()
    {
        return true;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
        return false;
    }
}
