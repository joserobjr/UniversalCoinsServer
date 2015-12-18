package br.com.gamemods.universalcoinsserver.item;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.api.ScanResult;
import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.datastore.DataBaseException;
import br.com.gamemods.universalcoinsserver.datastore.PlayerOperator;
import br.com.gamemods.universalcoinsserver.datastore.Transaction;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import java.text.NumberFormat;
import java.util.Map;

public class ItemEnderCard extends ItemCard
{
    public ItemEnderCard(CreativeTabs tabs)
    {
        setMaxStackSize(1);
        setCreativeTab(tabs);
        setUnlocalizedName("itemEnderCard");
    }

    private void deposit(ItemStack stack, EntityPlayer player)
    {
        ScanResult scanResult = UniversalCoinsServerAPI.scanCoins(player.inventory);
        int coins = scanResult.getCoins();
        if(coins <= 0)
            return;

        try
        {
            String account = stack.stackTagCompound.getString("Account");
            if(UniversalCoinsServer.cardDb.canDeposit(account, coins) > 0)
                return;

            PlayerOperator playerOperator = new PlayerOperator(player);
            Transaction transaction = new Transaction(
                    playerOperator, new Transaction.InventoryCoinSource(playerOperator, coins, -coins),
                    new Transaction.CardCoinSource(stack, coins),
                    coins
            );

            UniversalCoinsServer.cardDb.depositToAccount(account, coins, transaction);
            for(Map.Entry<Integer, Integer> entry: scanResult)
                player.inventory.setInventorySlotContents(entry.getKey(), null);
            player.inventoryContainer.detectAndSendChanges();

            player.addChatComponentMessage(new ChatComponentTranslation("item.itemEnderCard.message.deposit")
                    .appendText(" " + NumberFormat.getIntegerInstance().format(coins) + " ")
                    .appendSibling(new ChatComponentTranslation("item.itemCoin.name"))
            );
        }
        catch (DataBaseException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean canCheckBalance(ItemStack stack, EntityPlayer player)
    {
        return UniversalCoinsServer.proxy.enderCheckBalance && UniversalCoinsServerAPI.canCardBeUsedBy(stack, player);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float px, float py, float pz)
    {
        if(UniversalCoinsServer.proxy.enderDepositFromInventory && UniversalCoinsServerAPI.canCardBeUsedBy(stack, player))
            deposit(stack, player);

        return super.onItemUse(stack, player, world, x, y, z, side, px, py, pz);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if(UniversalCoinsServer.proxy.enderDepositFromInventory && UniversalCoinsServerAPI.canCardBeUsedBy(stack, player))
            deposit(stack, player);

        return super.onItemRightClick(stack, world, player);
    }
}
