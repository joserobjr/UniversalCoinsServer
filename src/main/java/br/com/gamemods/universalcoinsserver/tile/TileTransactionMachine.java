package br.com.gamemods.universalcoinsserver.tile;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.datastore.DataBaseException;
import br.com.gamemods.universalcoinsserver.datastore.Machine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public abstract class TileTransactionMachine extends TileEntity implements Machine, IInventory
{
    private UUID machineId;
    public EntityPlayer opener;

    public boolean isInUse(EntityPlayer player)
    {
        if(opener == null)
            return false;

        if(!opener.isEntityAlive() || !isUseableByPlayer(opener))
        {
            opener = null;
            return false;
        }

        return !opener.isEntityEqual(player);
    }

    public void setOpener(EntityPlayer opener)
    {
        this.opener = opener;
    }

    @Override
    public UUID getMachineId()
    {
        if(machineId == null)
        {
            machineId = UUID.randomUUID();
            try
            {
                UniversalCoinsServer.cardDb.saveNewMachine(this);
            }
            catch (DataBaseException e)
            {
                UniversalCoinsServer.logger.error("Failed to save machine ID "+machineId, e);
            }
        }
        return machineId;
    }

    @Override
    public TileEntity getMachineEntity()
    {
        return this;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        String str = compound.getString("MachineId");
        if(str.isEmpty()) getMachineId();
        try
        {
            machineId = UUID.fromString(str);
        }
        catch (Exception e)
        {
            getMachineId();
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString("MachineId", getMachineId().toString());
    }

    public void scheduleUpdate()
    {
        if(worldObj != null)
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void updateNeighbors()
    {
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, blockType);
    }

    public void onButtonPressed(EntityPlayerMP player, int buttonId, boolean shiftPressed)
    {}
}
