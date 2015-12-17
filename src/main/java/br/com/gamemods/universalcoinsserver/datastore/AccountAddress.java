package br.com.gamemods.universalcoinsserver.datastore;

import javax.annotation.Nonnull;
import java.util.UUID;

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

    @Nonnull
    public Object getNumber()
    {
        return number;
    }

    @Nonnull
    public String getName()
    {
        return name;
    }

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
