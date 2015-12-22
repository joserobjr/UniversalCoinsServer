package br.com.gamemods.universalcoinsserver.tile;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.datastore.*;
import br.com.gamemods.universalcoinsserver.item.ItemCoin;
import br.com.gamemods.universalcoinsserver.net.TileCardStationMessage;
import com.google.common.primitives.Ints;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TileCardStation extends TileTransactionMachine
{
    public static final int SLOT_COIN = 0;
    public static final int SLOT_CARD = 1;
    public static final int BUTTON_ONE = -1;
    public static final int BUTTON_TWO = -2;
    public static final int BUTTON_THREE = -3;
    public static final int BUTTON_FOUR = -4;
    public static final int FUNCTION_NONE = 0;
    public static final int FUNCTION_NEW_CARD = 1;
    public static final int FUNCTION_TRANSFER_ACCOUNT = 2;
    public static final int FUNCTION_DEPOSIT = 3;
    public static final int FUNCTION_WITHDRAW = 4;
    public static final int FUNCTION_ACCOUNT_INFO = 5;
    public static final int FUNCTION_DESTROY_CARD = 6;
    public static final int FUNCTION_NEW_CUSTOM_ACCOUNT_OR_CARD = 7;
    public static final int FUNCTION_NEW_EXTRA_CARD = 8;
    public static final int FUNCTION_TRANSFER_CUSTOM_ACCOUNT = 9;
    public static final int FUNCTION_ACCOUNT_ERROR = 10;
    public static final int GUI_WELCOME = 0;
    public static final int GUI_AUTHENTICATION = 1;
    public static final int GUI_MAIN_MENU = 2;
    public static final int GUI_ADDITIONAL_MENU = 3;
    public static final int GUI_BALANCE = 4;
    public static final int GUI_DEPOSIT = 5;
    public static final int GUI_WITHDRAW = 6;
    public static final int GUI_NEW_CARD = 7;
    public static final int GUI_TRANSFER_ACCOUNT = 8;
    public static final int GUI_CUSTOM_ACCOUNT = 9;
    public static final int GUI_TAKE_CARD = 10;
    public static final int GUI_TAKE_COINS = 11;
    public static final int GUI_INSUFFICIENT_FUNDS = 12;
    public static final int GUI_INVALID_INPUT = 13;
    public static final int GUI_BAD_CARD = 14;
    public static final int GUI_UNAUTHORIZED_ACCESS = 15;
    public static final int GUI_ACCOUNT_OPTIONS = 16;
    public static final int GUI_NEW_ACCOUNT = 17;
    public static final int GUI_DUPLICATED_ACCOUNT = 18;
    public static final int GUI_PROCESSING = 19;


    private ItemStack[] inventory = new ItemStack[2];
    public TileCardStationMessage state = new TileCardStationMessage();
    private int coins;
    private List<Integer> validOperations = Ints.asList(FUNCTION_ACCOUNT_INFO, FUNCTION_DESTROY_CARD);
    private Runnable schedule;
    private int scheduleTicks = -1;
    private ItemStack depositFailure = null;
    private Runnable cardRemovalHook;
    public Runnable[] customButtonOperation;
    public String customAccountName;

    public TileCardStation()
    {
        state.owner = this;
        state.reset();
    }

    public void validateFields()
    {
        if(coins < 0) coins = 0;
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if(scheduleTicks == 0)
        {
            Runnable last = schedule;
            schedule.run();
            if(schedule == last)
                cancelSchedule();
        }
        else if(scheduleTicks > 0)
            scheduleTicks--;

        if(state.depositCoins && state.cardAccount != null)
        {
            ItemStack stack = inventory[SLOT_COIN];
            if(stack != null && stack.getItem() instanceof ItemCoin && !ItemStack.areItemStacksEqual(depositFailure, stack))
            {
                int value = UniversalCoinsServerAPI.stackValue(stack);
                if(value <= 0)
                    return;

                try
                {
                    if(UniversalCoinsServer.cardDb.canDeposit(state.cardAccount.getNumber(), value) < 0)
                        return;

                    Transaction transaction = new Transaction(
                            this,
                            Transaction.Operation.DEPOSIT_TO_ACCOUNT_FROM_MACHINE,
                            new PlayerOperator(opener),
                            null,
                            createCardCoinSource(value),
                            stack
                    );

                    UniversalCoinsServer.cardDb.depositToAccount(state.cardAccount.getNumber(), value, transaction);
                    stack.stackSize = 0;
                    inventory[SLOT_COIN] = null;
                    markDirty();
                    worldObj.playSoundEffect(xCoord, yCoord, zCoord, "universalcoins:insert_coin", 1f, 1f);
                    state.accountBalance = UniversalCoinsServer.cardDb.getAccountBalance(state.cardAccount.getNumber());
                    depositFailure = null;
                }
                catch (DataBaseException e)
                {
                    e.printStackTrace();
                    depositFailure = stack.copy();
                    //UniversalCoinsServer.logger.error(e);
                }
            }
        }
    }

    public void schedule(Runnable task, int ticks)
    {
        schedule = task;
        scheduleTicks = ticks;
    }

    public void cancelSchedule()
    {
        schedule = null;
        scheduleTicks = -1;
    }

    public void onCardRemoved()
    {
        if(state.forcedMenuState == GUI_TAKE_CARD)
            state.stopForcing();

        if(cardRemovalHook != null)
        {
            Runnable before = cardRemovalHook;
            cardRemovalHook.run();
            if(before == cardRemovalHook)
                cardRemovalHook = null;
        }
    }

    @Override
    public void setInventorySlotContents(final int slot, ItemStack stack)
    {
        if(stack != null && stack.stackSize <= 0)
            stack = null;

        inventory[slot] = stack;
        markDirty();

        if(stack != null)
        {
            switch (slot)
            {
                case SLOT_CARD:
                {
                    state.cardAccount = UniversalCoinsServerAPI.getAddress(stack);
                    boolean valid;
                    if(state.cardAccount != null)
                        try
                        {
                            UUID accountOwner = UniversalCoinsServer.cardDb.getAccountOwner(state.cardAccount.getNumber().toString());
                            valid = accountOwner != null && accountOwner.equals(state.cardAccount.getOwner());
                        }
                        catch (DataBaseException e)
                        {
                            UniversalCoinsServer.logger.error(e);
                            e.printStackTrace();
                            valid = false;
                        }
                    else
                        valid = true;

                    if(state.cardAccount == null || !valid)
                    {
                        validOperations = Collections.singletonList(FUNCTION_DESTROY_CARD);

                        state.reset();
                        state.force(GUI_BAD_CARD);
                        schedule(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                inventory[slot] = null;
                                schedule(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if(state.forcedMenuState == GUI_BAD_CARD)
                                        {
                                            state.stopForcing();
                                            reset();
                                            state.reset();
                                        }
                                    }
                                }, 3*20);
                            }
                        }, 0);
                        return;
                    }
                    else
                    {
                        try
                        {
                            boolean open = stack.stackTagCompound.getBoolean("Open");
                            if(!open &&!state.playerUID.equals(state.cardAccount.getOwner()))
                            {
                                validOperations = Collections.singletonList(FUNCTION_DESTROY_CARD);

                                state.force(GUI_UNAUTHORIZED_ACCESS);
                                cardRemovalHook = new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        state.reset();
                                        state.stopForcing();
                                        cancelSchedule();
                                        reset();
                                    }
                                };
                                final Runnable[] states = new Runnable[2];
                                final int time = 30;
                                states[0] = new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if(state.forcedMenuState == GUI_UNAUTHORIZED_ACCESS)
                                        {
                                            state.force(GUI_TAKE_CARD);
                                            schedule(states[1], time);
                                        }
                                    }
                                };
                                states[1] = new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if(state.forcedMenuState == GUI_TAKE_CARD)
                                        {
                                            state.force(GUI_UNAUTHORIZED_ACCESS);
                                            schedule(states[0], time);
                                        }
                                    }
                                };
                                schedule(states[0], time);
                            }
                            else
                            {
                                validOperations = null;
                                state.activeCard = stack.copy();
                                if(open)
                                    state.customAccount = state.cardAccount;
                                else
                                    state.setPlayerData(UniversalCoinsServer.cardDb.getPlayerData(state.playerUID));

                                state.accountBalance = UniversalCoinsServer.cardDb.getAccountBalance(state.cardAccount.getNumber());
                                state.forcedMenuState = GUI_TAKE_CARD;
                                state.accountError = false;
                                customButtonOperation = new Runnable[]{null,null,null,new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        state.stopForcing();
                                    }
                                }};
                            }
                        }
                        catch (DataBaseException e)
                        {
                            e.printStackTrace();

                            validOperations = Collections.singletonList(FUNCTION_DESTROY_CARD);
                            state.reset();
                            state.force(GUI_INVALID_INPUT);
                        }
                    }
                    return;
                }
            }
        }
        else if(slot == SLOT_CARD)
            onCardRemoved();
        else if(slot == SLOT_COIN)
        {
            if(coins <= 0)
                onCoinsRemoved();
            else if(schedule == null)
                schedule(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        fillCoinSlot();
                    }
                }, 0);
            else
                fillCoinSlot();
        }
    }

    public void onCoinsRemoved()
    {
        state.withdrawCoins = false;
    }

    @Override
    public ItemStack decrStackSize(int slot, int size)
    {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null)
        {
            if (stack.stackSize <= size)
            {
                inventory[slot] = null;
                if(coins <= 0)
                    onCoinsRemoved();
            }
            else
            {
                stack = stack.splitStack(size);
                if (stack.stackSize == 0)
                {
                    inventory[slot] = null;
                    if(coins <= 0)
                        onCoinsRemoved();
                }
            }
        }

        if(slot == SLOT_CARD && inventory[SLOT_CARD] == null)
            onCardRemoved();

        if(schedule == null)
            schedule(new Runnable()
            {
                @Override
                public void run()
                {
                    fillCoinSlot();
                }
            }, 0);
        else
            fillCoinSlot();
        return stack;
    }

    private void exportCard(final ItemStack card)
    {
        if(inventory[SLOT_CARD] != null)
        {
            state.force(GUI_TAKE_CARD, 0, false);
            cardRemovalHook = new Runnable()
            {
                @Override
                public void run()
                {
                    schedule(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setInventorySlotContents(SLOT_CARD, card);
                            state.stopForcing();
                            markDirty();
                        }
                    },0);
                }
            };
        }
        else
        {
            inventory[SLOT_CARD] = card;
            markDirty();
        }
    }

    @Override
    public void onButtonPressed(final EntityPlayerMP player, int buttonId, final boolean shiftPressed)
    {
        Logger logger = UniversalCoinsServer.logger;
        logger.info("Button: " + buttonId);

        if(buttonId < 0 && state.forcedMenuState >= 0)
        {
            buttonId = -1-buttonId;
            if(customButtonOperation != null && customButtonOperation.length>buttonId)
            {
                Runnable runnable = customButtonOperation[buttonId];
                if(runnable != null)
                    runnable.run();
            }
            return;
        }

        if(validOperations != null && !validOperations.contains(buttonId))
        {
            logger.warn("Invalid operation "+buttonId);
            return;
        }

        state.accountError = false;

        switch (buttonId)
        {
            case FUNCTION_NONE:
                state.depositCoins = false;
                state.withdrawCoins = false;
                break;
            case FUNCTION_ACCOUNT_INFO:
                try
                {
                    state.reset();
                    state.setPlayerData(UniversalCoinsServer.cardDb.getPlayerData(state.playerUID));
                    state.cardAccount = state.primaryAccount;
                    if(state.cardAccount != null)
                    {
                        state.accountBalance = UniversalCoinsServer.cardDb.getAccountBalance(state.cardAccount.getNumber());
                        validOperations = null;
                    }
                    else
                        validOperations = Collections.singletonList(FUNCTION_NEW_CARD);
                }
                catch (DataBaseException e)
                {
                    e.printStackTrace();
                    state.accountError = true;
                }
                scheduleUpdate();
                break;
            case FUNCTION_TRANSFER_CUSTOM_ACCOUNT:
                try
                {
                    if(customAccountName == null || customAccountName.isEmpty() || state.customAccount == null
                            || UniversalCoinsServer.cardDb.getCustomAccountByName(customAccountName) != null)
                    {
                        state.accountError = true;
                        return;
                    }

                    AccountAddress oldAccount = state.customAccount;
                    state.customAccount = UniversalCoinsServer.cardDb.transferAccount(state.customAccount, customAccountName, this, new PlayerOperator(opener));
                    if(state.cardAccount == oldAccount)
                        state.cardAccount = state.customAccount;

                    state.accountError = false;
                    exportCard(UniversalCoinsServerAPI.createCard(state.customAccount, true));
                    return;
                }
                catch (DataBaseException e)
                {
                    e.printStackTrace();
                    state.accountError = true;
                    return;
                }
            case FUNCTION_TRANSFER_ACCOUNT:
                try
                {
                    if(state.primaryAccount ==null)
                    {
                        state.accountError = true;
                        return;
                    }

                    AccountAddress oldAccount = state.primaryAccount;
                    state.primaryAccount = UniversalCoinsServer.cardDb.transferPrimaryAccount(state.primaryAccount, state.playerName, this, new PlayerOperator(opener));
                    if(state.cardAccount == oldAccount)
                        state.cardAccount = state.primaryAccount;

                    state.accountError = false;
                    exportCard(UniversalCoinsServerAPI.createCard(state.primaryAccount, false));
                    return;
                }
                catch (DataBaseException e)
                {
                    e.printStackTrace();
                    state.accountError = true;
                    return;
                }
            case FUNCTION_NEW_CUSTOM_ACCOUNT_OR_CARD:
                try
                {
                    boolean createAccount;
                    if(customAccountName == null && state.customAccount != null)
                    {
                        createAccount = false;
                        PlayerData playerData = UniversalCoinsServer.cardDb.getPlayerData(state.playerUID);
                        if(playerData.getAlternativeAccounts().size() > 1)
                        {
                            state.accountError = true;
                            return;
                        }
                    }
                    else if(customAccountName == null||customAccountName.isEmpty())
                    {
                        state.accountError = true;
                        return;
                    }
                    else
                    {
                        AccountAddress other = UniversalCoinsServer.cardDb.getCustomAccountByName(customAccountName);
                        if(other != null && !other.getOwner().equals(state.playerUID))
                        {
                            state.accountError = true;
                            return;
                        }
                        createAccount = true;
                    }

                    if(createAccount)
                        state.customAccount = UniversalCoinsServer.cardDb.createCustomAccount(state.playerUID, customAccountName);

                    ItemStack card = UniversalCoinsServerAPI.createCard(state.customAccount, true);
                    exportCard(card);

                    return;
                }
                catch (DataBaseException e)
                {
                    state.accountError = true;
                    e.printStackTrace();
                    return;
                }
            case FUNCTION_NEW_CARD:
                try
                {
                    if(state.primaryAccount == null)
                    {
                        state.setPlayerData(UniversalCoinsServer.cardDb.getPlayerData(state.playerUID));
                        if(state.cardAccount == null)
                            state.cardAccount = state.primaryAccount;
                    }

                    if(state.cardAccount == null)
                    {
                        state.primaryAccount = UniversalCoinsServer.cardDb.createPrimaryAccount(state.playerUID, state.playerName);
                        state.cardAccount = state.primaryAccount;
                    }
                    else if(state.primaryAccount.getName().equals(state.primaryAccount.getNumber()) && !state.playerName.equals(state.primaryAccount.getNumber()))
                    {
                        state.primaryAccount = UniversalCoinsServer.cardDb.renamePrimaryAccount(state.primaryAccount, state.playerName);
                        state.cardAccount = state.primaryAccount;
                    }
                    inventory[SLOT_CARD] = UniversalCoinsServerAPI.createCard(state.primaryAccount, false);
                    markDirty();
                    state.accountBalance = 0;
                }
                catch (DataBaseException|NullPointerException e)
                {
                    e.printStackTrace();
                    state.accountError = true;
                }
                scheduleUpdate();
                return;
            case FUNCTION_DESTROY_CARD:
                inventory[SLOT_CARD] = null;
                state.reset();
                reset();
                markDirty();
                return;
            case FUNCTION_DEPOSIT:
                if(state.cardAccount == null)
                {
                    state.force(GUI_UNAUTHORIZED_ACCESS);
                    validOperations = Collections.singletonList(FUNCTION_DESTROY_CARD);
                    scheduleUpdate();
                    return;
                }
                state.depositCoins = true;
                state.withdrawCoins = false;
                return;
            case FUNCTION_WITHDRAW:
                if(state.cardAccount == null || state.coinWithdrawalAmount <= 0)
                {
                    state.force(GUI_UNAUTHORIZED_ACCESS);
                    validOperations = Collections.singletonList(FUNCTION_DESTROY_CARD);
                    scheduleUpdate();
                    return;
                }

                try
                {
                    state.accountBalance = UniversalCoinsServer.cardDb.getAccountBalance(state.cardAccount.getNumber());
                    state.coinWithdrawalAmount = Math.min(state.accountBalance, state.coinWithdrawalAmount);
                    int withdraw = state.coinWithdrawalAmount;
                    if(withdraw <= 0)
                    {
                        state.accountError = true;
                        return;
                    }

                    Transaction transaction = new Transaction(this,
                            Transaction.Operation.WITHDRAW_FROM_ACCOUNT_TO_MACHINE,
                            new PlayerOperator(opener),
                            new Transaction.MachineCoinSource(this, coins, withdraw),
                            createCardCoinSource(-withdraw),
                            null
                    );

                    state.accountBalance = UniversalCoinsServer.cardDb.takeFromAccount(state.cardAccount.getNumber(), withdraw, transaction);
                    coins += withdraw;

                    state.withdrawCoins = true;
                    state.depositCoins = false;
                    fillCoinSlot();
                }
                catch (DataBaseException e)
                {
                    e.printStackTrace();
                    state.accountError = true;
                }
        }
    }

    public Transaction.CardCoinSource createCardCoinSource(int increment) throws DataBaseException
    {
        if(state.activeCard != null)
            return new Transaction.CardCoinSource(state.activeCard, increment);
        else
            return new Transaction.CardCoinSource(state.cardAccount, increment);
    }

    public void fillCoinSlot()
    {
        if(state.withdrawCoins && coins > 0)
        {
            int before = coins;
            coins = UniversalCoinsServerAPI.addCoinsToSlot(this, coins, SLOT_COIN);
            if(before != coins)
            {
                worldObj.playSoundEffect(xCoord, yCoord, zCoord,
                        inventory[SLOT_COIN].stackSize > 1?
                                "universalcoins:take_coins":
                                "universalcoins:take_coin"
                        , 1.0F, 1.0F);
            }
            markDirty();
        }
    }

    public void reset()
    {
        cardRemovalHook = null;
        schedule = null;
        scheduleTicks = -1;
        validOperations = Ints.asList(FUNCTION_ACCOUNT_INFO, FUNCTION_DESTROY_CARD);
    }

    @Override
    public void setOpener(EntityPlayer opener)
    {
        super.setOpener(opener);
        if(opener == null)
            reset();
        state.reset();
        if(opener != null)
        {
            ItemStack stack = inventory[SLOT_CARD];
            if(stack != null)
                setInventorySlotContents(SLOT_CARD, stack);
        }
        else
            state.stopForcing();
        scheduleUpdate();
    }

    @Override
    public Packet getDescriptionPacket()
    {
        state.x = xCoord;
        state.y = yCoord;
        state.z = zCoord;
        return UniversalCoinsServer.network.getPacketFrom(state);
    }


    @Override
    public int getSizeInventory()
    {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return inventory[slot];
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inventory.length; i++)
        {
            ItemStack stack = inventory[i];
            if (stack != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }

        tagCompound.setTag("Inventory", itemList);
        tagCompound.setBoolean("InUse", opener != null);
        tagCompound.setBoolean("DepositCoins", state.depositCoins);
        tagCompound.setBoolean("WithdrawCoins", state.withdrawCoins);
        tagCompound.setInteger("CoinWithdrawalAmount", state.coinWithdrawalAmount);
        tagCompound.setInteger("coins", coins);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        NBTTagList tagList = compound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
        Arrays.fill(inventory, null);
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            byte slot = tag.getByte("Slot");
            if (slot >= 0 && slot < inventory.length)
                inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
        }

        state.depositCoins = compound.getBoolean("DepositCoins");
        state.withdrawCoins = compound.getBoolean("WithdrawCoins");
        state.coinWithdrawalAmount = compound.getInteger("CoinWithdrawalAmount");
        coins = compound.getInteger("Coins");
        validateFields();
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot)
    {
        return getStackInSlot(slot);
    }

    @Override
    public String getInventoryName()
    {
        return null;
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
    }

    @Override
    public void openInventory()
    { }

    @Override
    public void closeInventory()
    { }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return slot == SLOT_COIN && state.withdrawCoins && stack.getItem() instanceof ItemCoin;
    }
}
