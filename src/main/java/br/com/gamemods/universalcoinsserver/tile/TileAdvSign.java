package br.com.gamemods.universalcoinsserver.tile;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.blocks.PlayerOwned;
import br.com.gamemods.universalcoinsserver.net.SignMessage;
import br.com.gamemods.universalcoinsserver.net.TextureMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.IChatComponent;

import java.util.UUID;

public class TileAdvSign extends TileEntitySign implements PlayerOwned
{
    public UUID owner;
    public String ownerName;
    public String icon = "";

    @Override
    public UUID getOwnerId()
    {
        return owner;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);

        for (int i = 0; i < 4; ++i)
        {
            this.signText[i] = tagCompound.getString("Text" + (i + 1));
        }
        try
        {
            String blockOwnerId = tagCompound.getString("blockOwnerId");
            if(blockOwnerId.isEmpty())
                owner = null;
            else
                owner = UUID.fromString(blockOwnerId);
        }
        catch (Exception e)
        {
            owner = null;
        }

        ownerName = tagCompound.getString("blockOwner");
        icon = tagCompound.getString("blockIcon");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        tagCompound.setString("Text1", this.signText[0]);
        tagCompound.setString("Text2", this.signText[1]);
        tagCompound.setString("Text3", this.signText[2]);
        tagCompound.setString("Text4", this.signText[3]);
        tagCompound.setString("blockOwner", ownerName);
        tagCompound.setString("blockIcon", icon);
        if(owner != null) tagCompound.setString("blockOwnerId", owner.toString());
    }

    @Override
    public Packet getDescriptionPacket()
    {
        String[] lines = new String[4];
        System.arraycopy(this.signText, 0, lines, 0, 4);
        return UniversalCoinsServer.network.getPacketFrom(
                new SignMessage(this.xCoord, this.yCoord, this.zCoord, lines, ownerName, icon)
        );
    }

    public void sendTextureUpdateMessage(ItemStack stack)
    {
        String blockIcon = stack.getIconIndex().getIconName();
        UniversalCoinsServer.network.sendToServer(new TextureMessage(xCoord, yCoord, zCoord, blockIcon));
    }

    public void scheduleUpdate()
    {
        super.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void setLines(IChatComponent[] lines)
    {
        for(int i = 0; i < 4; i++)
        {
            if(lines.length < i || lines[i] == null)
                signText[i] = "";
            else
                signText[i] = IChatComponent.Serializer.func_150696_a(lines[i]);
        }
    }
}
