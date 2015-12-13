package br.com.gamemods.universalcoinsserver.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import java.util.UUID;

public class TileVendor extends TileEntity
{
    private ItemStack[] inventory = new ItemStack[17];

    public UUID owner;

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
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
        compound.setInteger("CoinSum", 0);
        compound.setInteger("UserCoinSum", 0);
        compound.setInteger("ItemPrice", 0);
        compound.setString("BlockOwner", owner == null? "" : owner.toString());
        compound.setBoolean("Infinite", false);
        compound.setBoolean("Mode", false);
        compound.setBoolean("OutOfStock", false);
        compound.setBoolean("OutOfCoins", false);
        compound.setBoolean("InventoryFull", false);
        compound.setBoolean("BuyButtonActive", false);
        compound.setBoolean("SellButtonActive", false);
        compound.setBoolean("CoinButtonActive", false);
        compound.setBoolean("SmallStackButtonActive", false);
        compound.setBoolean("LargeStackButtonActive", false);
        compound.setBoolean("SmallBagButtonActive", false);
        compound.setBoolean("LargeBagButtonActive", false);
        compound.setBoolean("UserCoinButtonActive", false);
        compound.setBoolean("UserSmallStackButtonActive", false);
        compound.setBoolean("UserLargeStackButtonActive", false);
        compound.setBoolean("UserSmallBagButtonActive", false);
        compound.setBoolean("UserLargeBagButtonActive", false);
        compound.setBoolean("InUse", false);
        compound.setString("BlockIcon", "");
        compound.setInteger("TextColor", 0);
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
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }
}
