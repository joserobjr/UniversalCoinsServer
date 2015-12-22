package br.com.gamemods.universalcoinsserver.datastore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public final class PlayerData
{
    private final UUID playerId;
    int version = Integer.MIN_VALUE;
    private AccountAddress primaryAccount;
    private Collection<AccountAddress> alternativeAccounts;

    public PlayerData(int version, @Nonnull UUID playerId, @Nullable AccountAddress primaryAccount, @Nullable Collection<AccountAddress> alternativeAccounts)
    {
        this.version = version;
        this.playerId = playerId;
        this.primaryAccount = primaryAccount;
        if(alternativeAccounts != null)
        {
            if(!alternativeAccounts.isEmpty())
                alternativeAccounts = Collections.unmodifiableCollection(alternativeAccounts);
            else
                alternativeAccounts = Collections.emptyList();
        }
        else
            alternativeAccounts = Collections.emptyList();

        this.alternativeAccounts = alternativeAccounts;
    }

    public UUID getPlayerId()
    {
        return playerId;
    }

    public int getVersion()
    {
        return version;
    }

    public AccountAddress getPrimaryAccount()
    {
        return primaryAccount;
    }

    public boolean hasPrimaryAccount()
    {
        return primaryAccount != null;
    }

    public boolean hasCustomAccount()
    {
        return !alternativeAccounts.isEmpty();
    }

    public Collection<AccountAddress> getAlternativeAccounts()
    {
        return alternativeAccounts;
    }
}
