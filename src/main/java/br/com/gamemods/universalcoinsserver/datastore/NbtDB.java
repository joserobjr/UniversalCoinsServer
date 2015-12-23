package br.com.gamemods.universalcoinsserver.datastore;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;

public class NbtDB extends AbstractDB<AbstractDB.Account>
{
    private UUID undefinedOwner = UUID.nameUUIDFromBytes("Undefined".getBytes());

    private class Account extends AbstractDB.Account
    {
        public Account(String id, UUID owner, int balance)
        {
            super(id, owner, balance);
        }

        @Override
        public void incrementBalance(int increment, @Nonnull Transaction transaction) throws DataStoreException
        {
            super.incrementBalance(increment, transaction);

            WorldData worldData = sync(getWorld());
            worldData.setWorldData(id, balance);
        }
    }

    public static class WorldData extends WorldSavedData
    {
        private NBTTagCompound data = new NBTTagCompound();

        public WorldData(String tagName)
        {
            super(tagName);
        }

        @Override
        public void readFromNBT(NBTTagCompound compound)
        {
            data = compound.getCompoundTag("universalcoins");
        }

        @Override
        public void writeToNBT(NBTTagCompound compound)
        {
            compound.setTag("universalcoins", data);
        }

        private int getWorldInt(String tag)
        {
            return data.getInteger(tag);
        }

        private String getWorldString(String tag)
        {
            return data.getString(tag);
        }

        private boolean hasKey(String tag)
        {
            return data.hasKey(tag);
        }

        private void setWorldData(String tag, String data)
        {
            this.data.setString(tag, data);
            markDirty();
        }

        private void setWorldData(String tag, int data)
        {
            this.data.setInteger(tag, data);
            markDirty();
        }

        private void delWorldData(String tag)
        {
            data.removeTag(tag);
            markDirty();
        }
    }

    private WorldData sync(World world)
    {
        MapStorage storage = world.mapStorage;
        WorldData result = (WorldData) storage.loadData(WorldData.class, "universalcoins");
        if (result == null)
        {
            result = new WorldData("universalcoins");
            storage.setData("universalcoins", result);
        }

        return result;
    }

    @Override
    protected String generateAccountNumber()
    {
        return Integer.toString((int) (Math.floor(Math.random() * 99999999) + 11111111));
    }

    private World getWorld()
    {
        return MinecraftServer.getServer().worldServers[0];
    }

    @Nullable
    @Override
    protected AbstractDB.Account getAccount(@Nonnull String number) throws DataStoreException
    {
        WorldData data = sync(getWorld());
        if(data.hasKey(number))
            return new Account(number, null, data.getWorldInt(number));

        return null;
    }

    @Nullable
    @Override
    protected AbstractDB.Account getCustomAccount(@Nonnull String name) throws DataStoreException
    {
        return getAccount(name);
    }

    @Override
    protected void storeTrade(@Nonnull Transaction transaction, @Nullable AbstractDB.Account ownerAccount, int ownerIncrement, @Nullable AbstractDB.Account userAccount, int userIncrement) throws DataStoreException
    {
        if(ownerAccount != null)
            ownerAccount.incrementBalance(ownerIncrement, transaction);

        if(userAccount != null)
            userAccount.incrementBalance(userIncrement, transaction);
    }

    @Override
    public void saveNewMachine(@Nonnull Machine machine) throws DataStoreException
    {
        // Unsupported
    }

    @Override
    public void saveTransaction(@Nonnull Transaction transaction) throws DataStoreException
    {
        // Unsupported
    }

    @Nonnull
    @Override
    public PlayerData getPlayerData(@Nonnull UUID playerUID) throws DataStoreException
    {
        WorldData worldData = sync(getWorld());
        String playerId = playerUID.toString();
        String primary = worldData.getWorldString(playerId);
        String custom  = worldData.getWorldString("\uFFFD"+playerId);
        return new PlayerData(Integer.MIN_VALUE, playerUID,
                primary.isEmpty()? null : new AccountAddress(primary, primary, playerUID),
                custom.isEmpty()? null : Collections.singleton(new AccountAddress(custom, custom, playerUID))
        );
    }

    @Nonnull
    @Override
    public AccountAddress createPrimaryAccount(@Nonnull UUID playerUID, @Nonnull String name) throws DataStoreException, DuplicatedKeyException
    {
        WorldData worldData = sync(getWorld());
        String playerId = playerUID.toString();
        String accountNumber = worldData.getWorldString(playerId);
        if (!accountNumber.isEmpty())
            throw new DuplicatedKeyException("Player "+playerId+" already have a primary account: "+accountNumber);

        while (!worldData.hasKey(playerId))
        {
            accountNumber = generateAccountNumber();
            if (worldData.getWorldString(accountNumber).isEmpty())
            {
                worldData.setWorldData(playerId, accountNumber);
                worldData.setWorldData(accountNumber, 0);

                return new AccountAddress(accountNumber, name, playerUID);
            }
        }

        throw new DataStoreException("Failed to create account for player "+playerId);
    }

    @Nullable
    @Override
    public AccountAddress getCustomAccountByName(@Nonnull String customAccountName) throws DataStoreException
    {
        WorldData worldData = sync(getWorld());
        String accountNumber = worldData.getWorldString(customAccountName);
        if(accountNumber.isEmpty())
            return null;

        return new AccountAddress(accountNumber, customAccountName, undefinedOwner);
    }

    @Nonnull
    @Override
    public AccountAddress createCustomAccount(@Nonnull UUID playerUID, @Nonnull String customAccountName) throws DataStoreException, DuplicatedKeyException
    {
        WorldData worldData = sync(getWorld());
        String playerId = playerUID.toString();
        String currentPlayerCustom = worldData.getWorldString("\uFFFD"+playerId);
        String existingCustom = worldData.getWorldString(customAccountName);
        if(!currentPlayerCustom.isEmpty())
            throw new DuplicatedKeyException("Player "+playerId+" already have a custom account "+currentPlayerCustom);

        if(!existingCustom.isEmpty())
            throw new DuplicatedKeyException("Account name "+customAccountName+" already exists: "+existingCustom);

        while (worldData.getWorldString(customAccountName).isEmpty())
        {
            String number = generateAccountNumber();
            if(worldData.getWorldString(number).isEmpty())
            {
                worldData.setWorldData("\uFFFD"+playerId, customAccountName);
                worldData.setWorldData(customAccountName, number);
                worldData.setWorldData(number, 0);
                return new AccountAddress(number, customAccountName, playerUID);
            }
        }

        throw new DataStoreException("Failed to create custom account "+customAccountName+" for player "+playerId);
    }

    @Nonnull
    @Override
    public AccountAddress transferAccount(@Nonnull AccountAddress origin, @Nonnull String destiny, @Nullable Machine machine, @Nullable Operator operator) throws DataStoreException, AccountNotFoundException, DuplicatedKeyException
    {
        char code = '\uFFFD';
        WorldData worldData = sync(getWorld());

        UUID owner = origin.getOwner();
        if(owner.equals(undefinedOwner))
            throw new DataStoreException(new UnsupportedOperationException("The owner of the account is unknown: "+origin));
        String ownerId = owner.toString();

        String oldName = worldData.getWorldString(code + ownerId);
        String oldAccount = worldData.getWorldString(oldName);
        int oldBalance = worldData.hasKey(oldAccount)? worldData.getWorldInt(oldAccount) : -1;
        worldData.delWorldData(code + ownerId);
        worldData.delWorldData(oldName);
        worldData.delWorldData(oldAccount);
        if (worldData.getWorldString(code + ownerId).isEmpty())
        {
            String customAccountNumber;
            do
            {
                customAccountNumber = generateAccountNumber();
                if (worldData.getWorldString(customAccountNumber).isEmpty())
                {
                    worldData.setWorldData(code + ownerId, destiny);
                    worldData.setWorldData(destiny, customAccountNumber);
                    worldData.setWorldData(customAccountNumber, oldBalance);
                }

                if (!worldData.getWorldString(oldAccount).isEmpty())
                {
                    worldData.delWorldData(oldAccount);
                    worldData.delWorldData(oldName);
                }
            } while (worldData.getWorldString(customAccountNumber).isEmpty());

            return new AccountAddress(customAccountNumber, destiny, owner);
        }

        throw new DataStoreException("Failed to transfer custom account from "+origin+" to "+destiny);
    }

    @Nonnull
    @Override
    public AccountAddress transferPrimaryAccount(@Nonnull AccountAddress primaryAccount, @Nonnull String newName, @Nullable Machine machine, @Nullable Operator operator) throws DataStoreException, AccountNotFoundException
    {
        UUID owner = primaryAccount.getOwner();
        if(owner.equals(undefinedOwner))
            throw new DataStoreException(new UnsupportedOperationException("The owner of the account is unknown: "+primaryAccount));

        String playerUID = owner.toString();
        WorldData worldData = sync(getWorld());

        String oldAccount = worldData.getWorldString(playerUID);
        int oldBalance = worldData.hasKey(oldAccount)? worldData.getWorldInt(oldAccount) : -1;
        worldData.delWorldData(playerUID);
        String accountNumber = null;
        if (worldData.getWorldString(playerUID).isEmpty())
        {
            do{
                accountNumber = generateAccountNumber();
                if (worldData.getWorldString(accountNumber).isEmpty())
                {
                    worldData.setWorldData(playerUID, accountNumber);
                    worldData.setWorldData(accountNumber, oldBalance);
                }
            } while (worldData.getWorldString(accountNumber).isEmpty());
        }
        worldData.delWorldData(oldAccount);

        if(accountNumber != null)
            return new AccountAddress(accountNumber, newName, owner);

        throw new UnsupportedOperationException("Failed to create primary account "+primaryAccount);
    }

    @Override
    public Collection<PlayerData> getAllPlayerData() throws DataStoreException
    {
        WorldData worldData = sync(getWorld());
        //noinspection unchecked
        Set<String> keySet = worldData.data.func_150296_c();

        ArrayList<PlayerData> list = new ArrayList<>();

        for(String key: keySet)
        {
            if(key.length() == 36 && key.contains("-"))
            {
                UUID playerId = UUID.fromString(key);
                String primaryNumber = worldData.data.getString(key);
                String customName = "";
                for(String key2: keySet)
                {
                    if(!key2.equals(key) && key2.endsWith(key))
                    {
                        customName = worldData.data.getString(key2);
                        break;
                    }
                }
                String customNumber = !customName.isEmpty()? worldData.data.getString(customName) : "";
                list.add(new PlayerData(Integer.MIN_VALUE, playerId, new AccountAddress(primaryNumber, primaryNumber, playerId),
                        customName.isEmpty()? null : Collections.singleton(new AccountAddress(customNumber, customName, playerId))
                ));
            }
        }
        return list;
    }

    @Override
    public Map<AccountAddress, Integer> getAllAccountsBalance() throws DataStoreException
    {
        WorldData worldData = sync(getWorld());
        //noinspection unchecked
        Set<String> keySet = worldData.data.func_150296_c();

        Pattern pattern = Pattern.compile("^[0-9]+$");

        Map<String, Integer> balances = new HashMap<>();
        BiMap<UUID, String> playerPrimaries = HashBiMap.create();
        BiMap<UUID, String> playerCustoms = HashBiMap.create();
        BiMap<String, String> customAccounts = HashBiMap.create();


        for(String key: keySet)
        {
            int length = key.length();
            if(length >= 8 && length < 36 && pattern.matcher(key).matches() && (worldData.data.getTag(key) instanceof NBTTagInt))
                balances.put(key, worldData.data.getInteger(key));
            else if(length == 36 && key.contains("-"))
                playerPrimaries.put(UUID.fromString(key), worldData.data.getString(key));
            else if(length > 36 && key.contains("-") && (worldData.data.getTag(key) instanceof NBTTagString))
            {
                try
                {
                    UUID playerId = UUID.fromString(key.substring(key.length()-36));
                    playerCustoms.put(playerId, worldData.data.getString(key));
                }
                catch (Exception e){e.printStackTrace();}
            }
            else if(worldData.data.getTag(key) instanceof NBTTagString)
                customAccounts.put(key, worldData.data.getString(key));
        }

        Map<AccountAddress, Integer> map = new HashMap<>(balances.size());

        for(Map.Entry<String, Integer> entry: balances.entrySet())
        {
            String number = entry.getKey();
            int balance = entry.getValue();
            UUID primaryOwner = playerPrimaries.inverse().get(number);
            String customName = customAccounts.inverse().get(number);
            UUID customOwner = playerCustoms.inverse().get(customName);

            if(primaryOwner == null && customOwner == null)
            {
                if(balance <= 0)
                    continue;
                else
                {
                    UniversalCoinsServer.logger.warn("Skipping account "+number+" with balance "+balance+" because the owner is unknown!");
                    continue;
                }
            }


            AccountAddress address = new AccountAddress(
                    number,
                    customName != null? customName : number,
                    customOwner != null? customOwner : primaryOwner
            );

            map.put(address, balance);
        }

        return map;
    }

    @Override
    public AccountAddress renamePrimaryAccount(AccountAddress primaryAccount, String playerName) throws DataStoreException, AccountNotFoundException
    {
        AbstractDB.Account account = getAccount(primaryAccount.getNumber());
        if(account == null) throw new AccountNotFoundException(primaryAccount);

        // Unsupported

        return primaryAccount;
    }

    @Override
    public void importData(CardDataBase original) throws DataStoreException
    {
        throw new DataStoreException(new UnsupportedOperationException());
    }

    @Override
    public void updatePlayerName(@Nonnull UUID persistentID, @Nullable String commandSenderName) throws DataStoreException
    {
        // Unsupported
    }

    @Nullable
    @Override
    public UUID getPlayerIdByName(@Nonnull String name) throws DataStoreException
    {
        // Unsupported
        return null;
    }

    @Nullable
    @Override
    public Map<UUID, String> findPlayerByName(@Nonnull String searchedName) throws DataStoreException
    {
        // Unsupported
        return null;
    }

    @Override
    public boolean storePackage(@Nonnull ItemStack packageStack, ICommandSender sender, @Nonnull UUID targetId) throws DataStoreException
    {
        // Unsupported
        return false;
    }

    @Override
    public void deliveryPackages(@Nonnull EntityPlayer player) throws DataStoreException
    {
        // Unsupported
    }

    @Override
    public int getPendingDeliveries(@Nonnull UUID persistentID) throws DataStoreException
    {
        // Unsupported
        return 0;
    }
}
