package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.GuiHandler;
import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.tile.TileVendor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.UUID;

public class BlockVendor extends BlockOwned
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
        else if(tile.sellToUser)
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
            tile.setOpener(player);
            tile.scheduleUpdate();
        }

        return true;
    }

    @Override
    public float getNormalHardness()
    {
        return 0.3f;
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
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        if(!willHarvest)
        {
            TileVendor vendor;
            {
                TileEntity tileEntity = world.getTileEntity(x,y,z);
                if(!(tileEntity instanceof TileVendor))
                    return super.removedByPlayer(world, player, x, y, z, false);
                vendor = (TileVendor) tileEntity;
            }

            ArrayList<ItemStack> drops = new ArrayList<>();
            if(vendor.userCoins > 0)
                drops.addAll(UniversalCoinsServerAPI.createStacks(vendor.userCoins));
            if(vendor.ownerCoins > 0)
                drops.addAll(UniversalCoinsServerAPI.createStacks(vendor.ownerCoins));
            int sizeInventory = vendor.getSizeInventory();
            for(int i = 0; i < sizeInventory; i++)
            {
                if(i == TileVendor.SLOT_COIN_OUTPUT || i == TileVendor.SLOT_TRADE)
                    continue;
                ItemStack stackInSlot = vendor.getStackInSlot(i);
                if(stackInSlot != null)
                    drops.add(stackInSlot);
            }

            if(!super.removedByPlayer(world, player, x, y, z, false))
                return false;

            for(ItemStack drop: drops)
            {
                EntityItem item = new EntityItem(world, x,y+0.5f,z, drop);
                world.spawnEntityInWorld(item);
            }

            return true;
        }

        return super.removedByPlayer(world, player, x, y, z, true);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
        TileVendor tile;
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if(!(tileEntity instanceof TileVendor))
                return super.getDrops(world, x, y, z, metadata, fortune);
            tile = (TileVendor) tileEntity;

            if(tile.ownerCoins == 0
                    && tile.getStackInSlot(TileVendor.SLOT_OWNER_CARD) == null
                    && tile.getStackInSlot(TileVendor.SLOT_OWNER_COIN_INPUT) == null
                    && tile.getStackInSlot(TileVendor.SLOT_COIN_OUTPUT) == null
            ){
                boolean empty = true;
                for(int i = TileVendor.SLOT_STORAGE_FIST; i <= TileVendor.SLOT_STORAGE_LAST; i++)
                    if(tile.getStackInSlot(i) != null)
                    {
                        empty = false;
                        break;
                    }

                if(empty)
                {
                    ArrayList<ItemStack> drops = super.getDrops(world, x, y, z, metadata, fortune);
                    if(tile.userCoins > 0)
                        drops.addAll(UniversalCoinsServerAPI.createStacks(tile.userCoins));

                    ItemStack stack = tile.getStackInSlot(TileVendor.SLOT_USER_CARD);
                    if(stack != null) drops.add(stack);

                    stack = tile.getStackInSlot(TileVendor.SLOT_USER_COIN_INPUT);
                    if(stack != null) drops.add(stack);
                    return drops;
                }
            }
        }

        ArrayList<ItemStack> ret = new ArrayList<>(3);

        Item item = getItemDropped(metadata, world.rand, fortune);
        if (item != null)
        {
            ItemStack stack = new ItemStack(item, 1, damageDropped(metadata));
            if(stack.stackTagCompound == null)
                stack.stackTagCompound = new NBTTagCompound();

            ret.add(stack);

            if(tile.userCoins > 0)
            {
                ret.addAll(UniversalCoinsServerAPI.createStacks(tile.userCoins));
                tile.userCoins = 0;
            }

            ItemStack userStack = tile.getStackInSlot(TileVendor.SLOT_USER_CARD);
            if(userStack != null)
            {
                ret.add(stack);
                tile.setInventorySlotContents(TileVendor.SLOT_USER_CARD, null);
            }

            userStack = tile.getStackInSlot(TileVendor.SLOT_USER_COIN_INPUT);
            if(userStack != null)
            {
                ret.add(stack);
                tile.setInventorySlotContents(TileVendor.SLOT_USER_COIN_INPUT, null);
            }

            tile.writeToNBT(stack.stackTagCompound);
        }

        return ret;
    }
}
