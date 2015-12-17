package br.com.gamemods.universalcoinsserver.item;

import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.Collections;
import java.util.Random;

public class ItemPackage extends Item
{
    public ItemPackage(CreativeTabs tabs)
    {
        setMaxStackSize(1);
        setUnlocalizedName("itemPackage");
    }

    @Override
    public ItemStack onItemRightClick(ItemStack packageStack, World world, EntityPlayer player)
    {
        ItemStack replacement = null;
        if (packageStack.stackTagCompound != null)
        {
            NBTTagList tagList = packageStack.stackTagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);

            packageStack.stackSize--;

            boolean destroyed = packageStack.stackSize > 0;

            Random rand = UniversalCoinsServerAPI.random;

            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
                boolean given;
                if (!player.inventory.addItemStackToInventory(stack))
                {
                    if(!destroyed)
                    {
                        replacement = stack;
                        destroyed = true;
                        given = true;
                    }
                    else
                    {
                        player.entityDropItem(stack, player.getEyeHeight());
                        given = false;
                    }
                }
                else given = true;

                if(given)
                    world.playSoundEffect(player.posX, player.posY+0.5, player.posZ, "random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }

            //if(!destroyed && packageStack.stackSize <= 0)
            //    player.inventory.setInventorySlotContents(player.inventory.currentItem, null);

            Collections.fill(player.openContainer.inventoryItemStacks, null);
            player.openContainer.detectAndSendChanges();
        }

        if(packageStack.stackSize > 0)
            return packageStack;
        return replacement != null? replacement : packageStack;
    }

    public boolean onItemUse(ItemStack packageStack, EntityPlayer player, World world, int x, int y, int z, int side,
                             float px, float py, float pz)
    {
        player.inventory.setInventorySlotContents(player.inventory.currentItem, onItemRightClick(packageStack, world, player));
        return true;
    }
}
