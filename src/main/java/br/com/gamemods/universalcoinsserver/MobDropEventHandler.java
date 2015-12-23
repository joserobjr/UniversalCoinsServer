package br.com.gamemods.universalcoinsserver;

import br.com.gamemods.universalcoinsserver.api.UniversalCoinsServerAPI;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class MobDropEventHandler
{
    int mobDropChance, mobDropMax, enderDragonMultiplier;

    private Map<UUID, Map<UUID, Float>> damageDealt = new HashMap<>();
    private Map<UUID, EntityMob> entityMap = new HashMap<>();
    private Map<UUID, Float> lastDamages = new HashMap<>();

    public MobDropEventHandler(int mobDropChance, int mobDropMax, int enderDragonMultiplier)
    {
        this.mobDropChance = mobDropChance;
        this.mobDropMax = mobDropMax;
        this.enderDragonMultiplier = enderDragonMultiplier;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void onEntityDamage(LivingAttackEvent event)
    {
        if(!(event.entity instanceof EntityMob) || event.entityLiving.isEntityInvulnerable() || event.entityLiving.getHealth() <= 0f)
            return;

        Entity source = event.source.getEntity();
        if((float)event.entityLiving.hurtResistantTime > event.entityLiving.maxHurtResistantTime / 2f)
        {
            Float last = lastDamages.get(event.entity.getPersistentID());
            if(last == null) last = 0f;
            if(event.ammount <= last)
                return;

            lastDamages.put(event.entity.getPersistentID(), event.ammount);
        }

        if(source instanceof EntityPlayer)
        {
            Map<UUID, Float> damageMap = damageDealt.get(event.entity.getPersistentID());
            if(damageMap == null) damageDealt.put(event.entity.getPersistentID(), damageMap = new HashMap<>(2));
            Float total = damageMap.get(source.getPersistentID());
            if(total == null) total = 0f;
            damageMap.put(source.getPersistentID(), Math.min(event.entityLiving.getMaxHealth(), total+Math.min(event.ammount, event.entityLiving.getHealth())));
            entityMap.put(event.entity.getPersistentID(), (EntityMob) event.entity);

            Iterator<Map.Entry<UUID, EntityMob>> iterator = entityMap.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry<UUID, EntityMob> entry = iterator.next();
                if(entry.getValue().isDead)
                {
                    UUID key = entry.getKey();
                    damageDealt.remove(key);
                    lastDamages.remove(key);
                    iterator.remove();
                }
            }
        }
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
            else
            {
                UUID persistentID = event.entity.getPersistentID();
                Map<UUID, Float> damageMap = damageDealt.remove(persistentID);
                entityMap.remove(persistentID);
                lastDamages.remove(persistentID);
                if(damageMap == null || damageMap.isEmpty())
                    return;

                float damageByPlayers = 0f;
                for(Float damage: damageMap.values())
                    damageByPlayers += damage;

                damageByPlayers = Math.min(damageByPlayers, health);
                dropped = (int)(dropped * (damageByPlayers/health));
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
