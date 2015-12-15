package br.com.gamemods.universalcoinsserver.item;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.tile.TileAdvSign;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSign;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemAdvSign extends ItemSign
{
    public ItemAdvSign(CreativeTabs tabs)
    {
        setMaxStackSize(16);
        setCreativeTab(tabs);
        setTextureName("sign");
        setUnlocalizedName("itemUCSign");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                             int direction, float par8, float par9, float par10)
    {
        if (direction == 0)
            return false;
        else if (!world.getBlock(x, y, z).getMaterial().isSolid())
            return false;
        else if (direction == 1)
            ++y;
        else if (direction == 2)
            --z;
        else if (direction == 3)
            ++z;
        else if (direction == 4)
            --x;
        else if (direction == 5)
            ++x;

        if (!player.canPlayerEdit(x, y, z, direction, stack))
            return false;
        else if (!UniversalCoinsServer.proxy.blockStandingAdvSign.canPlaceBlockAt(world, x, y, z))
            return false;


        if (direction == 1)
        {
            int i1 = MathHelper.floor_double((double) ((player.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
            world.setBlock(x, y, z, UniversalCoinsServer.proxy.blockStandingAdvSign, i1, 3);
        }
        else
        {
            world.setBlock(x, y, z, UniversalCoinsServer.proxy.blockWallAdvSign, direction, 3);
        }

        --stack.stackSize;
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileAdvSign)
        {
            TileAdvSign tile = (TileAdvSign) te;
            if (stack.hasTagCompound())
            {
                NBTTagCompound tagCompound = stack.getTagCompound();
                String blockIcon = tagCompound.getString("BlockIcon");
                if (blockIcon.isEmpty())
                {
                    //NBTTagList textureList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
                    //byte slot = tagCompound.getByte("Texture");
                    //ItemStack textureStack = ItemStack.loadItemStackFromNBT(tagCompound);
                    //tile.sendTextureUpdateMessage(textureStack);
                }
                else
                {
                    tile.icon = blockIcon;
                }
            }

            tile.ownerName = player.getDisplayName();
            tile.owner = player.getPersistentID();
            player.openGui(UniversalCoinsServer.instance, 0, world, x, y, z);
        }
        return true;
    }
}
