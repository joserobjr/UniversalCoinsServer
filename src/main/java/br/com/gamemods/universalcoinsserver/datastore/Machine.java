package br.com.gamemods.universalcoinsserver.datastore;

import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public interface Machine
{
    UUID getMachineId();

    TileEntity getMachineEntity();
}
