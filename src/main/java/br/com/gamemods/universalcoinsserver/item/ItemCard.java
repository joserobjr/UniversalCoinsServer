package br.com.gamemods.universalcoinsserver.item;

import br.com.gamemods.universalcoinsserver.datastore.AccountAddress;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

public class ItemCard extends Item
{
    public ItemCard(CreativeTabs creativeTabs)
    {
        setMaxStackSize(1);
        setCreativeTab(creativeTabs);
        setUnlocalizedName("itemUCCard");
    }

    public ItemCard()
    {
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
