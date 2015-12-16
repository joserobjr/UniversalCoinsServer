package br.com.gamemods.universalcoinsserver.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class BlockOwned extends BlockRotary
{
    public static Random random = new Random();

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

    public void drop(World world, int x, int y, int z, List<ItemStack> drops)
    {
        for(ItemStack drop: drops)
        {
            if(drop == null) continue;

            float xRand = random.nextFloat() * 0.8F + 0.1F;
            float yRand = random.nextFloat() * 0.8F + 0.1F;
            float zRand = random.nextFloat() * 0.8F + 0.1F;

            EntityItem item;
            for (; drop.stackSize > 0; world.spawnEntityInWorld(item))
            {
                int amount = random.nextInt(21) + 10;

                if (amount > drop.stackSize)
                    amount = drop.stackSize;
                drop.stackSize -= amount;

                item = new EntityItem(world, x + xRand, y + yRand, z + zRand, new ItemStack(drop.getItem(), amount, drop.getItemDamage()));
                item.motionX = (double)((float)random.nextGaussian() * 0.05F);
                item.motionY = (double)((float)random.nextGaussian() * 0.05F + 0.2F);
                item.motionZ = (double)((float)random.nextGaussian() * 0.05F);

                if (drop.hasTagCompound())
                    item.getEntityItem().setTagCompound((NBTTagCompound)drop.getTagCompound().copy());
            }

            //EntityItem item = new EntityItem(world, x,y+0.5f,z, drop);
            //world.spawnEntityInWorld(item);
        }
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
