package br.com.gamemods.universalcoinsserver.datastore;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.UUID;

public interface CardDataBase
{
    UUID getAccountOwner(String account) throws DataBaseException;

    int getAccountBalance(Object account) throws DataBaseException;

    int canDeposit(Object account, Collection<ItemStack> coins) throws DataBaseException;

    int canDeposit(Object account, ItemStack coins) throws DataBaseException;

    int canDeposit(Object account, int coins) throws DataBaseException;

    @Deprecated
    boolean depositToAccount(String account, int depositAmount, Operator operator, TransactionType transaction, String product)
            throws DataBaseException, IllegalArgumentException;

    /**
     *
     * @return Amount of coins that were taken from the item stack but couldn't be stored on the account
     * @throws DataBaseException
     */
    int depositToAccount(Object account, Collection<ItemStack> coins, Transaction transaction) throws DataBaseException;

    int depositToAccount(Object account, ItemStack coins, Transaction transaction) throws DataBaseException;

    int depositToAccount(Object account, int coins, Transaction transaction) throws DataBaseException, IllegalArgumentException;

    void saveNewMachine(@Nonnull Machine machine) throws DataBaseException;

    void saveTransaction(@Nonnull Transaction transaction) throws DataBaseException;

    @Nonnull
    PlayerData getPlayerData(@Nonnull UUID playerUID) throws DataBaseException;

    @Nonnull
    AccountAddress createPrimaryAccount(@Nonnull UUID playerUID, @Nonnull String name) throws DataBaseException;

    @SuppressWarnings("DuplicateThrows")
    int takeFromAccount(Object account, int amount, Transaction transaction) throws DataBaseException, OutOfCoinsException;

    AccountAddress getCustomAccountByName(String customAccountName) throws DataBaseException;

    AccountAddress createCustomAccount(UUID playerUID, String customAccountName) throws DataBaseException;

    AccountAddress transferAccount(AccountAddress origin, String destiny, Machine machine, Operator operator) throws DataBaseException;

    AccountAddress transferPrimaryAccount(AccountAddress primaryAccount, String newName, Machine machine, Operator operator) throws DataBaseException;
}
