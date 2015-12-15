package br.com.gamemods.universalcoinsserver.datastore;

import net.minecraft.tileentity.TileEntity;

public class MachineOperator extends BlockOperator
{
    private Machine machine;

    public MachineOperator(Machine tileEntity)
    {
        super((TileEntity) tileEntity);
        this.machine = tileEntity;
    }

    public Machine getMachine()
    {
        return machine;
    }

    @Override
    public String toString()
    {
        return "MachineOperator{machine="+String.valueOf(machine)+", super=" + super.toString()+"}";
    }
}
