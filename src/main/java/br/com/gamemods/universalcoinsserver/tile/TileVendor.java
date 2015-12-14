package br.com.gamemods.universalcoinsserver.tile;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.blocks.PlayerOwned;
import br.com.gamemods.universalcoinsserver.datastore.*;
import br.com.gamemods.universalcoinsserver.item.ItemCoin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TileVendor extends TileEntity implements IInventory, PlayerOwned
{
    public static final int SLOT_STORAGE_FIST = 0;
    public static final int SLOT_STORAGE_LAST = 8;
    public static final int SLOT_TRADE = 9;
    public static final int SLOT_OWNER_CARD = 10;
    public static final int SLOT_SELL = 11;
    public static final int SLOT_OUTPUT = 12;
    public static final int SLOT_COIN_OUTPUT = 13;
    public static final int SLOT_OWNER_COIN_INPUT = 14;
    public static final int SLOT_USER_COIN_INPUT = 15;
    public static final int SLOT_USER_CARD = 16;
    public static final int BUTTON_MODE = 0;
    public static final int BUTTON_OWNER_COIN=3;
    public static final int BUTTON_OWNER_LARGE_BAG = 7;
    public static final int BUTTON_USER_COIN=10;
    public static final int BUTTON_USER_LARGE_BAG = 14;
    public static final int BUTTON_COLOR_MINUS = 15;
    public static final int BUTTON_COLOR_PLUS = 16;

    private ItemStack[] inventory = new ItemStack[17];

    public String ownerName;
    public UUID owner;
    public int ownerCoins;
    public int userCoins;
    public int price;
    public boolean infinite;
    public boolean sellToUser;
    public byte textColor;
    public EntityPlayer opener;
    private boolean[] buttonOwnerWithdraw = new boolean[5];
    private boolean[] buttonUserWithdraw = new boolean[5];
    private boolean outOfStock, outOfInventorySpace, buyButtonActive, sellButtonActive, outOfCoins;

    public void validateFields()
    {
        if(ownerCoins < 0) ownerCoins = 0;
        if(userCoins < 0) userCoins = 0;
        if(price < 0) price = 0;
        updateWithdrawButtons(true);
        updateWithdrawButtons(false);
        updateOperations();
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        validateFields();

        NBTTagList itemList = new NBTTagList();
        for(int i=0; i < inventory.length; i++)
        {
            ItemStack stack = inventory[i];
            if(stack != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }

        compound.setTag("Inventory", itemList);
        compound.setInteger("CoinSum", ownerCoins);
        compound.setInteger("UserCoinSum", userCoins);
        compound.setInteger("ItemPrice", price);
        if(ownerName != null && !ownerName.isEmpty())
            compound.setString("OwnerName", ownerName);
        if(owner != null)
            compound.setString("BlockOwner", owner.toString());
        compound.setBoolean("Infinite", infinite);
        compound.setBoolean("Mode", sellToUser);
        compound.setBoolean("OutOfStock", outOfStock);
        compound.setBoolean("OutOfCoins", outOfCoins);
        compound.setBoolean("InventoryFull", outOfInventorySpace);
        compound.setBoolean("BuyButtonActive", buyButtonActive);
        compound.setBoolean("SellButtonActive", sellButtonActive);
        compound.setBoolean("CoinButtonActive", buttonOwnerWithdraw[0]);
        compound.setBoolean("SmallStackButtonActive", buttonOwnerWithdraw[1]);
        compound.setBoolean("LargeStackButtonActive", buttonOwnerWithdraw[2]);
        compound.setBoolean("SmallBagButtonActive", buttonOwnerWithdraw[3]);
        compound.setBoolean("LargeBagButtonActive", buttonOwnerWithdraw[4]);
        compound.setBoolean("UserCoinButtonActive", buttonUserWithdraw[0]);
        compound.setBoolean("UserSmallStackButtonActive", buttonUserWithdraw[1]);
        compound.setBoolean("UserLargeStackButtonActive", buttonUserWithdraw[2]);
        compound.setBoolean("UserSmallBagButtonActive", buttonUserWithdraw[3]);
        compound.setBoolean("UserLargeBagButtonActive", buttonUserWithdraw[4]);
        compound.setBoolean("InUse", opener != null);
        compound.setString("BlockIcon", "");
        compound.setInteger("TextColor", textColor);
        compound.setInteger("remoteX", 0);
        compound.setInteger("remoteY", 0);
        compound.setInteger("remoteZ", 0);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        NBTTagList tagList = compound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            byte slot = tag.getByte("Slot");
            if (slot >= 0 && slot < inventory.length)
            {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
            }
        }

        ownerName = compound.getString("OwnerName");
        String str = compound.getString("BlockOwner");
        if(str.isEmpty()) owner = null;
        else
            try
            {
                owner = UUID.fromString(str);
            }
            catch (Exception e)
            {
                owner = null;
            }

        ownerCoins = compound.getInteger("CoinSum");
        userCoins = compound.getInteger("UserCoinSum");
        price = compound.getInteger("ItemPrice");
        infinite = compound.getBoolean("Infinite");
        sellToUser = compound.getBoolean("Mode");
        textColor = (byte) compound.getInteger("TextColor");

        validateFields();
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        NBTTagList tagList = nbt.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
        List<Byte> slots = new ArrayList<>(inventory.length);
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            byte slot = tag.getByte("Slot");
            slots.add(slot);
        }
        ItemStack stack = new ItemStack(Blocks.air, 0);
        for(byte i = 0; i < inventory.length; i++)
        {
            if(!slots.contains(i))
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", i);
                stack.writeToNBT(tag);
                tagList.appendTag(tag);
            }
        }
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
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
    public ItemStack decrStackSize(int slot, int size)
    {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null)
        {
            if (stack.stackSize <= size)
            {
                setInventorySlotContents(slot, null);
            }
            else
            {
                stack = stack.splitStack(size);
                if (stack.stackSize == 0)
                {
                    setInventorySlotContents(slot, null);
                }
            }
        }

        scheduleUpdate();

        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot)
    {
        opener = null;
        return getStackInSlot(slot);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        inventory[slot] = stack;
        scheduleUpdate();

        if(stack == null)
            return;

        Item item = stack.getItem();
        switch (slot)
        {
            case SLOT_OWNER_COIN_INPUT:
            case SLOT_USER_COIN_INPUT:
            {
                if(item instanceof ItemCoin)
                {
                    int current;
                    int cardSlot;
                    boolean owner = SLOT_OWNER_COIN_INPUT == slot;
                    if(owner)
                    {
                        current = ownerCoins;
                        cardSlot = SLOT_OWNER_CARD;
                    }
                    else
                    {
                        current = userCoins;
                        cardSlot = SLOT_USER_CARD;
                    }

                    int itemValue = ((ItemCoin)item).getValue();
                    int depositAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - current) / itemValue);

                    int depositValue = depositAmount * itemValue;

                    if (!checkEnderCardAcceptDeposit(cardSlot, depositValue)
                            || !depositToEnderCard(cardSlot, depositValue))
                    {
                        current += depositValue;
                    }

                    if(owner) setOwnerCoins(current);
                    else setUserCoins(current);

                    inventory[slot].stackSize -= depositAmount;
                    if (inventory[slot].stackSize == 0)
                        inventory[slot] = null;
                }
                break;
            }
        }
    }

    public void setCoins(int coins, boolean owner)
    {
        if(owner)
            setOwnerCoins(coins);
        else
            setUserCoins(coins);
    }

    public void setOwnerCoins(int ownerCoins)
    {
        this.ownerCoins = ownerCoins;
        updateWithdrawButtons(true);
    }

    public void setUserCoins(int userCoins)
    {
        this.userCoins = userCoins;
        updateWithdrawButtons(false);
    }

    public void updateWithdrawButtons(int current, int slot, boolean[] buttons)
    {
        int hash = Arrays.hashCode(buttons);
        if(current < 1)
        {
            Arrays.fill(buttons, false);
            if(hash != Arrays.hashCode(buttons))
                scheduleUpdate();
            return;
        }

        int value = 1;
        ItemStack stack = inventory[slot];
        ItemCoin coin = (stack != null && stack.getItem() instanceof ItemCoin)? (ItemCoin) stack.getItem() : null;
        for(int i = 0; i < buttons.length; i++)
        {

            buttons[i] = current >= value && (coin == null || (coin.getValue() == value && stack.stackSize < stack.getMaxStackSize()));
            value *= 9;
        }

        if(hash != Arrays.hashCode(buttons))
            scheduleUpdate();
    }

    private boolean checkEnderCardAcceptDeposit(int cardSlot, int depositAmount)
    {
        ItemStack stack = inventory[cardSlot];
        if(stack == null || !stack.hasTagCompound())
            return false;

        Item item = stack.getItem();
        if(item != UniversalCoinsServer.proxy.itemEnderCard)
            return false;

        UUID owner;
        if(cardSlot == SLOT_OWNER_CARD)
            owner = this.owner;
        else if(cardSlot == SLOT_USER_CARD)
        {
            if(opener == null) return false;
            owner = opener.getPersistentID();
        }
        else
            return false;

        String account = stack.getTagCompound().getString("Account");
        if(account.isEmpty())
            return false;

        UUID cardOwner;
        try
        {
            cardOwner = UniversalCoinsServer.cardDb.getAccountOwner(account);
        } catch (DataBaseException e)
        {
            UniversalCoinsServer.logger.warn(e);
            return false;
        }

        if(!owner.equals(cardOwner))
            return false;

        int balance;
        try
        {
            balance = UniversalCoinsServer.cardDb.getAccountBalance(account);
        } catch (DataBaseException e)
        {
            UniversalCoinsServer.logger.warn(e);
            return false;
        }
        return balance >= 0 && ((long)depositAmount)+balance < Integer.MAX_VALUE;
    }

    private boolean depositToEnderCard(int cardSlot, int depositAmount)
    {
        CardOperator operator;
        if(cardSlot == SLOT_USER_CARD)
        {
            if(opener != null) operator = new PlayerOperator(opener);
            else operator = new BlockOperator(this);
        }
        else if(cardSlot == SLOT_OWNER_CARD)
        {
            if(opener != null && opener.getPersistentID().equals(owner)) operator = new PlayerOperator(opener);
            else operator = new BlockOperator(this);
        }
        else
            return false;

        return depositToEnderCard(cardSlot, depositAmount, operator, TransactionType.DEPOSIT_FROM_MACHINE, null);
    }

    private boolean depositToEnderCard(int cardSlot, int depositAmount, CardOperator operator, TransactionType transaction, String product)
    {
        ItemStack stack = inventory[cardSlot];
        if(stack == null || !stack.hasTagCompound())
            return false;

        Item item = stack.getItem();
        if(item != UniversalCoinsServer.proxy.itemEnderCard)
            return false;

        String account = stack.getTagCompound().getString("Account");
        if(account.isEmpty())
            return false;

        try
        {
            return UniversalCoinsServer.cardDb.depositToAccount(account, depositAmount, operator, transaction, product);
        } catch (DataBaseException e)
        {
            UniversalCoinsServer.logger.warn(e);
            return false;
        }
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
    {

    }

    @Override
    public void closeInventory()
    {

    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return false;
    }

    public void scheduleUpdate()
    {
        if(worldObj != null)
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void updateBlocks()
    {

    }

    public void onButtonPressed(EntityPlayerMP player, int buttonId, boolean shiftPressed)
    {
        // Security check
        switch (buttonId)
        {
            case BUTTON_MODE:
            case BUTTON_COLOR_MINUS:
            case BUTTON_COLOR_PLUS:
                if(!player.getPersistentID().equals(owner))
                    return;
        }

        if(buttonId >= BUTTON_OWNER_COIN && buttonId <= BUTTON_OWNER_LARGE_BAG)
        {
            if(!player.getPersistentID().equals(owner))
                return;
        }
        else if(buttonId >= BUTTON_USER_COIN && buttonId <= BUTTON_USER_LARGE_BAG)
        {
            if(player.getPersistentID().equals(owner))
                return;
        }

        // Action
        switch (buttonId)
        {
            case BUTTON_MODE:
                onModeButtonPressed();
                return;

            case BUTTON_COLOR_MINUS:
                if(textColor > 0)
                    textColor--;
                else
                    textColor = 15;
                updateBlocks();
                scheduleUpdate();
                return;

            case BUTTON_COLOR_PLUS:
                if(textColor < 15)
                    textColor++;
                else
                    textColor = 0;
                updateBlocks();
                scheduleUpdate();
        }

        if(buttonId >= BUTTON_OWNER_COIN && buttonId <= BUTTON_OWNER_LARGE_BAG)
            withdraw(buttonId - BUTTON_OWNER_COIN, SLOT_COIN_OUTPUT, shiftPressed, true);
        else if(buttonId >= BUTTON_USER_COIN && buttonId <= BUTTON_USER_LARGE_BAG)
            withdraw(buttonId - BUTTON_USER_COIN, SLOT_COIN_OUTPUT, shiftPressed, false);
    }

    public void withdraw(int multiplier, int slot, boolean all, boolean fromOwner)
    {
        ItemCoin item = UniversalCoinsServer.proxy.coins[multiplier];
        int balance = fromOwner? ownerCoins : userCoins;
        int value = item.getValue();

        ItemStack stack = inventory[slot];
        if(stack != null && (stack.getItem() != item || stack.stackSize >= stack.getMaxStackSize()
            || balance < value))
        {
            updateWithdrawButtons(fromOwner);
            return;
        }

        if(!all)
        {
            balance -= value;
            if(stack != null) stack.stackSize++;
            else setInventorySlotContents(slot, new ItemStack(item));

            setCoins(balance, fromOwner);
        }
        else
        {
            if(stack == null)
            {
                int amount = balance / value;
                stack = new ItemStack(item);
                int maxStackSize = stack.getMaxStackSize();
                if (amount > maxStackSize)
                    amount = maxStackSize;

                balance -= value * amount;
                stack.stackSize = amount;

                inventory[slot] = stack;
                setCoins(balance, fromOwner);
            }
            else
            {
                int amount = Math.min(balance / value, stack.getMaxStackSize() - stack.stackSize);

                balance -= value * amount;
                stack.stackSize += amount;

                setCoins(balance, fromOwner);
            }
        }

        scheduleUpdate();
    }

    private int stateHashcode()
    {
        return Arrays.hashCode(new boolean[]{sellToUser, outOfCoins, outOfStock, outOfInventorySpace, buyButtonActive, sellButtonActive});
    }

    private void updateOperations()
    {
        int hashcode = stateHashcode();
        ItemStack trade = inventory[SLOT_TRADE];
        outOfCoins = !infinite && !sellToUser && ownerCoins < price;
        if(trade == null)
        {
            outOfStock = true;
            outOfInventorySpace = false;
            if(stateHashcode() != hashcode)
            {
                scheduleUpdate();
                updateBlocks();
            }
            return;
        }

        if(!infinite)
        {
            outOfStock = true;
            outOfInventorySpace = true;
            int foundItems = 0;
            for(int i = SLOT_STORAGE_FIST; i <= SLOT_STORAGE_LAST; i++)
            {
                ItemStack stack = inventory[i];
                if(stack == null)
                {
                    outOfInventorySpace = false;
                }
                else if(stack.getItem() == trade.getItem() && stack.getItemDamage() == trade.getItemDamage() && ItemStack.areItemStackTagsEqual(stack, trade))
                {
                    if(stack.stackSize < stack.getMaxStackSize())
                        outOfInventorySpace = false;
                    if(stack.stackSize > 0)
                        foundItems += stack.stackSize;
                }
            }

            if(foundItems >= trade.stackSize)
                outOfStock = false;
        }
        else
        {
            outOfStock = false;
            outOfInventorySpace = false;
        }

        sellButtonActive = !sellToUser && !outOfInventorySpace && !outOfCoins;
        buyButtonActive = sellToUser && !outOfStock;

        if(stateHashcode() != hashcode)
        {
            scheduleUpdate();
            updateBlocks();
        }
    }

    private void updateWithdrawButtons(boolean fromOwner)
    {
        if(fromOwner)
            updateWithdrawButtons(ownerCoins, SLOT_COIN_OUTPUT, buttonOwnerWithdraw);
        else
            updateWithdrawButtons(userCoins, SLOT_COIN_OUTPUT, buttonUserWithdraw);
    }

    public void onModeButtonPressed()
    {
        sellToUser = !sellToUser;
        updateBlocks();
    }

    public boolean isInUse(EntityPlayer player)
    {
        if(opener == null)
            return false;

        if(!opener.isEntityAlive() || !isUseableByPlayer(opener))
        {
            opener = null;
            return false;
        }

        return !opener.isEntityEqual(player);

    }

    public void onContainerClosed(EntityPlayer player)
    {
        if(player.isEntityEqual(opener))
            opener = null;
    }

    @Override
    public UUID getOwnerId()
    {
        return owner;
    }

    public void setOpener(EntityPlayer opener)
    {
        this.opener = opener;
        updateOperations();
    }
}
