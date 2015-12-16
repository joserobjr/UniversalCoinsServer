package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.tile.TileSignal;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class BlockSignal extends BlockOwned
{
    public BlockSignal(CreativeTabs tabs)
    {
        super(new Material(MapColor.stoneColor));
        setHardness(3.0F);
        setCreativeTab(tabs);
        setResistance(30.0F);
        setBlockTextureName("universalcoins:blockSignal");
        setBlockName("blockSignal");
    }

    @Override
    public ArrayList<ItemStack> dropEverythingToList(World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(x,y,z);
        if(!(tileEntity instanceof TileSignal))
            return new ArrayList<>(0);

        TileSignal signal = (TileSignal) tileEntity;

        List<ItemStack> stacks = UniversalCoinsServerAPI.createStacks(signal.coins);
        int size = signal.getSizeInventory();
        ArrayList<ItemStack> drops = new ArrayList<>(stacks.size()+size);
        drops.addAll(stacks);
        signal.coins = 0;

        for(int i = 0; i < size; i++)
        {
            ItemStack stackInSlot = signal.getStackInSlot(i);
            if(stackInSlot != null)
            {
                signal.setInventorySlotContents(i, null);
                drops.add(stackInSlot);
            }
        }

        return drops;
    }

    @Override
    public ArrayList<ItemStack> dropToStack(World world, int x, int y, int z, int fortune)
    {
        TileEntity tileEntity = world.getTileEntity(x,y,z);
        if(!(tileEntity instanceof TileSignal))
            return super.dropToStack(world, x, y, z, fortune);
        TileSignal signal = (TileSignal) tileEntity;

        boolean empty = signal.coins == 0 && signal.ticks <= 0 && signal.fee == 1 && signal.duration == 1;
        if(empty)
        {
            int size = signal.getSizeInventory();
            for(int i = 0; i < size; i++)
                if(signal.getStackInSlot(i) != null)
                {
                    empty = false;
                    break;
                }
        }

        ArrayList<ItemStack> drops = new ArrayList<>(1);

        int metadata = world.getBlockMetadata(x,y,z);

        Item item = getItemDropped(metadata, random, fortune);
        if (item != null)
        {
            ItemStack stack = new ItemStack(item, 1, damageDropped(metadata));
            drops.add(stack);
            if(!empty)
            {
                NBTTagCompound tileData = new NBTTagCompound();
                signal.writeToStackNBT(tileData);
                stack.stackTagCompound = tileData;
            }
        }

        return drops;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
    {
        TileEntity te = world.getTileEntity(x,y,z);
        if(!(te instanceof TileSignal))
            return false;

        TileSignal tile = (TileSignal) te;
        tile.onLeftClick(player);
        return true;
    }

    @Override
    public TileSignal createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileSignal();
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess block, int x, int y, int z, int side)
    {
        TileEntity te = block.getTileEntity(x, y, z);
        if(te instanceof TileSignal)
        {
            TileSignal tile = (TileSignal) te;
            if(tile.providingPower)
                return 15;
        }

        return 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess block, int x, int y, int z, int side)
    {
        return isProvidingWeakPower(block, x, y, z, side);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack)
    {
        setFourWayRotation(world, x, y, z, player);
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileSignal)
        {
            TileSignal tile = (TileSignal) te;
            try
            {
                if(stack.hasTagCompound())
                {
                    tile.setWorldObj(world);
                    tile.readFromStackNBT(stack.stackTagCompound);
                }
            }
            finally
            {
                tile.owner = player.getPersistentID();
                tile.ownerName = player.getCommandSenderName();
            }
            tile.markDirty();
        }
    }

    @Override
    public boolean canProvidePower()
    {
        return true;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
        return false;
    }

    @Override
    public float getNormalHardness()
    {
        return 3f;
    }
}
