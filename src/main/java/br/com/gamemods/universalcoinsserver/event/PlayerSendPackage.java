package br.com.gamemods.universalcoinsserver.event;

import br.com.gamemods.universalcoinsserver.tile.TilePackager;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.UUID;

@Cancelable
public class PlayerSendPackage extends Event
{
    private final EntityPlayer sender;
    private final UUID targetId;
    private final EntityPlayer targetPlayer;
    private final ItemStack stack;
    public final int blockX;
    public final int blockY;
    public final int blockZ;
    public final World blockWorld;

    public PlayerSendPackage(EntityPlayer sender, EntityPlayer targetPlayer, ItemStack stack, TilePackager packager)
    {
        this.sender = sender;
        this.targetId = packager.targetId;
        this.targetPlayer = targetPlayer;
        this.stack = stack;
        this.blockX = packager.xCoord;
        this.blockY = packager.yCoord;
        this.blockZ = packager.yCoord;
        this.blockWorld = packager.getWorldObj();
    }
}
