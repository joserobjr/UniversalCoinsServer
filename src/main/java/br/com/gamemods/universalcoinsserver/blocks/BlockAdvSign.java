package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.GuiHandler;
import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.tile.TileAdvSign;
import net.minecraft.block.BlockSign;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

public class BlockAdvSign extends BlockSign
{
    public boolean standing;

    public BlockAdvSign(boolean standing)
    {
        super(TileAdvSign.class, standing);
        this.standing = standing;
        float f = 0.25F;
        float f1 = 1.0F;
        this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f1, 0.5F + f);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if(tileEntity instanceof TileAdvSign)
        {
            TileAdvSign tile = (TileAdvSign) tileEntity;
            if(player.getPersistentID().equals(tile.owner))
            {
                String name = player.getCommandSenderName();
                if(!name.equals(tile.ownerName))
                {
                    tile.ownerName = name;
                    tile.markDirty();
                    tile.scheduleUpdate();
                }

                player.openGui(UniversalCoinsServer.instance, GuiHandler.GUI_ADV_SIGN, world, x,y,z);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        TileAdvSign tile;
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if (player.capabilities.isCreativeMode || !(te instanceof TileAdvSign))
                return super.removedByPlayer(world, player, x, y, z, willHarvest);

            tile = (TileAdvSign) te;
        }

        return (tile.owner == null || player.getPersistentID().equals(tile.owner)) && super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
    {
        TileAdvSign tile;
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if(player.capabilities.isCreativeMode || !(te instanceof TileAdvSign))
                tile = null;
            else
                tile = (TileAdvSign) te;

            if(tile != null && player.getPersistentID().equals(tile.owner) && !player.getCommandSenderName().equals(tile.ownerName))
            {
                tile.ownerName = player.getCommandSenderName();
                tile.markDirty();
                tile.scheduleUpdate();
            }
        }

        if(tile == null || player.getPersistentID().equals(tile.owner))
            this.setHardness(1.0F);
        else
            this.setHardness(-1.0f);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
        ArrayList<ItemStack> ret = new ArrayList<>(1);
        Item item = getItemDropped(metadata, world.rand, fortune);
        if (item != null)
        {
            ItemStack stack = new ItemStack(item, 1, damageDropped(metadata));

            TileEntity tileEntity = world.getTileEntity(x,y,z);
            if(tileEntity instanceof TileAdvSign)
            {
                TileAdvSign tile = (TileAdvSign) tileEntity;
                stack.setTagInfo("BlockIcon", new NBTTagString(tile.icon));
            }

            ret.add(stack);
        }

        return ret;
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileAdvSign();
    }

    @Override
    public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_)
    {
        return UniversalCoinsServer.proxy.itemAdvSign;
    }
}
