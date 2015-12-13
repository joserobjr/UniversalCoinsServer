package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.CommonProxy;
import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.tile.TileVendor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlockVendor extends BlockContainer
{
    public BlockVendor(CreativeTabs creativeTabs)
    {
        super(Material.glass);
        setBlockName("blockVendor");
        setStepSound(soundTypeGlass);
        setCreativeTab(creativeTabs);
        setHardness(0.3F);
        setResistance(6000.0F);
        setBlockBounds(0.0625f, 0.125f, 0.0625f, 0.9375f, 0.9375f, 0.9375f);
    }

    protected BlockVendor(Material material)
    {
        super(material);
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public int damageDropped(int meta)
    {
        return meta;
    }

    public ItemStack getItemStack(World world, int x, int y, int z)
    {
        return new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        if (player.capabilities.isCreativeMode)
            return super.removedByPlayer(world, player, x, y, z, willHarvest);

        TileVendor tile;
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if (!(te instanceof TileVendor))
                return true;
            tile = (TileVendor) te;
        }

        return player.getPersistentID().equals(tile.owner) && super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
    {
        TileEntity tile = world.getTileEntity(x, y, z);
        if((tile instanceof TileVendor) && player.getPersistentID().equals(((TileVendor)tile).owner))
        {
            this.setHardness(0.3F);
        }
        else
        {
            this.setHardness(-1.0F);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack)
    {
        TileVendor tile;
        {
            TileEntity te = world.getTileEntity(x, y, z);
            tile = (te instanceof TileVendor)? (TileVendor) te : null;
        }

        if(tile != null)
        {
            NBTTagCompound tagCompound = stack.stackTagCompound;
            if (tagCompound == null) tagCompound = new NBTTagCompound();

            String string = tagCompound.getString("BlockOwner");
            if (!string.isEmpty())
                try
                {
                    tile.owner = UUID.fromString(string);
                }
                catch (Exception e)
                {
                    tile.owner = entity.getPersistentID();
                }
            else
                tile.owner = entity.getPersistentID();

            world.markBlockForUpdate(x,y,z);
        }

        world.setBlockMetadataWithNotify(x, y, z, stack.getItemDamage(), 2);
    }

    @Override
    public TileVendor createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileVendor();
    }

    @Override
    public int getRenderType()
    {
        return 0;
    }
}
