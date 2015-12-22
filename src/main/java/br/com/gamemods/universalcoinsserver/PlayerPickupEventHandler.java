package br.com.gamemods.universalcoinsserver;

import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import br.com.gamemods.universalcoinsserver.datastore.AccountNotFoundException;
import br.com.gamemods.universalcoinsserver.datastore.DataBaseException;
import br.com.gamemods.universalcoinsserver.datastore.PlayerOperator;
import br.com.gamemods.universalcoinsserver.datastore.Transaction;
import br.com.gamemods.universalcoinsserver.item.ItemCoin;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import java.text.NumberFormat;

public class PlayerPickupEventHandler
{
    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event)
    {
        ItemStack itemStack = event.item.getEntityItem();
        if(!(itemStack.getItem() instanceof ItemCoin) || itemStack.stackSize <= 0)
            return;

        event.entityPlayer.addStat(UniversalCoinsServer.proxy.achievementCoin, 1);

        if(!UniversalCoinsServer.proxy.enderDepositFromInventory)
            return;

        ItemCoin coin = (ItemCoin) itemStack.getItem();
        for(ItemStack stack: event.entityPlayer.inventory.mainInventory)
        {
            if(stack == null || stack.getItem() != UniversalCoinsServer.proxy.itemEnderCard || stack.stackTagCompound == null
                    || stack.stackTagCompound.getBoolean("DisablePickup")
                    || !UniversalCoinsServerAPI.canCardBeUsedBy(stack, event.entityPlayer))
                continue;

            int stackValue = itemStack.stackSize * coin.getValue();
            int coins = UniversalCoinsServerAPI.scanCoins(event.entityPlayer.inventory).getCoins() + stackValue;

            try
            {
                String account = stack.stackTagCompound.getString("Account");

                PlayerOperator playerOperator = new PlayerOperator(event.entityPlayer);
                Transaction transaction = new Transaction(
                        playerOperator, new Transaction.InventoryCoinSource(playerOperator, coins, -stackValue),
                        new Transaction.CardCoinSource(stack, stackValue),
                        coins
                );

                if(UniversalCoinsServer.cardDb.canDeposit(account, coins) < 0)
                    return;

                UniversalCoinsServer.cardDb.depositToAccount(account, coins, transaction);

                event.entityPlayer.addChatComponentMessage(
                        new ChatComponentTranslation("item.itemEnderCard.message.deposit")
                            .appendText(" "+ NumberFormat.getIntegerInstance().format(stackValue)+" ")
                            .appendSibling(new ChatComponentTranslation("item.itemCoin.name"))
                );

                event.setCanceled(true);
                event.item.setDead();
                return;
            }
            catch (AccountNotFoundException e)
            {
                stack.stackTagCompound.setBoolean("DisablePickup", true);
                return;
            }
            catch (DataBaseException e)
            {
                e.printStackTrace();
            }
        }
    }
}
