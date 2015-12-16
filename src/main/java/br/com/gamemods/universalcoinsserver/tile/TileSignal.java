package br.com.gamemods.universalcoinsserver.tile;

import br.com.gamemods.universalcoinsserver.GuiHandler;
import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.api.ScanResult;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.blocks.PlayerOwned;
import br.com.gamemods.universalcoinsserver.datastore.PlayerOperator;
import br.com.gamemods.universalcoinsserver.datastore.Transaction;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class TileSignal extends TileTransactionMachine implements PlayerOwned, IInventory
{
    public static final int SLOT_COIN_OUTPUT = 0;
    public static final int BUTTON_WITHDRAW = 0;
    public static final int BUTTON_DURATION_MINUS = 1;
    public static final int BUTTON_DURATION_PLUS = 2;
    public static final int BUTTON_FEE_MINUS = 3;
    public static final int BUTTON_FEE_PLUS = 4;

    private ItemStack coinOutput = null;
    public boolean providingPower;
    public UUID owner;
    public String ownerName;
    public int coins;
    public int fee = 1;
    public int duration = 1;
    public int secondsLeft;
    public int ticks;
    private boolean unlockOutputSlot;

    public int stateHashcode()
    {
        return Arrays.hashCode(new int[]{providingPower?1:0,coins,fee,duration,secondsLeft});
    }

    public void validateFields()
    {
        int hash = stateHashcode();
        if(coins < 0) coins = 0;
        if(fee < 0) fee = 1;
        if(duration < 0) duration = 1;
        if(secondsLeft < 0) secondsLeft = 0;

        if(coinOutput != null && coinOutput.stackSize <= 0)
        {
            coinOutput = null;
            markDirty();
        }
        else if(hash != stateHashcode())
            markDirty();
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if(ticks > 0)
        {
            ticks--;
            int last = secondsLeft;
            secondsLeft = (ticks+20) / 20;
            if(last != secondsLeft)
                markDirty();
            else
                super.markDirty();

            if(ticks == 0)
            {
                secondsLeft = 0;
                providingPower = false;
                markDirty();
                updateNeighbors();
            }
        }
    }

    public void onLeftClick(EntityPlayer player)
    {
        if(player.isSneaking())
        {
            if(player.getPersistentID().equals(owner))
                player.openGui(UniversalCoinsServer.instance, GuiHandler.GUI_SIGNAL, worldObj, xCoord, yCoord, zCoord);
            return;
        }

        ScanResult scanResult = UniversalCoinsServerAPI.scanCoins(player.inventory);
        if(scanResult.getCoins() < fee)
        {
            player.addChatMessage(new ChatComponentTranslation("signal.message.notenough"));
            return;
        }

        PlayerOperator operator = new PlayerOperator(player);
        Transaction transaction = new Transaction(this, Transaction.Operation.BUY_FROM_MACHINE, duration,
                operator,
                new Transaction.InventoryCoinSource(operator, scanResult.getCoins(), -fee),
                new Transaction.MachineCoinSource(this, coins, fee));

        try
        {
            UniversalCoinsServer.cardDb.saveTransaction(transaction);
        }
        catch (Exception e)
        {
            UniversalCoinsServer.logger.error(e);
            Collections.fill(player.openContainer.inventoryItemStacks, null);
            player.openContainer.detectAndSendChanges();
            return;
        }

        UniversalCoinsServerAPI.takeCoinsReturningChange(scanResult, fee, player);
        player.addChatComponentMessage(new ChatComponentTranslation("signal.message.activated"));
        Collections.fill(player.openContainer.inventoryItemStacks, null);
        player.openContainer.detectAndSendChanges();

        coins += fee;

        activate(duration * 20);
    }

    public void activate(int ticks)
    {
        providingPower = true;
        this.ticks += ticks;
        updateNeighbors();
    }

    @Override
    public void onButtonPressed(EntityPlayerMP player, int buttonId, boolean shiftPressed)
    {
        if(!player.getPersistentID().equals(owner))
            return;

        switch (buttonId)
        {
            case BUTTON_DURATION_MINUS:
                if (shiftPressed)
                    duration -= 10;
                else
                    duration--;
                if(duration <= 0)
                    duration = 1;
                markDirty();
                return;
            case BUTTON_DURATION_PLUS:
                if(shiftPressed)
                    duration += 10;
                else
                    duration++;
                markDirty();
                return;
            case BUTTON_FEE_MINUS:
                if(shiftPressed)
                    fee -= 10;
                else
                    fee--;
                if(fee < 0)
                    fee = 0;
                markDirty();
                return;
            case BUTTON_FEE_PLUS:
                if(shiftPressed)
                    fee += 10;
                else
                    fee++;
                markDirty();
                return;
            case BUTTON_WITHDRAW:
                try
                {
                    unlockOutputSlot = true;
                    int before = coins;
                    coins = UniversalCoinsServerAPI.addCoinsAnywhere(this, coins);
                    if(before == coins)
                        return;

                    markDirty();

                    Transaction transaction = new Transaction(this, Transaction.Operation.WITHDRAW_FROM_MACHINE, duration,
                            new PlayerOperator(player), null, new Transaction.MachineCoinSource(this, before, coins-before));

                    try
                    {
                        UniversalCoinsServer.cardDb.saveTransaction(transaction);
                    }
                    catch (Exception e)
                    {
                        UniversalCoinsServer.logger.error(e);
                    }
                }
                finally
                {
                    unlockOutputSlot = false;
                }

        }
    }

    @Override
    public void markDirty()
    {
        scheduleUpdate();
        super.markDirty();
    }

    @Override
    public UUID getOwnerId()
    {
        return owner;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if(ownerName != null) compound.setString("OwnerName", ownerName);
        if(owner != null) compound.setString("blockOwner", owner.toString());

        NBTTagList itemList = new NBTTagList();
        ItemStack stack = coinOutput == null? new ItemStack(Blocks.air,0) : coinOutput;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Slot", (byte) SLOT_COIN_OUTPUT);
        stack.writeToNBT(tag);
        itemList.appendTag(tag);

        compound.setTag("Inventory", itemList);
        compound.setInteger("coinSum", coins);
        compound.setInteger("fee", fee);
        compound.setInteger("duration", duration);
        compound.setInteger("secondsLeft", secondsLeft);
        compound.setInteger("ticks", ticks);
        //compound.setString("customName", "");
        compound.setBoolean("canProvidePower", providingPower);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        ticks = compound.getInteger("ticks");
        ownerName = compound.getString("OwnerName");
        String str = compound.getString("blockOwner");
        if(str.isEmpty())
            owner = null;
        else
            try
            {
                owner = UUID.fromString(str);
            }
            catch (Exception e)
            {
                owner = null;
            }

        NBTTagList tagList = compound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
        coinOutput = null;
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            byte slot = tag.getByte("Slot");
            if (slot == 1)
                coinOutput = ItemStack.loadItemStackFromNBT(tag);
        }

        coins = compound.getInteger("coinSum");
        fee = compound.getInteger("fee");
        duration = compound.getInteger("duration");
        secondsLeft = compound.getInteger("secondsLeft");
        providingPower = compound.getBoolean("canProvidePower");
        validateFields();
    }

    @Override
    public int getSizeInventory()
    {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        if(slot != 0) throw new ArrayIndexOutOfBoundsException("slot != 0: "+slot);
        return coinOutput;
    }

    @Override
    public ItemStack decrStackSize(int slot, int size)
    {
        if(coinOutput == null)
            return null;

        if (coinOutput.stackSize <= size)
        {
            ItemStack stack = coinOutput;
            coinOutput = null;
            return stack;
        }

        ItemStack stack = coinOutput.splitStack(size);
        if (stack.stackSize <= 0)
            coinOutput = null;

        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot)
    {
        return getStackInSlot(slot);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        if(slot != 0) throw new ArrayIndexOutOfBoundsException(slot);
        if(unlockOutputSlot || stack == null)
            coinOutput = stack;
        else
            throw new UnsupportedOperationException();
        markDirty();
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
    {}

    @Override
    public void closeInventory()
    {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack)
    {
        return unlockOutputSlot;
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.func_148857_g());
    }
}
