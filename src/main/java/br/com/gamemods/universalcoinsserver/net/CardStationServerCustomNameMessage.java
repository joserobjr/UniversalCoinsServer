package br.com.gamemods.universalcoinsserver.net;

import br.com.gamemods.universalcoinsserver.tile.TileCardStation;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CardStationServerCustomNameMessage
        implements IMessage, IMessageHandler<CardStationServerCustomNameMessage, IMessage>
{
    private int x, y, z;
    private String groupName;

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, groupName);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.groupName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public IMessage onMessage(CardStationServerCustomNameMessage message, MessageContext ctx)
    {
        EntityPlayerMP playerEntity = ctx.getServerHandler().playerEntity;
        World world = playerEntity.worldObj;

        TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
        if (tileEntity instanceof TileCardStation)
        {
            TileCardStation cardStation = (TileCardStation) tileEntity;
            if(playerEntity.equals(cardStation.opener))
                cardStation.customAccountName = message.groupName;
        }
        return null;
    }
}
