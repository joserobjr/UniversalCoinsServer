package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.tile.TileSignal;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSignal extends BlockOwned
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
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
    {
        TileEntity te = world.getTileEntity(x,y,z);
        if(!(te instanceof TileSignal))
            return false;

        TileSignal tile = (TileSignal) te;
        tile.onLeftClick(player);
        return true;
    }

    @Override
    public TileSignal createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileSignal();
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess block, int x, int y, int z, int side)
    {
        TileEntity te = block.getTileEntity(x, y, z);
        if(te instanceof TileSignal)
        {
            TileSignal tile = (TileSignal) te;
            if(tile.providingPower)
                return 15;
        }

        return 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess block, int x, int y, int z, int side)
    {
        return isProvidingWeakPower(block, x, y, z, side);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack)
    {
        setFourWayRotation(world, x, y, z, player);
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileSignal)
        {
            TileSignal tile = (TileSignal) te;
            tile.owner = player.getPersistentID();
            tile.ownerName = player.getCommandSenderName();
            tile.markDirty();
        }
    }

    @Override
    public boolean canProvidePower()
    {
        return true;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
        return false;
    }

    @Override
    public float getNormalHardness()
    {
        return 3f;
    }
}
