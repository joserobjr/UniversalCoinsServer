package br.com.gamemods.universalcoinsserver;

import br.com.gamemods.universalcoinsserver.container.*;
import br.com.gamemods.universalcoinsserver.tile.*;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler
{
    public static final int GUI_TRADE_STATION = 1,
            GUI_VENDOR_WRENCH = 2,
            GUI_VENDOR_OWNER = 3,
            GUI_VENDOR_SELL = 4,
            GUI_VENDOR_BUY = 5,
            GUI_CARD_STATION = 6,
            GUI_SAFE = 7,
            GUI_BANDIT = 9,
            GUI_BANDIT_WRENCH = 10,
            GUI_SIGNAL = 11,
            GUI_PACKAGER = 12,
            GUI_POWER_BASE = 13,
            GUI_POWER_RECEIVER = 14,
            GUI_ADV_SIGN = 15;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity te = world.getTileEntity(x,y,z);
        switch (ID)
        {
            case GUI_VENDOR_WRENCH:
            case GUI_VENDOR_OWNER:
            case GUI_VENDOR_SELL:
            case GUI_VENDOR_BUY:
                if(te instanceof TileVendor)
                {
                    TileVendor tile = (TileVendor) te;
                    switch (ID){
                        case GUI_VENDOR_WRENCH: return new ContainerVendorWrench(tile);
                        case GUI_VENDOR_OWNER: return new ContainerVendor(player.inventory, tile);
                        case GUI_VENDOR_SELL: return new ContainerVendorSell(player.inventory, tile);
                        default: return new ContainerVendorBuy(player.inventory, tile);
                    }
                }
                else return null;
            case GUI_CARD_STATION:
                if(te instanceof TileCardStation)
                    return new ContainerCardStation(player.inventory, (TileCardStation)te);
                else return null;
            case GUI_BANDIT:
                if(te instanceof TileSlots)
                    return new ContainerSlots(player.inventory, (TileSlots)te);
                else return null;
            case GUI_SIGNAL:
                if(te instanceof TileSignal)
                    return new ContainerSignal(player.inventory, (TileSignal)te);
                else return null;
            case GUI_PACKAGER:
                if(te instanceof TilePackager)
                    return new ContainerPackager(player.inventory, (TilePackager)te);
                else return null;
            case GUI_TRADE_STATION:
            case GUI_ADV_SIGN:
                return null;
        }

        if(te instanceof TileVendor)
        {
            TileVendor tile = (TileVendor) te;
            if (tile.owner == null || player.getPersistentID().equals(tile.owner))
                return new ContainerVendor(player.inventory, tile);
            else if (tile.sellToUser)
                return new ContainerVendorSell(player.inventory, tile);
            else
                return new ContainerVendorBuy(player.inventory, tile);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        return null;
    }
}
