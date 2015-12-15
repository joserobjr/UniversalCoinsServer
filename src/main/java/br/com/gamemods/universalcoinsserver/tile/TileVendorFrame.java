package br.com.gamemods.universalcoinsserver.tile;

import net.minecraft.util.IChatComponent;

public class TileVendorFrame extends TileVendor
{
    @Override
    protected void updateSign(IChatComponent[] lines)
    {
        updateSign(lines, worldObj.getTileEntity(xCoord, yCoord - 1, zCoord));
        updateSign(lines, worldObj.getTileEntity(xCoord, yCoord + 1, zCoord));
    }
}
