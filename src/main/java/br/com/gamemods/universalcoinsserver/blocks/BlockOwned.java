package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.tile.TileVendor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class BlockOwned extends BlockRotary
{
    protected BlockOwned(Material material)
    {
        super(material);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        if (player.capabilities.isCreativeMode)
            return super.removedByPlayer(world, player, x, y, z, willHarvest);

        PlayerOwned tile;
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if (!(te instanceof TileVendor))
                return true;
            tile = (TileVendor) te;
        }

        return player.getPersistentID().equals(tile.getOwnerId()) && super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
    {
        TileEntity tile = world.getTileEntity(x, y, z);
        if((tile instanceof PlayerOwned) && player.getPersistentID().equals(((PlayerOwned)tile).getOwnerId()))
        {
            this.setHardness(getNormalHardness());
        }
        else
        {
            this.setHardness(-1.0F);
        }
    }

    public abstract float getNormalHardness();
}
