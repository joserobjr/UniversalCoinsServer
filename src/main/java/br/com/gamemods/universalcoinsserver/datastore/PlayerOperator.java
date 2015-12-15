package br.com.gamemods.universalcoinsserver.datastore;

import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public class PlayerOperator implements Operator
{
    private final UUID playerId;

    public PlayerOperator(UUID playerId)
    {
        this.playerId = playerId;
    }

    public PlayerOperator(EntityPlayer player)
    {
        this.playerId = player.getPersistentID();
    }

    public UUID getPlayerId()
    {
        return playerId;
    }

    @Override
    public String toString()
    {
        return "PlayerOperator{" +
                "playerId=" + playerId +
                '}';
    }
}
