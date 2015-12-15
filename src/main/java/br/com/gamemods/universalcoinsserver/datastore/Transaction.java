package br.com.gamemods.universalcoinsserver.datastore;

import br.com.gamemods.universalcoinsserver.tile.TileVendor;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public final class Transaction
{
    private UUID id = UUID.randomUUID();
    private long time = System.currentTimeMillis();
    private Machine machine;
    private Operator operator;
    private ItemStack product;
    private ItemStack trade;
    private Operation operation;
    private boolean infiniteMachine;
    private int quantity;
    private int price;
    private int totalPrice;
    private CoinSource userCoinSource;
    private CoinSource ownerCoinSource;

    public Transaction(TileVendor vendor, Operation operation, int quantity,
                        CoinSource userSource, CoinSource ownerSource, ItemStack product)
    {
        this.operation = operation;
        machine = vendor;
        operator = vendor.getOperator();
        infiniteMachine = vendor.infinite;
        this.quantity = quantity;
        price = vendor.price;
        totalPrice = price * quantity;
        userCoinSource = userSource;
        ownerCoinSource = ownerSource;
        trade = vendor.getStackInSlot(TileVendor.SLOT_TRADE).copy();
        this.product = product.copy();
    }

    public static abstract class CoinSource
    {
        public abstract int getBalanceBefore();
        public abstract int getBalanceAfter();
    }

    public static class CardCoinSource extends CoinSource
    {
        private ItemStack card;
        private String accountNumber;
        private String accountOwner;
        private int balanceBefore;
        private int balanceAfter;

        @Override
        public int getBalanceAfter()
        {
            return balanceAfter;
        }

        @Override
        public int getBalanceBefore()
        {
            return balanceBefore;
        }

        @Override
        public String toString()
        {
            return "CardCoinSource{" +
                    "accountNumber='" + accountNumber + '\'' +
                    ", card=" + card +
                    ", accountOwner='" + accountOwner + '\'' +
                    ", balanceBefore=" + balanceBefore +
                    ", balanceAfter=" + balanceAfter +
                    '}';
        }
    }

    public static class MachineCoinSource extends CoinSource
    {
        private Machine machine;
        private int balanceBefore;
        private int balanceAfter;

        public MachineCoinSource(Machine machine, int balance, int increment)
        {
            this.machine = machine;
            this.balanceBefore = balance;
            this.balanceAfter = balance + increment;
        }

        @Override
        public int getBalanceAfter()
        {
            return balanceAfter;
        }

        @Override
        public int getBalanceBefore()
        {
            return balanceBefore;
        }

        @Override
        public String toString()
        {
            return "MachineCoinSource{" +
                    "balanceAfter=" + balanceAfter +
                    ", machine=" + machine +
                    ", balanceBefore=" + balanceBefore +
                    '}';
        }
    }

    public enum CoinSourceType
    {
        COIN,
        CARD
    }

    public enum Operation
    {
        BUY_FROM_MACHINE,
        SELL_TO_MACHINE,
        DEPOSIT_TO_MACHINE,
        WITHDRAW_FROM_MACHINE
    }

    public UUID getId()
    {
        return id;
    }

    public boolean isInfiniteMachine()
    {
        return infiniteMachine;
    }

    public Machine getMachine()
    {
        return machine;
    }

    public Operation getOperation()
    {
        return operation;
    }

    public Operator getOperator()
    {
        return operator;
    }

    public CoinSource getOwnerCoinSource()
    {
        return ownerCoinSource;
    }

    public int getPrice()
    {
        return price;
    }

    public ItemStack getProduct()
    {
        return product;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public long getTime()
    {
        return time;
    }

    public int getTotalPrice()
    {
        return totalPrice;
    }

    public ItemStack getTrade()
    {
        return trade;
    }

    public CoinSource getUserCoinSource()
    {
        return userCoinSource;
    }

    @Override
    public String toString()
    {
        return "Transaction{" +
                "id=" + id +
                ", time=" + time +
                ", machine=" + machine +
                ", operator=" + operator +
                ", product=" + product +
                ", trade=" + trade +
                ", operation=" + operation +
                ", infiniteMachine=" + infiniteMachine +
                ", quantity=" + quantity +
                ", price=" + price +
                ", totalPrice=" + totalPrice +
                ", userCoinSource=" + userCoinSource +
                ", ownerCoinSource=" + ownerCoinSource +
                '}';
    }
}
