package br.com.gamemods.universalcoinsserver.container;

import br.com.gamemods.universalcoinsserver.tile.TileVendor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import java.util.UUID;

public class ContainerVendorWrench extends Container
{
    private TileVendor tile;
    private Boolean lastInfinite;
    private UUID lastOwner;

    public ContainerVendorWrench(TileVendor tile)
    {
        if(tile == null) throw new NullPointerException("tile");
        this.tile = tile;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if(lastInfinite == null || lastOwner != tile.owner || lastInfinite != tile.infinite)
        {
            tile.scheduleUpdate();
            lastOwner = tile.owner;
            lastInfinite = tile.infinite;
        }
    }
}
