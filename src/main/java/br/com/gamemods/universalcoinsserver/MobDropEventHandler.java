package br.com.gamemods.universalcoinsserver;

import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

public class MobDropEventHandler
{
    int mobDropChance, mobDropMax, enderDragonMultiplier;

    public MobDropEventHandler(int mobDropChance, int mobDropMax, int enderDragonMultiplier)
    {
        this.mobDropChance = mobDropChance;
        this.mobDropMax = mobDropMax;
        this.enderDragonMultiplier = enderDragonMultiplier;
    }

    @SubscribeEvent
    public void onEntityDrop(LivingDropsEvent event)
    {
        if (event.source.getEntity() instanceof EntityPlayer)
        {
            int chance;
            if (mobDropChance <= 0)
                chance = 0;
            else
                chance = UniversalCoinsServerAPI.random.nextInt(mobDropChance);

            int randomDropValue = UniversalCoinsServerAPI.random.nextInt(mobDropMax) + 1;

            // get mob max health and adjust drop value
            float health = event.entityLiving.getMaxHealth();
            if (health == 0)
                health = 10;
            int dropped = (int) (randomDropValue * health / 20 * (event.lootingLevel + 1));

            //multiply drop if ender dragon
            if (event.entity instanceof EntityDragon)
            {
                dropped = dropped * enderDragonMultiplier;
                while (dropped > 0)
                {
                    int logVal = Math.min((int) (Math.log(dropped) / Math.log(9)), 4);
                    int stackSize = Math.min((int) (dropped / Math.pow(9, logVal)), 64);
                    ItemStack test = new ItemStack(UniversalCoinsServer.proxy.coins[logVal], stackSize);
                    event.entity.entityDropItem(test, 0.0F);
                    dropped -= Math.pow(9, logVal) * stackSize;
                }
            }

            // drop coins
            if (event.entity instanceof EntityMob
                    && !event.entity.worldObj.isRemote && chance == 0) {
                while (dropped > 0)
                {
                    int logVal = Math.min((int) (Math.log(dropped) / Math.log(9)), 4);
                    int stackSize = Math.min((int) (dropped / Math.pow(9, logVal)), 64);
                    ItemStack test = new ItemStack(UniversalCoinsServer.proxy.coins[logVal], stackSize);
                    event.entity.entityDropItem(test, 0.0F);
                    dropped -= Math.pow(9, logVal) * stackSize;
                }
            }
        }
    }
}
