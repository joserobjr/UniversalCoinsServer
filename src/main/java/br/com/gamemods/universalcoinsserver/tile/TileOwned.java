package br.com.gamemods.universalcoinsserver.tile;

import br.com.gamemods.universalcoinsserver.blocks.PlayerOwned;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public abstract class TileOwned extends TileTransactionMachine implements PlayerOwned
{
    public String ownerName;
    public UUID owner;

    @Override
    public UUID getOwnerId()
    {
        return owner;
    }

    public void writeToStackNBT(NBTTagCompound compound)
    {
    }

    public void readFromStackNBT(NBTTagCompound compound)
    {
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if(ownerName != null && !ownerName.isEmpty())
            compound.setString(getOwnerNameNBTKey(), ownerName);
        if(owner != null)
            compound.setString(getOwnerIdNBTKey(), owner.toString());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        ownerName = compound.getString(getOwnerNameNBTKey());
        String str = compound.getString(getOwnerIdNBTKey());
        if(str.isEmpty()) owner = null;
        else
            try
            {
                owner = UUID.fromString(str);
            }
            catch (Exception e)
            {
                owner = null;
            }
    }

    public String getOwnerIdNBTKey()
    {
        return "BlockOwner";
    }

    public String getOwnerNameNBTKey()
    {
        return "OwnerName";
    }
}
