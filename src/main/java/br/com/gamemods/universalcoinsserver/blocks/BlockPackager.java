package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.GuiHandler;
import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.tile.TilePackager;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;

public class BlockPackager extends BlockRotary
{
    public BlockPackager(CreativeTabs creativeTabs)
    {
        super(new Material(MapColor.stoneColor));
        setHardness(3.0F);
        setCreativeTab(creativeTabs);
        setResistance(30.0F);
        setBlockName("blockPackager");
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7,
                                    float par8, float par9)
    {
        TilePackager tile;
        {
            TileEntity te = world.getTileEntity(x,y,z);
            if(!(te instanceof TilePackager))
                return false;
            tile = (TilePackager) te;
        }

        if(tile.isInUse(player))
        {
            player.addChatMessage(new ChatComponentTranslation("chat.warning.inuse"));
            return false;
        }

        player.openGui(UniversalCoinsServer.instance, GuiHandler.GUI_PACKAGER, world, x, y, z);
        if(!(player.openContainer instanceof ContainerPlayer))
        {
            tile.setOpener(player);
            tile.scheduleUpdate();
        }

        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata)
    {
        TileEntity te = world.getTileEntity(x,y,z);
        if(te instanceof TilePackager)
        {
            TilePackager packager = (TilePackager) te;
            int size = packager.getSizeInventory();
            ArrayList<ItemStack> drops = new ArrayList<>(size+2);
            for(int i = 0; i < size; i++)
            {
                ItemStack stack = packager.getStackInSlot(i);
                if(stack != null && stack.stackSize > 0)
                {
                    drops.add(stack);
                    packager.setInventorySlotContents(i, null);
                }
            }

            drops.addAll(UniversalCoinsServerAPI.createStacks(packager.userCoins));

            drop(world, x, y, z, drops);
        }

        super.breakBlock(world, x, y, z, block, metadata);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TilePackager();
    }
}
