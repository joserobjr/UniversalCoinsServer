package br.com.gamemods.universalcoinsserver.command;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class CommandPackInventory extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "packinventory";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_)
    {
        return "Creates a package with all your inventory contents";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return sender instanceof EntityPlayer && super.canCommandSenderUseCommand(sender);
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] p_71515_2_)
    {
        EntityPlayer sender = (EntityPlayer) commandSender;
        NBTTagList itemList = new NBTTagList();
        int slot = 0;
        for(ItemStack stack: sender.inventory.mainInventory)
        {
            if(stack == null || stack.stackSize <= 0)
                continue;

            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("Slot", (byte) slot++);
            stack.writeToNBT(tag);
            itemList.appendTag(tag);
        }

        if(itemList.tagCount() == 0)
        {
            sender.addChatComponentMessage(new ChatComponentText("Your inventory is empty").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
            return;
        }

        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setTag("Inventory", itemList);
        ItemStack stack = new ItemStack(UniversalCoinsServer.proxy.itemPackage);
        stack.setTagCompound(tagCompound);

        if(!sender.inventory.addItemStackToInventory(stack))
            sender.addChatComponentMessage(new ChatComponentText("Your inventory is full").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
        else
            sender.addChatComponentMessage(new ChatComponentText("The packet has been added to your inventory").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)));

        sender.inventoryContainer.detectAndSendChanges();
    }
}
