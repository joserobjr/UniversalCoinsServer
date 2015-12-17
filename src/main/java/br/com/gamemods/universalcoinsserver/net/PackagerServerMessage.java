package br.com.gamemods.universalcoinsserver.net;

import br.com.gamemods.universalcoinsserver.tile.TilePackager;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PackagerServerMessage implements IMessage, IMessageHandler<PackagerServerMessage, IMessage>
{
    private int x, y, z;
    private String packageTarget;
    private boolean tabPressed;

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, packageTarget);
        buf.writeBoolean(tabPressed);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.packageTarget = ByteBufUtils.readUTF8String(buf);
        this.tabPressed = buf.readBoolean();
    }

    @Override
    public IMessage onMessage(PackagerServerMessage message, MessageContext ctx)
    {
        EntityPlayerMP playerEntity = ctx.getServerHandler().playerEntity;
        World world = playerEntity.worldObj;

        TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
        if (tileEntity instanceof TilePackager)
            ((TilePackager) tileEntity).playerLookup(message.packageTarget, message.tabPressed);

        return null;
    }
}
