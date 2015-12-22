package br.com.gamemods.universalcoinsserver.item;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.datastore.AccountAddress;
import br.com.gamemods.universalcoinsserver.datastore.AccountNotFoundException;
import br.com.gamemods.universalcoinsserver.datastore.DataBaseException;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.UUID;

public class ItemCard extends Item
{
    public ItemCard(CreativeTabs creativeTabs)
    {
        setMaxStackSize(1);
        setCreativeTab(creativeTabs);
        setUnlocalizedName("itemUCCard");
    }

    protected ItemCard(){}

    protected boolean canCheckBalance(ItemStack stack, EntityPlayer player)
    {
        return UniversalCoinsServer.proxy.cardCheckBalance && UniversalCoinsServerAPI.canCardBeUsedBy(stack, player);
    }

    protected void displayBalance(ItemStack stack, EntityPlayer player)
    {
        try
        {
            int balance = UniversalCoinsServer.cardDb.getAccountBalance(stack.stackTagCompound.getString("Account"));
            player.addChatComponentMessage(new ChatComponentTranslation("item.itemUCCard.balance")
                    .appendText(" " + NumberFormat.getIntegerInstance().format(balance)));
        }
        catch (AccountNotFoundException ignore)
        { }
        catch (DataBaseException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if(canCheckBalance(stack, player))
            displayBalance(stack, player);

        return super.onItemRightClick(stack, world, player);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float px, float py, float pz)
    {
        if(canCheckBalance(stack, player))
            displayBalance(stack, player);

        return super.onItemUse(stack, player, world, x, y, z, side, px, py, pz);
    }

    @Nullable
    public AccountAddress getAccountAddress(@Nullable ItemStack stack)
    {
        if(stack == null || stack.stackSize <= 0 || !(stack.getItem() instanceof ItemCard))
            return null;

        String number = stack.stackTagCompound.getString("Account");
        String owner = stack.stackTagCompound.getString("Owner");
        String name = stack.stackTagCompound.getString("Name");
        if(number.isEmpty() || owner.isEmpty() || name.isEmpty())
            return null;
        try
        {
            return new AccountAddress(number, name, UUID.fromString(owner));
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
