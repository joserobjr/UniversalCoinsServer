package br.com.gamemods.universalcoinsserver.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class BlockRotary extends BlockContainer
{
    protected BlockRotary(Material material)
    {
        super(material);
    }

    protected void setFourWayRotation(World world, int x, int y, int z, EntityLivingBase player)
    {
        int rotation = MathHelper.floor_double((double) ((player.rotationYaw * 4.0f) / 360F) + 2.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, rotation, 2);
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
