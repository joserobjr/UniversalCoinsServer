package br.com.gamemods.universalcoinsserver.tile;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.datastore.DataBaseException;
import br.com.gamemods.universalcoinsserver.datastore.PlayerOperator;
import br.com.gamemods.universalcoinsserver.datastore.Transaction;
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
    public static final int FUNCTION_NEW_ACCOUNT = 7;
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
                            new Transaction.CardCoinSource(state.activeCard, value),
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
            cardRemovalHook.run();
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
                    if(state.cardAccount == null)
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
                            if(!state.playerUID.equals(state.cardAccount.getOwner()))
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
                                state.accountBalance = UniversalCoinsServer.cardDb.getAccountBalance(state.cardAccount.getNumber());
                                state.forcedMenuState = GUI_TAKE_CARD;
                                state.accountError = false;
                            }
                        }
                        catch (DataBaseException e)
                        {
                            UniversalCoinsServer.logger.error(e);

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
    }

    @Override
    public ItemStack decrStackSize(int slot, int size)
    {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null)
        {
            if (stack.stackSize <= size)
                setInventorySlotContents(slot, null);
            else
            {
                stack = stack.splitStack(size);
                if (stack.stackSize == 0)
                    setInventorySlotContents(slot, null);
            }
        }

        if(slot == SLOT_CARD && inventory[SLOT_CARD] == null)
            onCardRemoved();

        fillCoinSlot();
        return stack;
    }

    @Override
    public void onButtonPressed(EntityPlayerMP player, int buttonId, boolean shiftPressed)
    {
        Logger logger = UniversalCoinsServer.logger;
        logger.info("Button: " + buttonId);

        if(validOperations != null && !validOperations.contains(buttonId))
        {
            logger.warn("Invalid operation "+buttonId);
            return;
        }

        state.accountError = false;

        switch (buttonId)
        {
            case FUNCTION_ACCOUNT_INFO:
                try
                {
                    state.reset();
                    state.setPlayerData(UniversalCoinsServer.cardDb.getPlayerData(state.playerUID));
                    state.cardAccount = state.primaryAccount;
                    getDescriptionPacket();
                }
                catch (DataBaseException e)
                {
                    logger.error(e);
                    state.accountError = true;
                }
                scheduleUpdate();
                break;
            case FUNCTION_NEW_CARD:
                try
                {
                    if(state.cardAccount == null)
                    {
                        state.primaryAccount = UniversalCoinsServer.cardDb.createPrimaryAccount(state.playerUID, state.playerName);
                        state.cardAccount = state.primaryAccount;

                    }
                    ItemStack stack = new ItemStack(UniversalCoinsServer.proxy.itemCard);
                    stack.stackTagCompound = new NBTTagCompound();
                    stack.stackTagCompound.setString("Name", state.primaryAccount.getName());
                    stack.stackTagCompound.setString("Owner", state.primaryAccount.getOwner().toString());
                    stack.stackTagCompound.setString("Account", state.primaryAccount.getNumber().toString());
                    inventory[SLOT_CARD] = stack;
                    state.accountBalance = 0;
                }
                catch (DataBaseException e)
                {
                    logger.error(e);
                    state.accountError = true;
                }
                scheduleUpdate();
                return;
            case FUNCTION_DESTROY_CARD:
                inventory[SLOT_CARD] = null;
                scheduleUpdate();
                return;
            case FUNCTION_DEPOSIT:
                if(state.cardAccount == null)
                {
                    state.accountError = true;
                    scheduleUpdate();
                    return;
                }
                state.depositCoins = true;
                state.withdrawCoins = false;
        }
    }

    public void fillCoinSlot()
    {

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
        tagCompound.setInteger("CoinWithdrawalAmount", coins);
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
        coins = compound.getInteger("CoinWithdrawalAmount");
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
        return false;
    }
}
