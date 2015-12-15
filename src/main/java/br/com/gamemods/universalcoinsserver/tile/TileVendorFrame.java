package br.com.gamemods.universalcoinsserver.tile;

public class TileVendorFrame extends TileVendor
{
    @Override
    protected void updateSign(String[] lines)
    {
        updateSign(lines, worldObj.getTileEntity(xCoord, yCoord - 1, zCoord));
        updateSign(lines, worldObj.getTileEntity(xCoord, yCoord + 1, zCoord));
    }
}
