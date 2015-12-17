package br.com.gamemods.universalcoinsserver.tile;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.datastore.DataBaseException;
import br.com.gamemods.universalcoinsserver.datastore.PlayerOperator;
import br.com.gamemods.universalcoinsserver.datastore.Transaction;
import br.com.gamemods.universalcoinsserver.item.ItemCoin;
import com.google.common.primitives.Ints;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class TileSlots extends TileTransactionMachine
{
    public static final int SLOT_CARD = 0;
    public static final int SLOT_COIN_INPUT = 1;
    public static final int SLOT_COIN_OUTPUT = 2;
    public static final int BUTTON_SPIN = 0;
    public static final int BUTTON_WITHDRAW = 1;
    public static final int BUTTON_CHECK_MATCH = 2;

    private ItemStack[] inventory = new ItemStack[3];
    public int userCoins;
    public int fee = 1;
    public int fourMatchPayout = 100;
    public int fiveMatchPayout = 10000;
    public int[] reelPos = { 0, 0, 0, 0, 0 };
    public int[] fakeReel;
    private int[] reelStops = { 0, 22, 44, 66, 88, 110, 132, 154, 176, 198 };
    private boolean unlockInventory = false;
    private long coolDown;
    private Random random = new Random();
    private boolean waitingCheck = false;

    @Override
    public void onButtonPressed(EntityPlayerMP player, int buttonId, boolean shiftPressed)
    {
        if(!player.equals(opener))
            return;

        switch (buttonId)
        {
            case BUTTON_WITHDRAW:
                try
                {
                    unlockInventory = true;
                    int before = userCoins;
                    try
                    {
                        userCoins = UniversalCoinsServerAPI.addCoinsToSlot(this, userCoins, SLOT_COIN_OUTPUT);
                    }
                    finally
                    {
                        unlockInventory = false;
                    }

                    if(before != userCoins)
                        worldObj.playSoundEffect(xCoord, yCoord, zCoord,
                                inventory[SLOT_COIN_OUTPUT].stackSize > 1?
                                        "universalcoins:take_coins":
                                        "universalcoins:take_coin"
                                , 1.0F, 1.0F);

                    try
                    {
                        Transaction transaction = new Transaction(this, Transaction.Operation.WITHDRAW_FROM_MACHINE,
                                new PlayerOperator(player),  new Transaction.MachineCoinSource(this, before, userCoins - before),
                                inventory[SLOT_COIN_OUTPUT].copy()
                        );

                        UniversalCoinsServer.cardDb.saveTransaction(transaction);
                    }
                    catch (Exception e)
                    {
                        UniversalCoinsServer.logger.error(e);
                    }
                }
                finally
                {
                    markDirty();
                }
                return;

            case BUTTON_SPIN:
                spin(false);
                return;
            case BUTTON_CHECK_MATCH:
                checkMatch();
        }
    }

    public void checkMatch()
    {
        if(!waitingCheck)
        {
            scheduleUpdate();
            return;
        }

        waitingCheck = false;

        int matchCount;
        for (int i = 0; i < reelStops.length; i++)
        {
            matchCount = 0;

            for (int j = 0; j < reelPos.length; j++)
                if (reelStops[i] == reelPos[j])
                    matchCount++;

            Transaction.Operation operation = null;
            int before = userCoins;
            if (matchCount == 5)
            {
                userCoins += fiveMatchPayout;
                worldObj.playSound(xCoord, yCoord, zCoord, "universalcoins:winner", 1.0F, 1.0F, true);
                operation = Transaction.Operation.SLOTS_WIN_5_MATCH;
            }
            if (matchCount == 4)
            {
                userCoins += fourMatchPayout;
                worldObj.playSound(xCoord, yCoord, zCoord, "universalcoins:winner", 1.0F, 1.0F, true);
                operation = Transaction.Operation.SLOTS_WIN_4_MATCH;
            }

            if(userCoins != before)
            {
                Transaction transaction = new Transaction(this, operation,
                        new PlayerOperator(opener),
                        new Transaction.MachineCoinSource(this, before, userCoins-before),
                        null);

                try
                {
                    UniversalCoinsServer.cardDb.saveTransaction(transaction);
                }
                catch (Exception e)
                {
                    UniversalCoinsServer.logger.error(e);
                }
            }
        }

        markDirty();
    }

    public void spin(boolean ignoreCoolDown)
    {
        if(userCoins < fee || !ignoreCoolDown && System.currentTimeMillis() <= coolDown)
        {
            fakeReel = reelPos.clone();
            LinkedList<Integer> available = null;
            Map<Integer, Integer> count = new HashMap<>(reelPos.length);
            for(int i=0; i< reelPos.length; i++)
            {
                int val = reelPos[i];
                if(!count.containsKey(val))
                    count.put(val, 1);
                else
                {
                    int matches = count.get(val) + 1;
                    count.put(val, matches);
                    if(matches >= 4)
                    {
                        if(fakeReel == null || available == null)
                        {
                            fakeReel = reelPos.clone();
                            available = new LinkedList<>(Ints.asList(reelStops));
                            Collections.shuffle(available);
                        }

                        available.remove(val);
                        fakeReel[i] = available.remove();
                    }
                }
            }
            scheduleUpdate();
            return;
        }

        userCoins -= fee;
        coolDown = System.currentTimeMillis() + 1500L;

        fakeReel = null;
        for (int i = 0; i < reelPos.length; i++)
        {
            int rnd = random.nextInt(reelStops.length);
            reelPos[i] = reelStops[rnd];
        }
        waitingCheck = true;
        markDirty();

        Transaction transaction = new Transaction(this, Transaction.Operation.BUY_FROM_MACHINE,
                new PlayerOperator(opener),
                new Transaction.MachineCoinSource(this, userCoins+fee, -fee),
                null);

        try
        {
            UniversalCoinsServer.cardDb.saveTransaction(transaction);
        }
        catch (Exception e)
        {
            UniversalCoinsServer.logger.error(e);
        }
    }

    @Override
    public void setOpener(EntityPlayer opener)
    {
        super.setOpener(opener);
        if(opener == null)
            fakeReel = null;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inventory.length; i++)
        {
            ItemStack stack = inventory[i];
            if(stack == null) stack = new ItemStack(Blocks.air, 0);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("Slot", (byte) i);
            stack.writeToNBT(tag);
            itemList.appendTag(tag);
        }

        tagCompound.setTag("Inventory", itemList);
        tagCompound.setInteger("coinSum", userCoins);
        tagCompound.setInteger("spinFee", fee);
        tagCompound.setInteger("fourMatchPayout", fourMatchPayout);
        tagCompound.setInteger("fiveMatchPayout", fiveMatchPayout);
        tagCompound.setBoolean("cardAvailable", false);
        tagCompound.setString("customName", "");
        tagCompound.setBoolean("inUse", opener != null);
        for(int i = 0; i < reelPos.length; i++)
            tagCompound.setInteger("reelPos"+i, reelPos[i]);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
        Arrays.fill(inventory, null);
        NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            byte slot = tag.getByte("Slot");
            if (slot >= 0 && slot < inventory.length)
            {
                ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
                if(stack != null && stack.stackSize > 0)
                    inventory[slot] = stack;
            }
        }

        userCoins = tagCompound.getInteger("coinSum");
        fee = tagCompound.getInteger("spinFee");
        fourMatchPayout = tagCompound.getInteger("fourMatchPayout");
        fiveMatchPayout = tagCompound.getInteger("fiveMatchPayout");
        for(int i= 0; i < reelPos.length; i++)
            reelPos[i] = tagCompound.getInteger("reelPos"+i);
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        if(fakeReel != null)
            for(int i = 0; i < fakeReel.length; i++)
                nbt.setInteger("reelPos"+i, fakeReel[i]);

        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
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
                inventory[slot] = null;
            else
            {
                stack = stack.splitStack(size);
                if (stack.stackSize == 0)
                    inventory[slot] = null;
            }
        }
        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot)
    {
        setOpener(opener);
        return getStackInSlot(slot);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        if(stack != null && stack.stackSize <= 0)
            stack = null;

        inventory[slot] = stack;
        scheduleUpdate();

        if(stack == null)
            return;

        Item item = stack.getItem();
        switch (slot)
        {
            case SLOT_COIN_INPUT:
            {
                if(item instanceof ItemCoin)
                {
                    int itemValue = ((ItemCoin) item).getValue();
                    int depositAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - userCoins) / itemValue);
                    int depositValue = depositAmount * itemValue;

                    Transaction.CoinSource coinSource = new Transaction.MachineCoinSource(this, userCoins, depositValue);
                    ItemStack product = stack.copy();
                    product.stackSize = depositAmount;
                    Transaction transaction = new Transaction(this, Transaction.Operation.DEPOSIT_TO_MACHINE,
                            new PlayerOperator(opener), coinSource, product);

                    try
                    {
                        UniversalCoinsServer.cardDb.saveTransaction(transaction);
                    }
                    catch (DataBaseException e)
                    {
                        UniversalCoinsServer.logger.error(e);
                    }

                    userCoins += depositValue;
                    inventory[slot] = null;
                    markDirty();

                    worldObj.playSoundEffect(xCoord, yCoord, zCoord, "universalcoins:insert_coin", 1f, 1f);
                }
                break;
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
    public String getInventoryName()
    {
        return UniversalCoinsServer.proxy.blockSlots.getUnlocalizedName();
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return true;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        boolean result = worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
        if(!result)
            player.closeScreen();
        return result;
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
        return unlockInventory && slot == SLOT_COIN_OUTPUT;
    }
}
