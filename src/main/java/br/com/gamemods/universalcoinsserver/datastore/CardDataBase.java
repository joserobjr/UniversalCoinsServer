package br.com.gamemods.universalcoinsserver.datastore;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * A base layer to load and store data from accounts.
 * <p>
 * The implementation is free to use any type of object as account number, however it must have a good String output
 * and must be able to identify the account by an {@link String} created from this object.
 * <p>
 * All {@link Object} account parameters on methods from this interfaces must accepts
 * {@link AccountAddress}, {@link ItemStack} and {@link String} or call {@link Object#toString()}
 * to use as account number. If an {@link ItemStack} is used the item on it must be
 * an instance of {@link br.com.gamemods.universalcoinsserver.item.ItemCard} and the account number must be read from
 * the NBT tags.
 * @see br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI#getAddress(ItemStack)
 * @see br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI#getAccountNumber(Object)
 */
public interface CardDataBase
{
    @Nullable
    UUID getAccountOwner(@Nonnull Object account) throws DataStoreException;

    int getAccountBalance(@Nonnull Object account) throws DataStoreException, AccountNotFoundException;

    int canDeposit(@Nonnull Object account, @Nullable Collection<ItemStack> coins) throws DataStoreException, AccountNotFoundException;

    int canDeposit(@Nonnull Object account, @Nullable ItemStack coins) throws DataStoreException, AccountNotFoundException;

    int canDeposit(@Nonnull Object account, int coins) throws DataStoreException, AccountNotFoundException;

    /**
     * Deposit coins from stacks and decrement the stack sizes according to the amount that could be deposited.
     * <p>
     * It follows the same contract as {@link #depositToAccount(Object, ItemStack, Transaction)}
     * @param coins A collection of stack of coins, the stackSizes will be decremented
     * @return Positive: The amount of coins that couldn't be deposited<br>
     *         Negative: The amount of coins that must be refunded to the player<br>
     *         Zero: Deposit cancelled or successful (the stackSize won't be changed if the deposit was cancelled)
     */
    int depositToAccount(@Nonnull Object account, @Nullable Collection<ItemStack> coins, @Nonnull Transaction transaction) throws DataStoreException, AccountNotFoundException;

    /**
     * Deposit coins from an stack and decrement the stack size according to the amount that could be deposited.
     * <p>
     * The implementation can deposit the entire stack and specify the value that must be returned to the player or it
     * can take only what it can store and return what couldn't be stored or it can just abort the deposit and return zero.
     * @param coins A stack of coins, the stackSize will be decremented
     * @return Positive: The amount of coins that couldn't be deposited<br>
     *         Negative: The amount of coins that must be refunded to the player<br>
     *         Zero: Deposit cancelled or successful (the stackSize won't be changed if the deposit was cancelled)
     */
    int depositToAccount(@Nonnull Object account, @Nullable ItemStack coins, @Nonnull Transaction transaction) throws DataStoreException, AccountNotFoundException;

    /**
     * @return The amount of coins that couldn't be deposited
     * @throws IllegalArgumentException If @{code coins < 0}
     */
    int depositToAccount(@Nonnull Object account, int coins, @Nonnull Transaction transaction) throws DataStoreException, AccountNotFoundException, IllegalArgumentException;

    void saveNewMachine(@Nonnull Machine machine) throws DataStoreException;

    void saveTransaction(@Nonnull Transaction transaction) throws DataStoreException;

    @Nonnull
    PlayerData getPlayerData(@Nonnull UUID playerUID) throws DataStoreException;

    @Nonnull
    AccountAddress createPrimaryAccount(@Nonnull UUID playerUID, @Nonnull String name) throws DataStoreException, DuplicatedKeyException;

    /**
     * @return The balance after the coins are taken
     */
    int takeFromAccount(@Nonnull Object account, int amount, @Nonnull Transaction transaction)
            throws DataStoreException, AccountNotFoundException, OutOfCoinsException;

    @Nullable
    AccountAddress getCustomAccountByName(@Nonnull String customAccountName) throws DataStoreException;

    @Nonnull
    AccountAddress createCustomAccount(@Nonnull UUID playerUID, @Nonnull String customAccountName)
            throws DataStoreException, DuplicatedKeyException;

    /**
     * Invalidates the custom account while transferring the balance to a new one
     * @param origin Custom account address, if a primary account is used it will attempt to transfer a custom account
     *               with the same name as the creator of the primary account.
     * @throws AccountNotFoundException If no account alternative account was found with that name. The name is checked before the number.
     */
    @Nonnull
    AccountAddress transferAccount(@Nonnull AccountAddress origin, @Nonnull String destiny, @Nullable Machine machine, @Nullable Operator operator)
            throws DataStoreException, AccountNotFoundException, DuplicatedKeyException;

    /**
     * Invalidates the primary account while transferring the balance to a new one
     */
    @Nonnull
    AccountAddress transferPrimaryAccount(@Nonnull AccountAddress primaryAccount, @Nonnull String newName, @Nullable Machine machine, @Nullable Operator operator)
            throws DataStoreException, AccountNotFoundException;

    /**
     * Increment the accounts and saves the transaction safely
     */
    void processTrade(@Nonnull Transaction transaction)
            throws DataStoreException, AccountNotFoundException, OutOfCoinsException;

    Collection<PlayerData> getAllPlayerData() throws DataStoreException;

    Map<AccountAddress,Integer> getAllAccountsBalance() throws DataStoreException;

    void importData(CardDataBase original) throws DataStoreException;

    AccountAddress renamePrimaryAccount(AccountAddress primaryAccount, String playerName) throws DataStoreException, AccountNotFoundException;
}
