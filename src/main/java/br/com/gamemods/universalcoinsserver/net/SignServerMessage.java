package br.com.gamemods.universalcoinsserver.net;

import br.com.gamemods.universalcoinsserver.tile.TileAdvSign;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class SignServerMessage implements IMessage, IMessageHandler<SignServerMessage, IMessage>
{
    private int x, y, z;
    private String signText0, signText1, signText2, signText3;

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, signText0);
        ByteBufUtils.writeUTF8String(buf, signText1);
        ByteBufUtils.writeUTF8String(buf, signText2);
        ByteBufUtils.writeUTF8String(buf, signText3);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.signText0 = ByteBufUtils.readUTF8String(buf);
        this.signText1 = ByteBufUtils.readUTF8String(buf);
        this.signText2 = ByteBufUtils.readUTF8String(buf);
        this.signText3 = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public IMessage onMessage(SignServerMessage message, MessageContext ctx)
    {
        World world = ctx.getServerHandler().playerEntity.worldObj;

        TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
        if (tileEntity instanceof TileAdvSign)
        {
            TileAdvSign tile = (TileAdvSign) tileEntity;
            tile.signText[0] = message.signText0;
            tile.signText[1] = message.signText1;
            tile.signText[2] = message.signText2;
            tile.signText[3] = message.signText3;
            tile.scheduleUpdate();
            tile.markDirty();
        }
        return null;
    }
}
