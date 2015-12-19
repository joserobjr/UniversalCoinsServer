package br.com.gamemods.universalcoinsserver.datastore;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * A simple identification of an account, it's immutable store only basic information
 */
public final class AccountAddress
{
    @Nonnull
    private final Object number;
    @Nonnull
    private final String name;

    @Nonnull
    private final UUID owner;

    public AccountAddress(@Nonnull Object number, @Nonnull String name, @Nonnull UUID owner)
    {
        this.number = number;
        this.name = name;
        this.owner = owner;
    }

    @Override
    public String toString()
    {
        return "AccountAddress{" +
                "number=" + number +
                ", name='" + name + '\'' +
                ", owner=" + owner +
                '}';
    }

    /**
     * @return The account "number". The type is specified by the {@link CardDataBase}.
     */
    @Nonnull
    public Object getNumber()
    {
        return number;
    }

    /**
     * The name of the account, if it's the player's primary account it will have the player name used when the
     * account was created.
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * @return The player UUID
     */
    @Nonnull
    public UUID getOwner()
    {
        return owner;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountAddress that = (AccountAddress) o;

        if (!number.equals(that.number)) return false;
        if (!name.equals(that.name)) return false;
        return owner.equals(that.owner);

    }

    @Override
    public int hashCode()
    {
        int result = number.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + owner.hashCode();
        return result;
    }
}
