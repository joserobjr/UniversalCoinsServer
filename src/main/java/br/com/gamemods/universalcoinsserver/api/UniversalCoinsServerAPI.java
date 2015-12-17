package br.com.gamemods.universalcoinsserver.api;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.datastore.AccountAddress;
import br.com.gamemods.universalcoinsserver.item.ItemCard;
import br.com.gamemods.universalcoinsserver.item.ItemCoin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class UniversalCoinsServerAPI
{
    public static final Random random = new Random();

    @Nonnull
    public static ScanResult scanCoins(@Nonnull IInventory inventory)
            throws NullPointerException
    {
        return scanCoins(inventory, 0, inventory.getSizeInventory());
    }

    @Nonnull
    public static ScanResult scanCoins(@Nonnull IInventory inventory, int startIndex, int endIndex)
            throws NullPointerException, IndexOutOfBoundsException
    {
        if(startIndex < 0) throw new IndexOutOfBoundsException("startIndex < 0: "+startIndex);
        else if(startIndex > endIndex) throw new IndexOutOfBoundsException("startIndex > endIndex: start:"+startIndex+" end:"+endIndex);

        TreeMap<Integer, SortedMap<Integer, SortedSet<Integer>>> coinMap = new TreeMap<>();
        int total = 0;
        for(int i = startIndex; i < endIndex; i++)
        {
            ItemStack stack = inventory.getStackInSlot(i);
            if(stack == null || stack.stackSize <= 0)
                continue;

            Item item = stack.getItem();
            if(item instanceof ItemCoin)
            {
                int value = ((ItemCoin) item).getValue();
                SortedMap<Integer, SortedSet<Integer>> map = coinMap.get(value);
                if(map == null) coinMap.put(value, map = new TreeMap<>());
                SortedSet<Integer> set = map.get(stack.stackSize);
                if(set == null) set = new TreeSet<>();
                set.add(i);
                map.put(stack.stackSize, set);
                total += value * stack.stackSize;
            }
        }

        return new ScanResult(inventory, coinMap, total, startIndex, endIndex);
    }

    public static int takeCoins(@Nonnull IInventory inventory, int coins)
    {
        return takeCoins(inventory, coins, 0, inventory.getSizeInventory());
    }

    public static int takeCoins(@Nonnull IInventory inventory, int coins, int startIndex, int endIndex)
    {
        return takeCoins(scanCoins(inventory, startIndex, endIndex), coins);
    }

    /**
     * Takes coins from an inventory following a fresh scan distribution result
     * @param scanResult The latest scan from the inventory
     * @param coins The amount of coins that will be taken
     * @return The difference from the requested amount, a negative value indicates that more coins were taken than
     *         the requested value, positive that less coins were taken.
     * @throws IllegalArgumentException If {@code coins < 0}
     * @throws ConcurrentModificationException If the scan result doesn't match the inventory content's, part of the coins
     *         will be taken before this exception is fired
     * @throws NullPointerException If one of non null parameters are null
     */
    public static int takeCoins(@Nonnull ScanResult scanResult, int coins)
            throws IllegalArgumentException, ConcurrentModificationException, NullPointerException
    {
        IInventory inventory = scanResult.getScannedInventory();
        if(coins == 0) return 0;
        else if(coins < 0) throw new IllegalArgumentException("coins < 0: "+coins);

        for(Map.Entry<Integer, SortedMap<Integer, SortedSet<Integer>>> coinEntry: scanResult.getDistribution().entrySet())
        {
            int value = coinEntry.getKey();
            for(Map.Entry<Integer, SortedSet<Integer>> amountEntry: coinEntry.getValue().entrySet())
            {
                int amount = amountEntry.getKey();
                for(int slot: amountEntry.getValue())
                {
                    ItemStack stack = inventory.getStackInSlot(slot);
                    Item item;
                    if(stack == null || !((item=stack.getItem()) instanceof ItemCoin) || ((ItemCoin)item).getValue() != value
                        || stack.stackSize != amount)
                        throw new ConcurrentModificationException();

                    int amountToTake = Math.min(Math.max(coins / value, 1), amount);
                    if(amountToTake > 0)
                    {
                        stack.stackSize -= amountToTake;
                        coins -= amountToTake * value;
                        if(stack.stackSize == 0)
                            stack = null;
                        inventory.setInventorySlotContents(slot, stack);

                        if(coins <= 0)
                            return coins;
                    }
                }
            }
        }

        return coins;
    }

    public static int addCoins(@Nonnull IInventory inventory, int coins)
            throws IllegalArgumentException, NullPointerException
    {
        return addCoins(inventory, coins, 0, inventory.getSizeInventory());
    }

    public static int addCoins(@Nonnull IInventory inventory, int coins, int startIndex, int endIndex)
            throws IllegalArgumentException, NullPointerException, IndexOutOfBoundsException
    {
        return addCoins(scanCoins(inventory, startIndex, endIndex), coins);
    }

    public static int addCoins(@Nonnull ScanResult scanResult, int coins)
            throws IllegalArgumentException, NullPointerException
    {
        IInventory inventory = scanResult.getScannedInventory();
        if(coins == 0) return 0;
        else if(coins < 0) throw new IllegalArgumentException("coins < 0: "+coins);

        TreeMap<Integer, SortedMap<Integer, SortedSet<Integer>>> reverseCoins = new  TreeMap<>(Collections.reverseOrder());
        reverseCoins.putAll(scanResult.getDistribution());

        int inventoryStackLimit = inventory.getInventoryStackLimit();

        for(Map.Entry<Integer, SortedMap<Integer, SortedSet<Integer>>> coinEntry: reverseCoins.entrySet())
        {
            int value = coinEntry.getKey();
            TreeMap<Integer, SortedSet<Integer>> reverseAmounts = new TreeMap<>(Collections.reverseOrder());
            reverseAmounts.putAll(coinEntry.getValue());

            for(Map.Entry<Integer, SortedSet<Integer>> amountEntry: reverseAmounts.entrySet())
            {
                SortedSet<Integer> reverseSlots = new TreeSet<>(Collections.reverseOrder());
                reverseSlots.addAll(amountEntry.getValue());

                for(int slot: reverseSlots)
                {
                    ItemStack stack = inventory.getStackInSlot(slot);
                    Item item;
                    if(stack == null || !((item=stack.getItem()) instanceof ItemCoin) || !inventory.isItemValidForSlot(slot, stack))
                        continue;

                    value = ((ItemCoin) item).getValue();

                    int amountToGive = Math.min(
                            Math.min( coins / value, stack.getMaxStackSize() - stack.stackSize ),
                                            inventoryStackLimit - stack.stackSize
                    );

                    if(amountToGive > 0)
                    {
                        stack.stackSize += amountToGive;
                        coins -= amountToGive * value;
                        inventory.setInventorySlotContents(slot, stack);

                        if(coins <= 0)
                            return coins;
                    }
                }
            }
        }

        if(coins > 0)
            return addCoinsAnywhere(inventory, coins, scanResult.getStartIndex(), scanResult.getEndIndex());

        return coins;
    }

    public static int addCoinsAnywhere(@Nonnull IInventory inventory, int coins)
            throws NullPointerException, IllegalArgumentException
    {
        return addCoinsAnywhere(inventory, coins, 0, inventory.getSizeInventory());
    }

    public static int addCoinsToSlot(@Nonnull IInventory inventory, int coins, int slot)
    {
        return addCoinsAnywhere(inventory, coins, slot, slot+1);
    }

    public static int addCoinsAnywhere(@Nonnull IInventory inventory, int coins, int startIndex, int endIndex)
            throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException
    {
        if(coins == 0) return 0;
        else if(coins < 0) throw new IllegalArgumentException("coins < 0: "+coins);
        else if(startIndex < 0) throw new IndexOutOfBoundsException("startIndex < 0: "+startIndex);
        else if(startIndex > endIndex) throw new IndexOutOfBoundsException("startIndex > endIndex: start:"+startIndex+" end:"+endIndex);

        int inventoryStackLimit = inventory.getInventoryStackLimit();

        for(int slot = startIndex; slot < endIndex; slot++)
        {
            ItemStack stack = inventory.getStackInSlot(slot);
            if(stack == null)
            {
                stack = createBestStack(coins);
                if(inventory.isItemValidForSlot(slot, stack))
                {
                    inventory.setInventorySlotContents(slot, stack);
                    coins -= stackValue(stack);
                }
            }
            else
            {
                Item item = stack.getItem();
                int maxStackSize = stack.getMaxStackSize();
                if(!(item instanceof ItemCoin) || stack.stackSize >= maxStackSize || !inventory.isItemValidForSlot(slot, stack))
                    continue;

                int value = ((ItemCoin) item).getValue();
                int amountToAdd = Math.min(Math.min(coins / value, maxStackSize - stack.stackSize), inventoryStackLimit - stack.stackSize);
                if(amountToAdd > 0)
                {
                    stack.stackSize += amountToAdd;
                    inventory.setInventorySlotContents(slot, stack);
                    coins -= amountToAdd * value;
                }
            }

            if(coins <= 0)
                return coins;
        }

        return coins;
    }

    public static int rebalance(@Nonnull IInventory inventory)
            throws NullPointerException
    {
        return rebalance(inventory, 0, inventory.getSizeInventory());
    }

    public static int rebalance(@Nonnull IInventory inventory, int startIndex, int endIndex)
            throws NullPointerException, IndexOutOfBoundsException
    {
        if(startIndex < 0) throw new IndexOutOfBoundsException("startIndex < 0: "+startIndex);
        else if(startIndex > endIndex) throw new IndexOutOfBoundsException("startIndex > endIndex: start:"+startIndex+" end:"+endIndex);

        long balance = 0;
        for(int slot = startIndex; slot < endIndex; slot++)
        {
            ItemStack stack = inventory.getStackInSlot(slot);
            Item item = stack.getItem();
            if(item instanceof ItemCoin && inventory.isItemValidForSlot(slot, stack) && stack.stackSize > 0)
            {
                int stackValue = ((ItemCoin) item).getValue() * stack.stackSize;
                long sum;
                if((sum=balance + stackValue) <= Integer.MAX_VALUE)
                {
                    inventory.setInventorySlotContents(slot, null);
                    balance = sum;
                }
            }
        }

        return addCoinsAnywhere(inventory, (int) Math.min(balance, Integer.MAX_VALUE), startIndex, endIndex);
    }

    public static void takeCoinsReturningChange(@Nonnull ScanResult scanResult, int coins, @Nonnull EntityPlayer player)
    {
        takeCoinsReturningChange(scanResult, coins, player, 2);
    }

    public static int takeCoinsReturningChange(@Nonnull ScanResult scanResult, int coins, @Nonnull EntityPlayer player, int returnStrategy)
    {
        int change = takeCoinsReturningChange(scanResult, coins);
        if(change >= 0) return change;
        if((returnStrategy & 1) > 0)
        {
            InventoryEnderChest enderChest = player.getInventoryEnderChest();
            change = addCoinsAnywhere(enderChest, -change);
            if(change <= 0) return -change;

            change += rebalance(enderChest);
            change = addCoinsAnywhere(enderChest, change);
            if(change <= 0) return -change;
            change = -change;
        }

        if((returnStrategy & 2) > 0)
        {
            dropAtEntity(player, change);
            return 0;
        }

        return change;
    }

    public static int takeCoinsReturningChange(@Nonnull ScanResult scanResult, int coins)
    {
        IInventory inventory = scanResult.getScannedInventory();

        int change = takeCoins(scanResult, coins);
        if(change >= 0) return change;

        change = addCoins(scanResult, -change);
        if(change <= 0) return -change;

        int startIndex = scanResult.getStartIndex();
        int endIndex = scanResult.getEndIndex();

        change += rebalance(inventory, startIndex, endIndex);
        return addCoinsAnywhere(inventory, change, startIndex, endIndex);
    }

    public static void dropAtEntity(Entity entity, int coins)
    {
        dropAtEntity(entity, coins, entity.getEyeHeight());
    }

    public static void dropAtEntity(Entity entity, int coins, float height)
    {
        for(ItemStack drop: createStacks(coins))
            entity.entityDropItem(drop, height);
    }

    public static int stackValue(@Nullable ItemStack stack)
    {
        if(stack == null) return 0;

        Item item = stack.getItem();
        if(!(item instanceof ItemCoin)) return 0;

        return ((ItemCoin) item).getValue() * stack.stackSize;
    }

    public static int stackValue(@Nullable Collection<ItemStack> stacks)
    {
        if(stacks == null) return 0;
        int value = 0;
        for(ItemStack stack: stacks)
            value += stackValue(stack);
        return value;
    }

    public static List<ItemStack> createStacks(int coins)
    {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        while (coins > 0)
        {
            ItemStack bestStack = createBestStack(coins);
            stacks.add(bestStack);
            coins -= stackValue(bestStack);
        }
        return stacks;
    }

    @Nonnull
    public static ItemStack createBestStack(int coins)
    {
        //TODO Use logarithm?
        ItemCoin lastItem = UniversalCoinsServer.proxy.itemCoin;
        /*
        for(ItemCoin item: UniversalCoinsServer.proxy.coins)
        {
            lastItem = item;
            if(coins < item.getValue()*9)
                break;
        }
        */
        ItemCoin[] coinDegree = UniversalCoinsServer.proxy.coins;
        int length = coinDegree.length;
        for(int i = length-1; i >= 0; i--)
        {
            ItemCoin coin = coinDegree[i];
            if(coin.getValue() <= coins)
            {
                lastItem = coin;
                break;
            }
        }

        ItemStack stack = new ItemStack(lastItem, coins / lastItem.getValue());
        if(stack.stackSize > stack.getMaxStackSize())
            stack.stackSize = stack.getMaxStackSize();
        return stack;
    }

    @Nullable
    public static AccountAddress getAddress(@Nullable ItemStack cardStack)
    {
        if(cardStack == null || cardStack.stackSize <= 0 || cardStack.stackTagCompound == null)
            return null;

        Item item = cardStack.getItem();
        if(!(item instanceof ItemCard))
            return null;

        return ((ItemCard) item).getAccountAddress(cardStack);
    }

    public static boolean matches(ItemStack stack, ItemStack otherStack)
    {
        return !(stack == null || otherStack == null)
                && stack.stackSize > 0 && otherStack.stackSize > 0
                && stack.getItem() == otherStack.getItem() && stack.getItemDamage() == otherStack.getItemDamage()
                && ItemStack.areItemStackTagsEqual(stack, otherStack);
    }

    public static void drop(World world, double x, double y, double z, List<ItemStack> drops)
    {
        drop(world, x, y, z, drops, random);
    }

    public static void drop(World world, double x, double y, double z, List<ItemStack> drops, Random random)
    {
        for(ItemStack drop: drops)
        {
            if(drop == null) continue;

            float xRand = random.nextFloat() * 0.8F + 0.1F;
            float yRand = random.nextFloat() * 0.8F + 0.1F;
            float zRand = random.nextFloat() * 0.8F + 0.1F;

            EntityItem item;
            for (; drop.stackSize > 0; world.spawnEntityInWorld(item))
            {
                int amount = random.nextInt(21) + 10;

                if (amount > drop.stackSize)
                    amount = drop.stackSize;
                drop.stackSize -= amount;

                item = new EntityItem(world, x + xRand, y + yRand, z + zRand, new ItemStack(drop.getItem(), amount, drop.getItemDamage()));
                item.motionX = (float)random.nextGaussian() * 0.05F;
                item.motionY = (float)random.nextGaussian() * 0.05F + 0.2F;
                item.motionZ = (float)random.nextGaussian() * 0.05F;

                if (drop.hasTagCompound())
                    item.getEntityItem().setTagCompound((NBTTagCompound)drop.getTagCompound().copy());
            }
        }
    }

    public static ItemStack createCard(AccountAddress account, boolean open)
    {
        ItemStack stack = new ItemStack(UniversalCoinsServer.proxy.itemCard);
        stack.stackTagCompound = new NBTTagCompound();
        stack.stackTagCompound.setString("Name", account.getName());
        stack.stackTagCompound.setString("Owner", account.getOwner().toString());
        stack.stackTagCompound.setString("Account", account.getNumber().toString());
        if(open)
            stack.stackTagCompound.setBoolean("Open", true);
        return stack;
    }
}
