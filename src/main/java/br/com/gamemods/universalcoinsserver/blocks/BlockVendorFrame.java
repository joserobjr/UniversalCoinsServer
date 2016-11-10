package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.tile.TileVendorFrame;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockVendorFrame extends BlockVendor
{
    public BlockVendorFrame(CreativeTabs creativeTabs)
    {
        super(new Material(MapColor.woodColor));
        setHardness(1.0f);
        setUnlocalizedName("blockVendorFrame");
        setTextureName("minecraft:planks_oak");
        setResistance(6000.0F);
        setBlockBounds(0, 0, 0, 0, 0, 0);
        setCreativeTab(creativeTabs);
    }

    @Override
    public TileVendorFrame createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileVendorFrame();
    }

    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack)
    {
        super.onBlockPlacedBy(world, x, y, z, entity, stack);
        int rotation = MathHelper.floor_double((double) ((entity.rotationYaw * 4.0f) / 360F) + 2.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, rotation, 2);

        TileEntity tileEntity = world.getTileEntity(x,y,z);
        if(tileEntity instanceof TileVendorFrame)
        {
            TileVendorFrame tile = (TileVendorFrame) tileEntity;
            if(tile.icon.isEmpty())
                tile.icon = "planks_birch";
        }
    }

    @Override
    public int damageDropped(int meta)
    {
        return 0;
    }

    @Override
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess block, int x, int y, int z)
    {
        this.getBlockBoundsFromMeta(block.getBlockMetadata(x, y, z));
    }

    public void getBlockBoundsFromMeta(int meta)
    {
        if (meta == 0)
            this.setBlockBounds(0.12f, 0.12f, 0f, 0.88f, 0.88f, 0.07f);
        else if (meta == 1)
            this.setBlockBounds(0.93f, 0.12f, 0.12f, 1.0f, 0.88f, 0.88f);
        else if (meta == 2)
            this.setBlockBounds(0.12f, 0.12f, 0.93f, 0.88f, 0.88f, 1.00f);
        else if (meta == 3)
            this.setBlockBounds(0.07f, 0.12f, 0.12f, 0f, 0.88f, 0.88f);
    }
}
