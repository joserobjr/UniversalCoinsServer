package br.com.gamemods.universalcoinsserver.net;

import br.com.gamemods.universalcoinsserver.tile.TileAdvSign;
import br.com.gamemods.universalcoinsserver.tile.TileVendorFrame;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TextureMessage implements IMessage, IMessageHandler<TextureMessage, IMessage>
{
    private int x, y, z;
    private String blockIcon;

    public TextureMessage()
    { }

    public TextureMessage(int x, int y, int z, String blockIcon)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockIcon = blockIcon;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, blockIcon);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.blockIcon = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public IMessage onMessage(TextureMessage message, MessageContext ctx)
    {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;

        TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
        if (tileEntity instanceof TileVendorFrame)
        {
            ((TileVendorFrame) tileEntity).icon = message.blockIcon;
        }
        if (tileEntity instanceof TileAdvSign)
        {
            ((TileAdvSign) tileEntity).icon = message.blockIcon;
        }
        return null;
    }
}
