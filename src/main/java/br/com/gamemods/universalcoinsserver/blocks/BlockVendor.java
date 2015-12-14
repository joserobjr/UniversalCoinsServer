package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.GuiHandler;
import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.tile.TileVendor;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

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

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int metadata, float sideX, float sideY, float sideZ)
    {
        TileVendor tile;
        {
            TileEntity te = world.getTileEntity(x,y,z);
            if(!(te instanceof TileVendor))
                return false;
            tile = (TileVendor) te;
        }

        ItemStack heldItem = player.getHeldItem();
        int open;
        if(heldItem != null && heldItem.getItem() == UniversalCoinsServer.proxy.itemVendorWrench)
            open = GuiHandler.GUI_VENDOR_WRENCH;
        else if(player.getPersistentID().equals(tile.owner))
            open = GuiHandler.GUI_VENDOR_OWNER;
        else if(tile.price <= 0 || tile.getStackInSlot(TileVendor.SLOT_TRADE) == null)
            return false;
        else if(tile.sellMode)
            open = GuiHandler.GUI_VENDOR_SELL;
        else
            open = GuiHandler.GUI_VENDOR_BUY;

        if(tile.isInUse(player))
        {
            player.addChatMessage(new ChatComponentTranslation("chat.warning.inuse"));
            return false;
        }

        player.openGui(UniversalCoinsServer.instance, open, world, x, y, z);
        if(!(player.openContainer instanceof ContainerPlayer))
        {
            tile.opener = player;
            tile.scheduleUpdate();
        }

        return true;
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
