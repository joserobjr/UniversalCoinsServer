package br.com.gamemods.universalcoinsserver.net;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.tile.TileVendor;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.UUID;

public class VendorServerMessage implements IMessage, IMessageHandler<VendorServerMessage, IMessage>
{
    private int x, y, z, itemPrice;
    private UUID blockOwner;
    private boolean infinite;

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(itemPrice);
        ByteBufUtils.writeUTF8String(buf, blockOwner == null? "" : blockOwner.toString());
        buf.writeBoolean(infinite);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.itemPrice = buf.readInt();
        String str = ByteBufUtils.readUTF8String(buf);
        try
        {
            this.blockOwner = UUID.fromString(str);
        }
        catch (Exception e)
        {
            this.blockOwner = null;
        }

        this.infinite = buf.readBoolean();
    }

    @Override
    public IMessage onMessage(VendorServerMessage message, MessageContext ctx)
    {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;

        TileEntity te = world.getTileEntity(message.x, message.y, message.z);
        if(te instanceof IInventory && !((IInventory) te).isUseableByPlayer(player))
            return null;

        if(te instanceof TileVendor)
        {
            TileVendor tile = (TileVendor) te;
            ItemStack heldItem = player.getHeldItem();
            if(heldItem != null && heldItem.getItem() == UniversalCoinsServer.proxy.itemVendorWrench)
            {
                tile.owner = message.blockOwner;
                tile.infinite = message.infinite;
            }

            if(player.getPersistentID().equals(tile.owner))
                tile.price = message.itemPrice;

            tile.scheduleUpdate();
            tile.updateBlocks();
        }

        return null;
    }
}
