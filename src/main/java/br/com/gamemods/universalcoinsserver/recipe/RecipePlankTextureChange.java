package br.com.gamemods.universalcoinsserver.recipe;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Set;

public class RecipePlankTextureChange implements IRecipe
{
    private boolean acceptAnything, changeAdvSign, changeVendorFrame;
    private Set<String> acceptedNonWoodBlocks;
    private Set<String> acceptedNonWoodFromDictionary;

    public RecipePlankTextureChange(boolean acceptAnything, boolean changeAdvSign, boolean changeVendorFrame,
                                    Set<String> acceptedNonWoodBlocks, Set<String> acceptedNonWoodFromDictionary)
    {
        this.acceptAnything = acceptAnything;
        this.changeAdvSign = changeAdvSign;
        this.changeVendorFrame = changeVendorFrame;
        this.acceptedNonWoodBlocks = acceptedNonWoodBlocks;
        this.acceptedNonWoodFromDictionary = acceptedNonWoodFromDictionary;
    }

    private ItemStack newStack;
    private ItemStack plankStack;

    @Override
    public boolean matches(InventoryCrafting inventorycrafting, World world)
    {
        this.newStack = null;
        boolean hasItem = false;
        boolean hasPlank = false;
        for (int j = 0; j < inventorycrafting.getSizeInventory(); j++)
        {
            if (inventorycrafting.getStackInSlot(j) != null && !hasItem
                && (
                (changeAdvSign && inventorycrafting.getStackInSlot(j).getItem() == UniversalCoinsServer.proxy.itemAdvSign)
                || (changeVendorFrame && Block.getBlockFromItem(inventorycrafting.getStackInSlot(j).getItem()) == UniversalCoinsServer.proxy.blockVendorFrame)
                )
            ){
                hasItem = true;
                newStack = inventorycrafting.getStackInSlot(j).copy();
                continue;
            }
            if (inventorycrafting.getStackInSlot(j) != null && !hasPlank
                    && isWoodPlank(inventorycrafting.getStackInSlot(j)))
            {
                hasPlank = true;
                plankStack = inventorycrafting.getStackInSlot(j);
                continue;
            }
            if (inventorycrafting.getStackInSlot(j) != null)
                return false;
        }

        if (!hasPlank || !hasItem)
            return false;
        else
            return true;
    }

    private boolean isWoodPlank(ItemStack stack)
    {
        Block block = Block.getBlockFromItem(stack.getItem());
        if(acceptAnything && block != null)
            return true;

        if(acceptedNonWoodBlocks != null && acceptedNonWoodBlocks.contains(GameData.getItemRegistry().getNameForObject(stack.getItem())))
            return true;

        if(acceptedNonWoodFromDictionary != null)
            for(String oreName: acceptedNonWoodFromDictionary)
                for(ItemStack oreStack: OreDictionary.getOres(oreName))
                    if(OreDictionary.itemMatches(oreStack, stack, false))
                        return true;

        for (ItemStack oreStack : OreDictionary.getOres("plankWood"))
            if (OreDictionary.itemMatches(oreStack, stack, false))
                return true;

        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting var1)
    {
        NBTTagList itemList = new NBTTagList();
        NBTTagCompound tag = new NBTTagCompound();
        if (plankStack != null)
        {
            tag.setByte("Texture", (byte) 0);
            plankStack.writeToNBT(tag);
        }
        tag.setTag("Inventory", itemList);
        this.newStack.setTagCompound(tag);
        return newStack;
    }

    @Override
    public int getRecipeSize()
    {
        return 9;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return newStack;
    }

}
