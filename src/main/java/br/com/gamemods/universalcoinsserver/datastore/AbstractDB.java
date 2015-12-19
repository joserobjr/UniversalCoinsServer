package br.com.gamemods.universalcoinsserver.datastore;

import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.item.ItemCoin;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

public abstract class AbstractDB implements CardDataBase
{
    protected int maxAccountValue = Integer.MAX_VALUE;

    class Account
    {
        final String id;
        final UUID owner;
        int balance;

        public Account(String id, UUID owner)
        {
            this.id = id;
            this.owner = owner;
        }

        public Account(String id, UUID owner, int balance)
        {
            this.id = id;
            this.owner = owner;
            this.balance = balance;
        }

        public int getBalance()
        {
            return balance;
        }

        public void incrementBalance(int increment, @Nonnull Transaction transaction) throws DataStoreException
        {
            balance += increment;
            saveTransaction(transaction);
        }
    }

    @Nullable
    protected abstract Account getAccount(@Nonnull String number) throws DataStoreException;

    protected Account getAccount(@Nullable Object account) throws DataStoreException
    {
        String number = getAccountNumber(account);
        return number == null? null : getAccount(number);
    }

    @Nullable
    protected String getAccountNumber(@Nullable Object account)
    {
        if(account instanceof String) return (String) account;
        if(account instanceof AccountAddress) return ((AccountAddress) account).getNumber().toString();
        if(account == null) return null;
        if(account instanceof ItemStack)
        {
            AccountAddress address = UniversalCoinsServerAPI.getAddress((ItemStack) account);
            return address == null? null : address.getNumber().toString();
        }
        return account.toString();
    }

    @Nullable
    @Override
    public UUID getAccountOwner(@Nonnull Object account) throws DataStoreException
    {
        Account acc = getAccount(account);
        return acc == null? null : acc.owner;
    }

    @Override
    public int getAccountBalance(@Nonnull Object account) throws DataStoreException, AccountNotFoundException
    {
        Account acc = getAccount(account);
        if(acc == null) throw new AccountNotFoundException(account);
        return acc.getBalance();
    }

    @Override
    public int canDeposit(@Nonnull Object account, @Nullable Collection<ItemStack> coins) throws DataStoreException, AccountNotFoundException
    {
        return canDeposit(account, UniversalCoinsServerAPI.stackValue(coins));
    }

    @Override
    public int canDeposit(@Nonnull Object account, @Nullable ItemStack coins) throws DataStoreException, AccountNotFoundException
    {
        return canDeposit(account, UniversalCoinsServerAPI.stackValue(coins));
    }

    @Override
    public int canDeposit(@Nonnull Object account, int coins) throws DataStoreException, AccountNotFoundException
    {
        Account acc = getAccount(account);
        if(acc == null) throw new AccountNotFoundException(account);
        return (int)(maxAccountValue - (acc.getBalance() + (long)coins));
    }

    @Override
    public int depositToAccount(@Nonnull Object account, @Nullable Collection<ItemStack> coins, @Nonnull Transaction transaction) throws DataStoreException, AccountNotFoundException
    {
        Account acc = getAccount(account);
        if(acc == null) throw new AccountNotFoundException(account);

        if(coins == null || coins.isEmpty())
            return 0;

        long balance = acc.getBalance();
        int ret = 0;
        int deposit = 0;
        Object[][] decrements = new Object[coins.size()][2];

        int i = 0;
        for(ItemStack stack: coins)
        {
            Item item;
            if(stack == null || !((item = stack.getItem()) instanceof ItemCoin) || stack.stackSize <= 0)
                return 0;

            int amountToDeposit = stack.stackSize;
            int itemValue = ((ItemCoin) item).getValue();
            int stackValue = amountToDeposit * itemValue;
            int stackDeposit = deposit + stackValue;

            long valueAboveInverted = maxAccountValue - (balance + stackValue + deposit);
            if(valueAboveInverted < 0)
            {
                amountToDeposit -= -valueAboveInverted / itemValue;
                stackDeposit = amountToDeposit * itemValue;
            }

            if(amountToDeposit <= 0)
                continue;

            deposit += stackDeposit;
            decrements[i++] = new Object[]{stack, amountToDeposit};

            ret += stackValue - deposit;
        }

        if(deposit <= 0)
            return 0;

        acc.incrementBalance(deposit, transaction);

        for(Object[] decrement: decrements)
            ((ItemStack)decrement[0]).stackSize -= (int)decrement[1];

        return ret;
    }

    @Override
    public int depositToAccount(@Nonnull Object account, @Nullable ItemStack stack, @Nonnull Transaction transaction) throws DataStoreException, AccountNotFoundException
    {
        Account acc = getAccount(account);
        if(acc == null) throw new AccountNotFoundException(account);

        Item item;
        if(stack == null || !((item = stack.getItem()) instanceof ItemCoin) || stack.stackSize <= 0)
            return 0;

        int amountToDeposit = stack.stackSize;
        int itemValue = ((ItemCoin) item).getValue();
        int stackValue = amountToDeposit * itemValue;
        int deposit = stackValue;

        long valueAboveInverted = maxAccountValue - (acc.getBalance() + (long)stackValue);
        if(valueAboveInverted < 0)
        {
            amountToDeposit -= -valueAboveInverted / itemValue;
            deposit = amountToDeposit * itemValue;
        }

        if(amountToDeposit <= 0)
            return 0;

        acc.incrementBalance(deposit, transaction);

        stack.stackSize -= amountToDeposit;

        return stackValue - deposit;
    }

    @Override
    public int depositToAccount(@Nonnull Object account, int coins, @Nonnull Transaction transaction) throws DataStoreException, AccountNotFoundException, IllegalArgumentException
    {
        if(coins < 0)
            throw new IllegalArgumentException("coins: "+coins);

        Account acc = getAccount(account);
        if(acc == null) throw new AccountNotFoundException(account);
        // Will be negative if the final account balance bypasses the maximum value
        long valueAboveInverted = maxAccountValue - (acc.getBalance() + (long)coins);

        // The value that will be deposited
        int deposit = valueAboveInverted >= 0? coins : (int)(coins + valueAboveInverted);

        if(deposit <= 0)
            return coins;

        acc.incrementBalance(deposit, transaction);

        return coins - deposit;
    }

    @Override
    public int takeFromAccount(@Nonnull Object account, int amount, @Nonnull Transaction transaction) throws DataStoreException, AccountNotFoundException, OutOfCoinsException
    {
        Account acc = getAccount(account);
        if(acc == null) throw new AccountNotFoundException(account);

        int afterIncrement = acc.balance - amount;
        if(afterIncrement < 0)
            throw new OutOfCoinsException(-afterIncrement);

        acc.incrementBalance(-amount, transaction);

        return acc.getBalance();
    }

    @Override
    public void processTrade(@Nonnull Transaction transaction) throws DataStoreException, AccountNotFoundException, OutOfCoinsException
    {
        Transaction.CoinSource ownerCoinSource = transaction.getOwnerCoinSource();
        Transaction.CoinSource userCoinSource = transaction.getUserCoinSource();
        Account ownerAccount = null, userAccount = null;
        long ownerIncrement = 0, userIncrement = 0;

        if(ownerCoinSource instanceof Transaction.CardCoinSource)
        {
            Object number = ((Transaction.CardCoinSource) ownerCoinSource).getAccountAddress().getNumber();
            ownerAccount = getAccount(number);
            if(ownerAccount == null) throw new AccountNotFoundException(number);
            ownerIncrement = ownerCoinSource.getBalanceAfter() - ownerCoinSource.getBalanceBefore();
            int balance = ownerAccount.getBalance();
            long result = balance + ownerIncrement;
            if(result < 0) throw new OutOfCoinsException((int)-result);
            if(result > maxAccountValue) throw new DataStoreException("Final balance above the limit. Balance: "+balance+" Increment:"+ownerIncrement+" Limit:"+maxAccountValue);
        }

        if(userCoinSource instanceof Transaction.CardCoinSource)
        {
            Object number = ((Transaction.CardCoinSource) userCoinSource).getAccountAddress().getNumber();
            userAccount = getAccount(number);
            if(userAccount == null) throw new AccountNotFoundException(number);

            userIncrement = userCoinSource.getBalanceAfter() - userCoinSource.getBalanceBefore();
            int balance = userAccount.getBalance();
            long result = balance + userIncrement;
            if(result < 0) throw new OutOfCoinsException((int)-result);
            if(result > maxAccountValue) throw new DataStoreException("Final balance above the limit. Balance: "+balance+" Increment:"+userIncrement+" Limit:"+maxAccountValue);
        }

        store(transaction, ownerAccount, ownerIncrement, userAccount, userIncrement);
    }

    protected abstract void store(Transaction transaction, Account ownerAccount, long ownerIncrement, Account userAccount, long userIncrement)
            throws DataStoreException;
}
