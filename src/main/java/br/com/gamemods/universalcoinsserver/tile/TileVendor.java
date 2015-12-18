package br.com.gamemods.universalcoinsserver.tile;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.datastore.*;
import br.com.gamemods.universalcoinsserver.item.ItemCoin;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;

public class TileVendor extends TileOwned
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
    public static final int BUTTON_OWNER_COIN = 3;
    public static final int BUTTON_OWNER_LARGE_BAG = 7;
    public static final int BUTTON_SELL = 8;
    public static final int BUTTON_BUY = 9;
    public static final int BUTTON_USER_COIN = 10;
    public static final int BUTTON_USER_LARGE_BAG = 14;
    public static final int BUTTON_COLOR_MINUS = 15;
    public static final int BUTTON_COLOR_PLUS = 16;

    private ItemStack[] inventory = new ItemStack[17];

    public int ownerCoins;
    public int userCoins;
    public int price;
    public boolean infinite;
    public boolean sellToUser;
    public byte textColor;
    public EntityPlayer opener;
    public String icon = "";
    private boolean[] buttonOwnerWithdraw = new boolean[5];
    private boolean[] buttonUserWithdraw = new boolean[5];
    private boolean outOfStock, outOfInventorySpace, buyButtonActive, sellButtonActive, outOfCoins;
    private AccountAddress ownerCard;
    private AccountAddress userCard;

    public void validateFields()
    {
        if(ownerCoins < 0) ownerCoins = 0;
        if(userCoins < 0) userCoins = 0;
        if(price < 0) price = 0;
        updateWithdrawButtons(true);
        updateWithdrawButtons(false);
        updateCards();
        updateOperations();
    }

    public void updateCards()
    {
        AccountAddress ownerBefore = ownerCard, userBefore = userCard;
        int price = sellToUser? this.price : -this.price;
        ownerCard = UniversalCoinsServerAPI.isCardValidForTransaction(inventory[SLOT_OWNER_CARD], owner, -price);
        userCard = UniversalCoinsServerAPI.isCardValidForTransaction(inventory[SLOT_USER_CARD], opener, price);

        if(!Objects.equals(ownerBefore, ownerCard) || !Objects.equals(userBefore, userCard))
            markDirty();
    }

    @Override
    public void writeToStackNBT(NBTTagCompound compound)
    {
        String[] copy = new String[]{
                "Inventory",
                "UserCoinSum",
                //"OwnerName",
                "Mode",
                "CoinSum",
                "BlockIcon",
                "ItemPrice",
                "TextColor"
                //"BlockOwner",
                //"MachineId"
        };

        NBTTagCompound base = new NBTTagCompound();
        writeToNBT(base);

        for(String key: copy)
        {
            NBTBase tag = base.getTag(key);
            if(tag != null)
                compound.setTag(key, tag);
        }
    }

    @Override
    public void readFromStackNBT(NBTTagCompound compound)
    {
        readInventoryFromNBT(compound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND));
        userCoins = compound.getInteger("UserCoinSum");
        sellToUser = compound.getBoolean("Mode");
        ownerCoins = compound.getInteger("CoinSum");
        icon = compound.getString("BlockIcon");
        price = compound.getInteger("ItemPrice");
        textColor = (byte) compound.getInteger("TextColor");
        validateFields();
    }

    public void readInventoryFromNBT(NBTTagList tagList)
    {
        Arrays.fill(inventory, null);
        if(tagList == null)
            return;

        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            byte slot = tag.getByte("Slot");
            if (slot >= 0 && slot < inventory.length)
            {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
            }
        }
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
        compound.setString("BlockIcon", icon);
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
        readInventoryFromNBT(tagList);

        ownerCoins = compound.getInteger("CoinSum");
        userCoins = compound.getInteger("UserCoinSum");
        price = compound.getInteger("ItemPrice");
        infinite = compound.getBoolean("Infinite");
        sellToUser = compound.getBoolean("Mode");
        textColor = (byte) compound.getInteger("TextColor");
        icon = compound.getString("BlockIcon");

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

        if(slot == SLOT_USER_CARD || slot == SLOT_OWNER_CARD)
            updateCards();

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

        switch (slot)
        {
            case SLOT_OWNER_CARD:
            case SLOT_USER_CARD:
                updateCards();
                return;
        }

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
                        Transaction.CoinSource coinSource = new Transaction.MachineCoinSource(this, current, depositValue);
                        ItemStack product = stack.copy();
                        product.stackSize = depositAmount;
                        Transaction transaction = new Transaction(this, Transaction.Operation.DEPOSIT_TO_MACHINE,
                                depositAmount,
                                owner?coinSource:null,
                                owner?null:coinSource,
                                product);

                        try
                        {
                            UniversalCoinsServer.cardDb.saveTransaction(transaction);
                        }
                        catch (DataBaseException e)
                        {
                            UniversalCoinsServer.logger.error(e);
                        }

                        current += depositValue;

                        worldObj.playSoundEffect(xCoord, yCoord, zCoord, "universalcoins:insert_coin", 1f, 1f);
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
        if(ownerCoins == this.ownerCoins)
            return;

        this.ownerCoins = ownerCoins;
        boolean coins = outOfCoins;
        updateOperations();
        if(coins != outOfCoins)
            updateBlocks();
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

    @Override
    public void markDirty()
    {
        updateOperations();
        updateBlocks();
        scheduleUpdate();
        super.markDirty();
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
        Operator operator;
        if(cardSlot == SLOT_USER_CARD)
        {
            if(opener != null) operator = new PlayerOperator(opener);
            else operator = new MachineOperator(this);
        }
        else if(cardSlot == SLOT_OWNER_CARD)
        {
            if(opener != null && opener.getPersistentID().equals(owner)) operator = new PlayerOperator(opener);
            else operator = new MachineOperator(this);
        }
        else
            return false;

        return depositToEnderCard(cardSlot, depositAmount, operator, null);
    }

    private boolean depositToEnderCard(int cardSlot, int depositAmount, Operator operator, ItemStack product)
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
            if(UniversalCoinsServer.cardDb.canDeposit(account, depositAmount) < 0)
                return false;

            Transaction.CoinSource userSource = null;
            if(operator instanceof PlayerOperator)
            {
                int balance =  UniversalCoinsServerAPI.scanCoins(opener.inventory).getCoins();
                userSource = new Transaction.InventoryCoinSource(operator, balance+depositAmount, -depositAmount);
            }

            Transaction transaction = new Transaction(this, Transaction.Operation.DEPOSIT_TO_ACCOUNT_FROM_MACHINE,
                    operator, userSource, new Transaction.CardCoinSource(stack, depositAmount), product);

            UniversalCoinsServer.cardDb.depositToAccount(account, depositAmount, transaction);
            //worldObj.playSoundEffect(xCoord, yCoord, zCoord, "universalcoins:insert_coin", 1f, 1f);
            worldObj.playSoundEffect(xCoord, yCoord, zCoord, "mob.endermen.portal", 0.15f, 2f);
            return true;
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

    public void updateBlocks()
    {
        if(worldObj == null)
            return;

        @Nullable
        ItemStack trade = inventory[SLOT_TRADE];
        IChatComponent signText[] = new IChatComponent[5];
        EnumChatFormatting[] styles = EnumChatFormatting.values();
        ChatStyle color = new ChatStyle().setColor(styles[textColor]);

        // Selling / Buying
        signText[0] = new ChatComponentTranslation(sellToUser ? "sign.sellmode.sell" : "sign.sellmode.buy").setChatStyle(color);

        // add out of stock notification if not infinite and no stock found
        if (!infinite && sellToUser && outOfStock)
            signText[0] = new ChatComponentTranslation("sign.warning.stock").setChatStyle(color);

        // add out of coins notification if buying and no funds available
        if (!sellToUser && outOfCoins && !infinite)
            signText[0] = new ChatComponentTranslation("sign.warning.coins").setChatStyle(color);

        // add inventory full notification
        if (!sellToUser && outOfInventorySpace)
            signText[0] = new ChatComponentTranslation("sign.warning.inventoryfull").setChatStyle(color);

        if(trade != null)
        {
            if (trade.stackSize > 1)
                signText[1] = new ChatComponentText(trade.stackSize+"x ").setChatStyle(color)
                        .appendSibling(new ChatComponentTranslation(trade.getUnlocalizedName()+".name"));
            else
                signText[1] = new ChatComponentTranslation(trade.getUnlocalizedName()+".name").setChatStyle(color);

            Item item = trade.getItem();
            if (trade.isItemEnchanted() || (item == Items.enchanted_book && trade.hasTagCompound()))
            {
                NBTTagList tagList = trade.isItemEnchanted()? trade.getEnchantmentTagList() :
                        trade.stackTagCompound.getTagList("StoredEnchantments", Constants.NBT.TAG_COMPOUND);

                IChatComponent base = null;
                for (int i = 0; i < tagList.tagCount(); i++)
                {
                    NBTTagCompound tag = tagList.getCompoundTagAt(i);
                    Enchantment enchantment = Enchantment.enchantmentsList[tag.getInteger("id")];

                    IChatComponent msg = new ChatComponentTranslation(enchantment.getName())
                            .appendText(" ").appendSibling(new ChatComponentTranslation("enchantment.level."+tag.getInteger("lvl")));

                    if(base == null)
                        base = msg.setChatStyle(color);
                    else
                        base.appendText(", ").appendSibling(msg);
                }

                signText[2] = base;
            }

            if (item == UniversalCoinsServer.proxy.itemPackage)
            {
                IChatComponent base = null;
                if (trade.stackTagCompound != null)
                {
                    NBTTagList tagList = trade.stackTagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
                    for (int i = 0; i < tagList.tagCount(); i++)
                    {
                        NBTTagCompound tag = tagList.getCompoundTagAt(i);
                        //byte slot = tag.getByte("Slot");
                        ItemStack stack = ItemStack.loadItemStackFromNBT(tag);

                        IChatComponent msg = new ChatComponentText(stack.stackSize+":")
                                .appendSibling(new ChatComponentTranslation(stack.getUnlocalizedName()+".name"));

                        if(base == null)
                            base = msg.setChatStyle(color);
                        else
                            base.appendText(" ").appendSibling(msg);
                    }
                }
                signText[2] = base;
            }
        }

        signText[3] = new ChatComponentTranslation("sign.price").setChatStyle(color).appendText(Integer.toString(price));

        // find and update all signs
        updateSign(signText);
    }

    protected void updateSign(IChatComponent[] lines, TileEntity te)
    {
        if (te instanceof TileAdvSign)
        {
            TileAdvSign tile = (TileAdvSign) te;
            tile.setLines(lines);
            tile.scheduleUpdate();
            tile.markDirty();
        }
    }

    protected void updateSign(IChatComponent[] lines)
    {
        updateSign(lines, worldObj.getTileEntity(xCoord + 1, yCoord - 1, zCoord));
        updateSign(lines, worldObj.getTileEntity(xCoord - 1, yCoord - 1, zCoord));
        updateSign(lines, worldObj.getTileEntity(xCoord, yCoord - 1, zCoord - 1));
        updateSign(lines, worldObj.getTileEntity(xCoord, yCoord - 1, zCoord + 1));
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
                break;
            case BUTTON_BUY:
            case BUTTON_SELL:
                if(player.getPersistentID().equals(owner))
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

        if(buttonId == BUTTON_BUY && !buyButtonActive
            || buttonId == BUTTON_SELL && !sellButtonActive)
        {
            scheduleUpdate();
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
                markDirty();
                return;

            case BUTTON_COLOR_PLUS:
                if(textColor < 15)
                    textColor++;
                else
                    textColor = 0;
                markDirty();
                return;

            case BUTTON_BUY:
                buy(shiftPressed);
                return;

            case BUTTON_SELL:
                sell(shiftPressed);
                return;
        }

        if(buttonId >= BUTTON_OWNER_COIN && buttonId <= BUTTON_OWNER_LARGE_BAG)
            withdraw(buttonId - BUTTON_OWNER_COIN, SLOT_COIN_OUTPUT, shiftPressed, true);
        else if(buttonId >= BUTTON_USER_COIN && buttonId <= BUTTON_USER_LARGE_BAG)
            withdraw(buttonId - BUTTON_USER_COIN, SLOT_COIN_OUTPUT, shiftPressed, false);
    }

    public void sell(boolean all)
    {
        updateCards();
        Transaction.Operation operation = Transaction.Operation.SELL_TO_MACHINE;
        Transaction.CoinSource userSource;
        Transaction.CoinSource ownerSource;

        ItemStack trade = inventory[SLOT_TRADE];
        ItemStack input = inventory[SLOT_SELL];
        if(!UniversalCoinsServerAPI.matches(trade, input)
            || input.stackSize < trade.stackSize
            || (!infinite && ((ownerCard == null && ownerCoins < price) || outOfInventorySpace)))
        {
            sellButtonActive = false;
            scheduleUpdate();
            return;
        }

        List<Integer> spaces = new ArrayList<>(SLOT_STORAGE_LAST - SLOT_STORAGE_FIST);
        int storageSpace = 0;
        int maxStackSize = trade.getMaxStackSize();
        if(!infinite)
        {
            for(int i = SLOT_STORAGE_FIST; i <= SLOT_STORAGE_LAST; i++)
            {
                ItemStack stack = inventory[i];
                if(stack == null)
                {
                    storageSpace += maxStackSize;
                    spaces.add(i);
                }
                else if(UniversalCoinsServerAPI.matches(stack, trade) && stack.stackSize < maxStackSize)
                {
                    storageSpace += maxStackSize - stack.stackSize;
                    spaces.add(i);
                }
            }

            if(storageSpace < trade.stackSize)
            {
                sellButtonActive = false;
                outOfInventorySpace = true;
                scheduleUpdate();
                return;
            }
        }

        if(userCard == null && ((long)userCoins) + ((long)price) > Integer.MAX_VALUE)
        {
            sellButtonActive = false;
            scheduleUpdate();
            return;
        }

        int ownerBalance;
        int userBalance;
        AccountAddress ownerCard = this.ownerCard;
        AccountAddress userCard = this.userCard;
        if(ownerCard != null)
            try
            {
                ownerBalance = UniversalCoinsServer.cardDb.getAccountBalance(ownerCard);
            }
            catch (DataBaseException e)
            {
                e.printStackTrace();
                ownerCard = null;
                ownerBalance = ownerCoins;
            }
        else
            ownerBalance = ownerCoins;

        if(userCard != null)
            try
            {
                userBalance = UniversalCoinsServer.cardDb.getAccountBalance(userCard);
            }
            catch (DataBaseException e)
            {
                e.printStackTrace();
                userCard = null;
                userBalance = userCoins;
            }
        else
            userBalance = userCoins;

        int quantity = 1;
        if(all && trade.stackSize * 2 <= maxStackSize)
        {
            int maxQuantity = input.stackSize / trade.stackSize;
            if(!infinite) maxQuantity = Math.min(maxQuantity, storageSpace / trade.stackSize);

            if(maxQuantity <= 0)
            {
                sellButtonActive = false;
                scheduleUpdate();
                return;
            }
            else if(maxQuantity > 1)
            {
                if(infinite || price * maxQuantity <= ownerBalance)
                    quantity = maxQuantity;
                else
                    quantity = ownerBalance / price;
            }

            if(quantity <= 0)
                return;
            else if(quantity > 1)
            {
                for(; quantity>1; quantity--)
                {
                    if(((long)userBalance) + (((long)price)*quantity) <= Integer.MAX_VALUE)
                        break;
                }
            }
        }

        try
        {
            userSource = userCard != null ? new Transaction.CardCoinSource(userCard, price * quantity) : new Transaction.MachineCoinSource(this, userCoins, price * quantity);
            ownerSource = infinite ? null : ownerCard != null ? new Transaction.CardCoinSource(ownerCard, -(price * quantity)) : new Transaction.MachineCoinSource(this, ownerCoins, -(price * quantity));
        }
        catch (DataBaseException e)
        {
            e.printStackTrace();
            return;
        }

        ItemStack product = input.copy();
        product.stackSize = trade.stackSize * quantity;

        Transaction transaction = new Transaction(this, operation, quantity, userSource, ownerSource, product);
        try
        {
            UniversalCoinsServer.cardDb.processTrade(transaction);
        }
        catch (DataBaseException e)
        {
            UniversalCoinsServer.logger.error("Failed to save transaction "+transaction, e);
            return;
        }

        input.stackSize -= product.stackSize;
        if(input.stackSize <= 0)
            inventory[SLOT_SELL] = null;

        if(userCard == null)
            userCoins += price * quantity;

        if(!infinite)
        {
            if(ownerCard == null)
                ownerCoins -= price * quantity;

            storageSpace = product.stackSize;
            for(int space: spaces)
            {
                ItemStack stack = inventory[space];
                if(stack == null)
                {
                    if(storageSpace >= maxStackSize)
                    {
                        storageSpace -= maxStackSize;
                        stack = trade.copy();
                        stack.stackSize = maxStackSize;
                        inventory[space] = stack;
                        if(storageSpace == 0)
                            break;
                    }
                    else
                    {
                        stack = trade.copy();
                        stack.stackSize = storageSpace;
                        //storageSpace = 0;
                        inventory[space] = stack;
                        break;
                    }
                }
                else
                {
                    int available = maxStackSize - stack.stackSize;
                    if(storageSpace >= available)
                    {
                        stack.stackSize += available;
                        storageSpace -= available;
                        if(storageSpace == 0)
                            break;
                    }
                    else
                    {
                        stack.stackSize += storageSpace;
                        //storageSpace = 0;
                        break;
                    }
                }
            }
        }

        worldObj.playSoundEffect(xCoord, yCoord, zCoord, "universalcoins:sold", 1f, 1f);

        validateFields();
        scheduleUpdate();
    }

    public Operator getOperator()
    {
        if(opener != null)
            return new PlayerOperator(opener);
        return new MachineOperator(this);
    }

    public void buy(boolean all)
    {
        updateCards();

        Transaction.Operation operation = Transaction.Operation.BUY_FROM_MACHINE;
        Transaction.CoinSource userSource;
        Transaction.CoinSource ownerSource;

        ItemStack trade = inventory[SLOT_TRADE];
        if(trade == null || trade.stackSize <= 0)
        {
            buyButtonActive = false;
            markDirty();
            return;
        }

        if(userCard == null && userCoins < price)
        {
            buyButtonActive = false;
            markDirty();
            return;
        }

        ItemStack output = inventory[SLOT_OUTPUT];
        if(output != null)
        {
            if (UniversalCoinsServerAPI.matches(output, trade) || output.stackSize + trade.stackSize > output.getMaxStackSize())
            {
                buyButtonActive = false;
                markDirty();
                return;
            }
        }

        int found = 0;
        List<ItemStack> subtraction = new ArrayList<>(SLOT_STORAGE_LAST - SLOT_STORAGE_FIST);
        if(!infinite)
        {
            for(int i = SLOT_STORAGE_FIST; i <= SLOT_STORAGE_LAST; i++)
            {
                ItemStack stack = inventory[i];
                if(UniversalCoinsServerAPI.matches(stack, trade))
                {
                    subtraction.add(stack);
                    found += stack.stackSize;
                }
            }

            if(found < trade.stackSize)
            {
                updateOperations();
                return;
            }
        }

        int ownerBalance;
        int userBalance;
        AccountAddress ownerCard = this.ownerCard;
        AccountAddress userCard = this.userCard;
        if(ownerCard != null)
            try
            {
                ownerBalance = UniversalCoinsServer.cardDb.getAccountBalance(ownerCard);
            }
            catch (DataBaseException e)
            {
                e.printStackTrace();
                ownerCard = null;
                ownerBalance = ownerCoins;
            }
        else
            ownerBalance = ownerCoins;

        if(userCard != null)
            try
            {
                userBalance = UniversalCoinsServer.cardDb.getAccountBalance(userCard);
            }
            catch (DataBaseException e)
            {
                e.printStackTrace();
                userCard = null;
                userBalance = userCoins;
            }
        else
            userBalance = userCoins;


        int quantity = 1;
        if(all && trade.stackSize * 2 <= trade.getMaxStackSize())
        {
            if(output == null)
            {
                if(trade.getMaxStackSize() * price / trade.stackSize <= userBalance)
                {
                    // buy as many as will fit in a stack
                    quantity = trade.getMaxStackSize() / trade.stackSize;
                }
                else
                {
                    // buy as many as i have coins for.
                    quantity = userBalance / price;
                }
            }
            else
            {
                if((output.getMaxStackSize() - output.stackSize) * price <= userBalance)
                {
                    // buy as much as i can fit in a stack since we have enough
                    // coins
                    quantity = (trade.getMaxStackSize() - output.stackSize) / output.stackSize;
                }
                else
                {
                    // buy as many as possible with available coins.
                    quantity = userBalance / price;
                }
            }

            if(quantity <= 0)
                return;
        }

        try
        {
            userSource = userCard != null? new Transaction.CardCoinSource(userCard, -(price*quantity)) : new Transaction.MachineCoinSource(this, userBalance, -(price*quantity));
            ownerSource = infinite? null : ownerCard != null? new Transaction.CardCoinSource(ownerCard, price*quantity) : new Transaction.MachineCoinSource(this, ownerBalance, price*quantity);
        }
        catch (DataBaseException e)
        {
            e.printStackTrace();
            return;
        }

        ItemStack product;
        if(output != null)
        {
            product = output.copy();
            product.stackSize = trade.stackSize * quantity;
        }
        else
        {
            product = trade.copy();
            product.stackSize *= quantity;
        }

        Transaction transaction = new Transaction(this, operation, quantity, userSource, ownerSource, product);
        try
        {
            UniversalCoinsServer.cardDb.processTrade(transaction);
        }
        catch (DataBaseException e)
        {
            UniversalCoinsServer.logger.error("Failed to save transaction "+transaction, e);
            return;
        }

        if(!infinite)
        {
            found = trade.stackSize * quantity;

            for(ItemStack stack: subtraction)
            {
                if(stack.stackSize >= found)
                {
                    stack.stackSize -= found;
                    break;
                }

                found -= stack.stackSize;
                stack.stackSize = 0;
                if(found == 0)
                    break;
            }

            for(int i = SLOT_STORAGE_FIST; i <= SLOT_STORAGE_LAST; i++)
            {
                ItemStack stack = inventory[i];
                if (stack != null && stack.stackSize <= 0)
                    inventory[i] = null;
            }
        }

        if(output != null)
        {
            output.stackSize += product.stackSize;
            if(userCard == null)
                userCoins-= price * quantity;
            if(!infinite && ownerCard == null)
                ownerCoins += price * quantity;
        }
        else
        {
            inventory[SLOT_OUTPUT] = product;
            if(userCard == null)
                userCoins -= price * quantity;
            if(!infinite && ownerCard == null)
                ownerCoins += price * quantity;
        }

        worldObj.playSoundEffect(xCoord, yCoord, zCoord, "universalcoins:sold", 1f, 1f);

        validateFields();
        markDirty();
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
            Transaction.CoinSource coinSource = new Transaction.MachineCoinSource(this, balance, -value);
            Transaction transaction = new Transaction(this, Transaction.Operation.WITHDRAW_FROM_MACHINE, 1,
                    fromOwner?null:coinSource,
                    fromOwner?coinSource:null,
                    new ItemStack(item, 1));
            try
            {
                UniversalCoinsServer.cardDb.saveTransaction(transaction);
            }
            catch (DataBaseException e)
            {
                UniversalCoinsServer.logger.error(e);
            }

            balance -= value;
            if(stack != null) stack.stackSize++;
            else setInventorySlotContents(slot, new ItemStack(item));

            setCoins(balance, fromOwner);

            worldObj.playSoundEffect(xCoord, yCoord, zCoord,"universalcoins:take_coin", 1.0F, 1.0F);
        }
        else
        {
            int totalValue;
            int balanceBefore = balance;
            int amount;
            if(stack == null)
            {
                amount = balance / value;
                stack = new ItemStack(item);
                int maxStackSize = stack.getMaxStackSize();
                if (amount > maxStackSize)
                    amount = maxStackSize;

                totalValue = value * amount;
                balance -= totalValue;
                stack.stackSize = amount;

                inventory[slot] = stack;
                setCoins(balance, fromOwner);
            }
            else
            {
                amount = Math.min(balance / value, stack.getMaxStackSize() - stack.stackSize);

                totalValue = value * amount;
                balance -= totalValue;
                stack.stackSize += amount;

                setCoins(balance, fromOwner);
            }

            Transaction.CoinSource coinSource = new Transaction.MachineCoinSource(this, balanceBefore, -totalValue);
            Transaction transaction = new Transaction(this, Transaction.Operation.WITHDRAW_FROM_MACHINE, amount,
                    fromOwner?null:coinSource,
                    fromOwner?coinSource:null,
                    new ItemStack(item, amount));
            try
            {
                UniversalCoinsServer.cardDb.saveTransaction(transaction);

                worldObj.playSoundEffect(xCoord, yCoord, zCoord,
                         coinSource.getBalanceBefore()-coinSource.getBalanceAfter() > 1?
                                "universalcoins:take_coins":
                                "universalcoins:take_coin"
                        , 1.0F, 1.0F);

            }
            catch (DataBaseException e)
            {
                UniversalCoinsServer.logger.error(e);
            }
        }

        markDirty();
    }

    private int stateHashcode()
    {
        return Arrays.hashCode(new boolean[]{sellToUser, outOfCoins, outOfStock, outOfInventorySpace, buyButtonActive, sellButtonActive});
    }

    private void updateOperations()
    {
        int hashcode = stateHashcode();
        ItemStack trade = inventory[SLOT_TRADE];
        outOfCoins = !infinite && !sellToUser && (ownerCard != null || ownerCoins < price);
        if(trade == null)
        {
            outOfStock = true;
            outOfInventorySpace = false;
            if(stateHashcode() != hashcode)
            {
                markDirty();
            }
            return;
        }

        if(!infinite)
        {
            outOfStock = true;
            outOfInventorySpace = true;
            int foundItems = 0;
            int foundSpace = 0;
            for(int i = SLOT_STORAGE_FIST; i <= SLOT_STORAGE_LAST; i++)
            {
                ItemStack stack = inventory[i];
                if(stack == null)
                {
                    foundSpace += trade.getMaxStackSize();
                }
                else if(UniversalCoinsServerAPI.matches(stack, trade))
                {
                    int maxStackSize = stack.getMaxStackSize();
                    if(stack.stackSize < maxStackSize)
                        foundSpace += maxStackSize - stack.stackSize;
                    if(stack.stackSize > 0)
                        foundItems += stack.stackSize;
                }
            }

            if(foundItems >= trade.stackSize)
                outOfStock = false;
            if(foundSpace >= trade.stackSize)
                outOfInventorySpace = false;
        }
        else
        {
            outOfStock = false;
            outOfInventorySpace = false;
        }

        ItemStack sellStack = inventory[SLOT_SELL];
        sellButtonActive = !sellToUser && !outOfInventorySpace && !outOfCoins && UniversalCoinsServerAPI.matches(trade, sellStack)
                && sellStack.stackSize >= trade.stackSize && (ownerCard != null || ((long)userCoins)+price <= Integer.MAX_VALUE);
        buyButtonActive = sellToUser && !outOfStock && (userCard != null || userCoins >= price && ((long)ownerCoins)+price <= Integer.MAX_VALUE);

        if(buyButtonActive)
        {
            ItemStack output = inventory[SLOT_OUTPUT];
            if(output != null && (!UniversalCoinsServerAPI.matches(output, trade) || output.stackSize+trade.stackSize > output.getMaxStackSize()))
            {
                buyButtonActive = false;
            }
        }

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
        updateCards();
        markDirty();
    }

    @Override
    public void setOpener(EntityPlayer opener)
    {
        this.opener = opener;
        updateCards();
        updateOperations();
    }
}
