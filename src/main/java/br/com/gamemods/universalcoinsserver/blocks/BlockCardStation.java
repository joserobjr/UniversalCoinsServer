package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.GuiHandler;
import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.tile.TileCardStation;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCardStation extends BlockRotary
{
    public BlockCardStation(CreativeTabs creativeTabs)
    {
        super(new Material(MapColor.stoneColor));
        setHardness(3.0F);
        setCreativeTab(creativeTabs);
        setBlockTextureName("universalcoins:blockTradeStation1");
        setResistance(30.0F);
        setBlockName("blockCardStation");
    }

    @Override
    public TileCardStation createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileCardStation();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int metadata, float sideX, float sideY, float sideZ)
    {
        TileCardStation tile;
        {
            TileEntity te = world.getTileEntity(x,y,z);
            if(!(te instanceof TileCardStation))
                return false;
            tile = (TileCardStation) te;
        }

        if(tile.isInUse(player))
        {
            player.addChatMessage(new ChatComponentTranslation("chat.warning.inuse"));
            return false;
        }

        player.openGui(UniversalCoinsServer.instance, GuiHandler.GUI_CARD_STATION, world, x, y, z);
        if(!(player.openContainer instanceof ContainerPlayer))
        {
            tile.setOpener(player);
            tile.scheduleUpdate();
        }

        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack)
    {
        setFourWayRotation(world, x, y, z, player);
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
}
