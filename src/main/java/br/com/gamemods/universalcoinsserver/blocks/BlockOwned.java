package br.com.gamemods.universalcoinsserver.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class BlockOwned extends BlockRotary
{
    protected BlockOwned(Material material)
    {
        super(material);
    }

    @Override
    public final boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        if(!canRemoveBlock(world, player, x,y,z,willHarvest))
            return false;

        if(!willHarvest)
        {
            drop(world, x, y, z, dropEverythingToList(world, x, y, z));
        }
        else
        {
            List<ItemStack> drops = dropNonOwnerContentToList(world, x, y, z);
            drops.addAll(dropToStack(world, x, y, z, 0));
            drop(world, x, y, z, drops);
        }

        world.removeTileEntity(x,y,z);
        return world.setBlockToAir(x,y,z);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
        drop(world, x,y,z, dropEverythingToList(world, x, y, z));
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    protected void dropBlockAsItem(World world, int x, int y, int z, ItemStack stack)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if(!(tileEntity instanceof PlayerOwned))
            return;

        super.dropBlockAsItem(world, x, y, z, stack);
    }

    @Override
    public final ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
        ArrayList<ItemStack> drops = dropEverythingToList(world, x,y,z);
        drops.addAll(dropToStack(world, x,y,z, 0));
        return drops;
    }

    public ArrayList<ItemStack> dropToStack(World world, int x, int y, int z, int fortune)
    {
        int metadata = world.getBlockMetadata(x,y,z);

        ArrayList<ItemStack> drops = new ArrayList<>();

        int count = quantityDropped(metadata, fortune, world.rand);
        for(int i = 0; i < count; i++)
        {
            Item item = getItemDropped(metadata, world.rand, fortune);
            if (item != null)
                drops.add(new ItemStack(item, 1, damageDropped(metadata)));
        }

        return drops;
    }

    public ArrayList<ItemStack> dropEverythingToList(World world, int x, int y, int z)
    {
        return new ArrayList<>(0);
    }

    public ArrayList<ItemStack> dropNonOwnerContentToList(World world, int x, int y, int z)
    {
        return new ArrayList<>(0);
    }

    @Override
    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity)
    {
        return canRemoveBlock(world, entity, x,y,z, true);
    }

    public boolean canRemoveBlock(IBlockAccess world, Entity entity, int x, int y, int z, boolean willHarvest)
    {
        if(entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.capabilities.isCreativeMode)
                return true;

            PlayerOwned tile;
            {
                TileEntity te = world.getTileEntity(x, y, z);
                if (!(te instanceof PlayerOwned))
                    return true;
                tile = (PlayerOwned) te;
            }

            return player.getPersistentID().equals(tile.getOwnerId());
        }

        return false;
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
