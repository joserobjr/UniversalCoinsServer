package br.com.gamemods.universalcoinsserver.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public abstract class BlockRotary extends BlockContainer
{
    public static Random random = new Random();

    protected BlockRotary(Material material)
    {
        super(material);
    }

    protected void setFourWayRotation(World world, int x, int y, int z, EntityLivingBase player)
    {
        int rotation = MathHelper.floor_double((double) ((player.rotationYaw * 4.0f) / 360F) + 2.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, rotation, 2);
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
        }
    }

    protected void setSixWayRotation(World world, int x, int y, int z, EntityLivingBase player)
    {
        int l = MathHelper.floor_double((double) ((player.rotationYaw * 4F) / 360F) + 0.5D) & 3;

        switch (l)
        {
            case 0:
                world.setBlockMetadataWithNotify(x, y, z, 2, l);
                break;
            case 1:
                world.setBlockMetadataWithNotify(x, y, z, 5, l);
                break;
            case 2:
                world.setBlockMetadataWithNotify(x, y, z, 3, l);
                break;
            case 3:
                world.setBlockMetadataWithNotify(x, y, z, 4, l);
                break;
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack)
    {
        setSixWayRotation(world, x, y, z, player);
    }
}
