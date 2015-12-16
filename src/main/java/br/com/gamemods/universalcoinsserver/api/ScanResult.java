package br.com.gamemods.universalcoinsserver.api;

import net.minecraft.inventory.IInventory;

import javax.annotation.Nonnull;
import java.util.SortedMap;
import java.util.SortedSet;

public class ScanResult
{
    @Nonnull
    private final IInventory scannedInventory;
    @Nonnull
    private final SortedMap<Integer, SortedMap<Integer, SortedSet<Integer>>> distribution;
    private final int coins;
    private final int startIndex, endIndex;

    public ScanResult(@Nonnull IInventory scannedInventory, @Nonnull SortedMap<Integer, SortedMap<Integer, SortedSet<Integer>>> distribution, int coins)
    {
        if(coins < 0) throw new IllegalArgumentException("coins < 0: "+coins);
        this.scannedInventory = scannedInventory;
        this.distribution = distribution;
        this.coins = coins;
        this.startIndex = 0;
        this.endIndex = scannedInventory.getSizeInventory();
    }

    public ScanResult(@Nonnull IInventory scannedInventory, @Nonnull SortedMap<Integer, SortedMap<Integer, SortedSet<Integer>>> distribution, int coins, int startIndex, int endIndex)
    {
        this.scannedInventory = scannedInventory;
        this.distribution = distribution;
        this.coins = coins;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getCoins()
    {
        return coins;
    }

    @Nonnull
    public SortedMap<Integer, SortedMap<Integer, SortedSet<Integer>>> getDistribution()
    {
        return distribution;
    }

    @Nonnull
    public IInventory getScannedInventory()
    {
        return scannedInventory;
    }

    public int getStartIndex()
    {
        return startIndex;
    }

    public int getEndIndex()
    {
        return endIndex;
    }

    @Override
    public String toString()
    {
        return "ScanResult{" +
                "coins=" + coins +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                ", distribution=" + distribution +
                ", scannedInventory=" + scannedInventory +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScanResult that = (ScanResult) o;

        if (coins != that.coins) return false;
        if (startIndex != that.startIndex) return false;
        if (endIndex != that.endIndex) return false;
        if (!scannedInventory.equals(that.scannedInventory)) return false;
        return distribution.equals(that.distribution);

    }

    @Override
    public int hashCode()
    {
        int result = scannedInventory.hashCode();
        result = 31 * result + distribution.hashCode();
        result = 31 * result + coins;
        result = 31 * result + startIndex;
        result = 31 * result + endIndex;
        return result;
    }
}
