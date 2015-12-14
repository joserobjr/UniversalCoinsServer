package br.com.gamemods.universalcoinsserver.net;

import br.com.gamemods.universalcoinsserver.tile.TileVendor;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ButtonMessage implements IMessage, IMessageHandler<ButtonMessage, IMessage>
{
    private int x, y, z, buttonId;
    private boolean shiftPressed;

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(buttonId);
        buf.writeBoolean(shiftPressed);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.buttonId = buf.readInt();
        this.shiftPressed = buf.readBoolean();
    }

    @Override
    public IMessage onMessage(ButtonMessage message, MessageContext ctx)
    {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;
        TileEntity te = world.getTileEntity(message.x, message.y, message.z);

        if(te instanceof IInventory && !((IInventory) te).isUseableByPlayer(player))
            return null;

        if (te instanceof TileVendor) {
            ((TileVendor) te).onButtonPressed(player, message.buttonId, message.shiftPressed);
        }

        return null;
    }
}
