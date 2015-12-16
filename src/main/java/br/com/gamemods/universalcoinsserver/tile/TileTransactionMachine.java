package br.com.gamemods.universalcoinsserver.tile;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.datastore.DataBaseException;
import br.com.gamemods.universalcoinsserver.datastore.Machine;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public abstract class TileTransactionMachine extends TileEntity implements Machine
{
    private UUID machineId;

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
}
