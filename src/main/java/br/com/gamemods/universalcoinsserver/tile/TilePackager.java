package br.com.gamemods.universalcoinsserver.tile;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.event.PlayerLookupEvent;
import br.com.gamemods.universalcoinsserver.event.PlayerSendPackage;
import br.com.gamemods.universalcoinsserver.item.ItemCoin;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.item.EntityItem;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class TilePackager extends TileTransactionMachine
{
    public static final int SLOT_PACKAGE_START = 0;
    public static final int SLOT_PACKAGE_END = 7;
    public static final int SLOT_CARD = 8;
    public static final int SLOT_COIN_INPUT = 9;
    public static final int SLOT_OUTPUT = 10;
    public static final int SLOT_PACKAGE_INPUT = 11;
    public static final int BUTTON_SEND_OR_BUY = 0;
    public static final int BUTTON_WITHDRAW = 1;
    public static final int BUTTON_SIZE_SMALL = 2;
    public static final int BUTTON_SIZE_MEDIUM = 3;
    public static final int BUTTON_SIZE_LARGE = 4;

    private ItemStack[] inventory = new ItemStack[12];
    public int userCoins;
    public int packageSize;
    public int[] price = { 10, 20, 40 };
    private boolean outputUnlocked;
    private String targetName = "";
    public UUID targetId;

    public void validateFields()
    {
        for(int i = 0; i < price.length; i++)
            if(price[i] <= 0)
                price[i] = 1;

        if(packageSize < 0)
            packageSize = 0;
        else if(packageSize > 2)
            packageSize = 2;

        if(userCoins < 0)
            userCoins = 0;
    }


    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        inventory[slot] = stack;
        if(slot == SLOT_COIN_INPUT)
        {
            Item item = stack.getItem();
            if(item instanceof ItemCoin)
            {
                int itemValue = ((ItemCoin) item).getValue();
                int depositAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - userCoins) / itemValue);
                int depositValue = depositAmount * itemValue;

                userCoins += depositValue;
                worldObj.playSoundEffect(xCoord, yCoord, zCoord, "universalcoins:insert_coin", 1f, 1f);

                stack.stackSize -= depositAmount;
                if (stack.stackSize == 0)
                    inventory[slot] = null;
            }
        }
        markDirty();
    }

    @Override
    public void onButtonPressed(EntityPlayerMP player, int buttonId, boolean shiftPressed)
    {
        switch (buttonId)
        {
            case BUTTON_SIZE_SMALL:
            case BUTTON_SIZE_MEDIUM:
            case BUTTON_SIZE_LARGE:
                setPackageSize(buttonId - BUTTON_SIZE_SMALL);
                return;

            case BUTTON_WITHDRAW:
            {
                int before = userCoins;
                try
                {
                    outputUnlocked = true;
                    userCoins = UniversalCoinsServerAPI.addCoinsToSlot(this, userCoins, SLOT_OUTPUT);

                    worldObj.playSoundEffect(xCoord, yCoord, zCoord,
                            before-userCoins > 1?
                                    "universalcoins:take_coins":
                                    "universalcoins:take_coin"
                            , 1.0F, 1.0F);
                }
                finally
                {
                    outputUnlocked = false;
                }
                if (before == userCoins)
                    return;

                markDirty();
                return;
            }

            case BUTTON_SEND_OR_BUY:
                if(shiftPressed)
                    send();
                else
                    purchasePacket();
        }
    }

    public void send()
    {
        ItemStack stack = inventory[SLOT_PACKAGE_INPUT];
        if(targetId == null || opener == null || stack == null || stack.stackSize <=0 || !stack.hasTagCompound())
            return;

        //noinspection unchecked
        for(EntityPlayer p: (List<EntityPlayer>) MinecraftServer.getServer().getConfigurationManager().playerEntityList)
        {
            if(p.getPersistentID().equals(targetId))
            {
                if(MinecraftForge.EVENT_BUS.post(new PlayerSendPackage(opener, p, stack, this)))
                    return;

                if(!p.inventory.addItemStackToInventory(stack))
                {
                    Random rand = UniversalCoinsServerAPI.random;
                    float rx = rand.nextFloat() * 0.8F + 0.1F;
                    float ry = rand.nextFloat() * 0.8F + 0.1F;
                    float rz = rand.nextFloat() * 0.8F + 0.1F;
                    EntityItem entityItem = new EntityItem(worldObj, p.posX + rx, p.posY + ry, p.posZ + rz,
                            inventory[SLOT_PACKAGE_INPUT]);
                    worldObj.spawnEntityInWorld(entityItem);
                }
                else
                {
                    p.addChatComponentMessage(
                            new ChatComponentText(opener.getDisplayName())
                                .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
                                .appendSibling(new ChatComponentTranslation("packager.message.sent"))
                    );
                }
                inventory[SLOT_PACKAGE_INPUT] = null;
                markDirty();
                return;
            }
        }

        opener.addChatComponentMessage(new ChatComponentTranslation("player.is.not.online").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
    }

    public void playerLookup(String player, boolean tabPressed)
    {
        String before = targetName;
        if (tabPressed)
        {
            //noinspection unchecked
            List<EntityPlayer> onlinePlayers = (List<EntityPlayer>) MinecraftServer.getServer().getConfigurationManager().playerEntityList;
            Map<String, EntityPlayer> playerMap = new HashMap<>(onlinePlayers.size());
            for (EntityPlayer p : onlinePlayers)
                playerMap.put(p.getDisplayName(), p);

            String test[] = new String[1];
            test[0] = player;

            //noinspection unchecked
            ArrayList<String> match = (ArrayList<String>) CommandBase.getListOfStringsFromIterableMatchingLastWord(test, playerMap.keySet());

            HashMap<String, UUID> uuidMap = new HashMap<>(match.size());
            for(String matchedName: match)
                uuidMap.put(matchedName, playerMap.get(matchedName).getPersistentID());

            PlayerLookupEvent event = new PlayerLookupEvent(player, match, uuidMap);
            MinecraftForge.EVENT_BUS.post(event);
            if(event.exactResultId != null)
            {
                targetName = event.exactResultName;
                targetId = event.exactResultId;
                markDirty();
                return;
            }

            if (match.size() > 0)
            {
                targetName = match.get(0);
                targetId = uuidMap.get(targetName);
                markDirty();
                return;
            }
        }
        else
        {
            EntityPlayer onlinePlayer = null;
            //noinspection unchecked
            for(EntityPlayer p: (List<EntityPlayer>) MinecraftServer.getServer().getConfigurationManager().playerEntityList)
                if(p.getCommandSenderName().equalsIgnoreCase(player))
                {
                    onlinePlayer = p;
                    break;
                }

            if (onlinePlayer != null)
            {
                targetName = player;
                targetId = onlinePlayer.getPersistentID();
                markDirty();
                return;
            }
            else
            {
                ArrayList<String> match = new ArrayList<>(1);
                HashMap<String, UUID> uuidMap = new HashMap<>(1);
                MinecraftForge.EVENT_BUS.post(new PlayerLookupEvent(player, match, uuidMap));
                if(match.size() > 1)
                {
                    targetName = match.get(0);
                    if(targetName.equalsIgnoreCase(player))
                        targetId = uuidMap.get(targetName);
                }
                else
                    targetName = player;
            }
        }

        if(!before.equals(targetName))
        {
            targetId = null;
            markDirty();
        }
    }

    public void purchasePacket()
    {
        NBTTagList itemList = new NBTTagList();
        NBTTagCompound tagCompound = new NBTTagCompound();

        int firstSlot = (2-packageSize) * 2;
        if(firstSlot < 0)
            throw new IllegalArgumentException("firstSlot < 0: "+firstSlot);

        if(userCoins < price[packageSize])
        {
            scheduleUpdate();
            return;
        }

        for (int slot = firstSlot; slot <= SLOT_PACKAGE_END; slot++)
        {
            ItemStack stack = inventory[slot];
            if (stack == null || stack.getItem() == UniversalCoinsServer.proxy.itemPackage)
                continue;

            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("Slot", (byte) slot);
            stack.writeToNBT(tag);
            itemList.appendTag(tag);
            inventory[slot] = null;
        }


        if (itemList.tagCount() > 0)
        {
            ItemStack stack = new ItemStack(UniversalCoinsServer.proxy.itemPackage);
            inventory[SLOT_OUTPUT] = stack;
            tagCompound.setTag("Inventory", itemList);
            stack.setTagCompound(tagCompound);

            userCoins -= price[packageSize];
            validateFields();
            markDirty();
        }
    }

    public void setPackageSize(int size)
    {
        if(size > 2) throw new IllegalArgumentException("size > 2: "+size);
        this.packageSize = size;

        int firstSlot = (2-size) * 2;
        if(firstSlot == 0)
        {
            markDirty();
            return;
        }

        for(int disabledSlot = 0; disabledSlot < firstSlot; disabledSlot++)
        {
            ItemStack stack = inventory[disabledSlot];
            if(stack != null)
            {
                for(int slot=firstSlot; slot < SLOT_PACKAGE_END; slot++)
                {
                    ItemStack foundStack = inventory[slot];
                    if(foundStack == null)
                    {
                        inventory[slot] = stack;
                        inventory[disabledSlot] = null;
                        stack = null;
                        break;
                    }
                    else
                    {
                        int maxSize = stack.getMaxStackSize();
                        if(stack.stackSize < maxSize && UniversalCoinsServerAPI.matches(stack, foundStack))
                        {
                            int moved = Math.min(maxSize - foundStack.stackSize, stack.stackSize);
                            foundStack.stackSize += moved;
                            if(stack.stackSize <= moved)
                            {
                                inventory[disabledSlot] = null;
                                stack = null;
                                break;
                            }
                            else
                                stack.stackSize -= moved;
                        }
                    }
                }

                World world=worldObj;
                double x=xCoord,y=yCoord,z=zCoord;
                if(opener != null)
                {
                    if(opener.inventory.addItemStackToInventory(stack))
                    {
                        inventory[disabledSlot] = null;
                        stack = null;
                    }

                    if(stack != null && stack.stackSize > 0)
                    {
                        opener.entityDropItem(stack, opener.getEyeHeight());
                        stack = null;
                        inventory[disabledSlot] = null;
                    }
                }

                if(stack != null && stack.stackSize > 0)
                {
                    UniversalCoinsServerAPI.drop(world, x, y, z, Collections.singletonList(stack));
                    inventory[disabledSlot] = null;
                }
            }
        }

        markDirty();
    }

    @Override
    public void markDirty()
    {
        scheduleUpdate();
        super.markDirty();
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inventory.length; i++)
        {
            ItemStack stack = inventory[i];
            if (stack != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }

        tagCompound.setTag("Inventory", itemList);
        tagCompound.setInteger("coinSum", userCoins);
        tagCompound.setBoolean("cardAvailable", false);
        tagCompound.setString("customName", "");
        tagCompound.setString("packageTarget", targetName);
        if(targetId != null) tagCompound.setString("packageTargetId", targetId.toString());
        tagCompound.setBoolean("inUse", false);
        tagCompound.setInteger("packageSize", packageSize);
        tagCompound.setInteger("smallPrice", price[0]);
        tagCompound.setInteger("medPrice", price[1]);
        tagCompound.setInteger("largePrice", price[2]);
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
                inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
        }

        userCoins = tagCompound.getInteger("coinSum");
        packageSize = tagCompound.getInteger("packageSize");
        price[0] = tagCompound.getInteger("smallPrice");
        price[1] = tagCompound.getInteger("medPrice");
        price[2] = tagCompound.getInteger("largePrice");
        targetName = tagCompound.getString("packageTarget");
        String str = tagCompound.getString("packageTargetId");
        if(str.isEmpty())
            targetId = null;
        else
            try
            {
                targetId = UUID.fromString(str);
            }
            catch (Exception ignored)
            {
                targetId = null;
            }

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

        if(targetId == null)
            nbt.setString("packageTarget", "");

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
            markDirty();
        }
        return stack;
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
        if(slot >= SLOT_PACKAGE_START && slot <= SLOT_PACKAGE_END)
        {
            int firstSlot = (2-packageSize) * 2;
            return slot >= firstSlot;
        }

        return outputUnlocked && slot == SLOT_OUTPUT || slot != SLOT_COIN_INPUT && slot != SLOT_OUTPUT;
    }
}
