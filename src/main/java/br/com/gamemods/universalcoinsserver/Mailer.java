package br.com.gamemods.universalcoinsserver;

import br.com.gamemods.universalcoinsserver.datastore.DataStoreException;
import br.com.gamemods.universalcoinsserver.event.PlayerLookupEvent;
import br.com.gamemods.universalcoinsserver.event.PlayerSendPackage;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class Mailer
{
    private Queue<Runnable> scheduledTasks = new LinkedList<>();

    public Mailer()
    {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void on(TickEvent.ServerTickEvent event)
    {
        Runnable task;
        while((task = scheduledTasks.poll()) != null)
            task.run();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event)
    {
        try
        {
            UniversalCoinsServer.cardDb.updatePlayerName(event.player.getPersistentID(), event.player.getCommandSenderName());

            new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        final int deliveries;
                        try
                        {
                            deliveries = UniversalCoinsServer.cardDb.getPendingDeliveries(event.player.getPersistentID());
                        } catch (DataStoreException e)
                        {
                            e.printStackTrace();
                            return;
                        }

                        if(deliveries <= 0)
                            return;

                        sleep(3000);

                        scheduledTasks.add(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                EntityPlayer player;
                                if(!event.player.isDead)
                                    player = event.player;
                                else
                                {
                                    player = null;
                                    String commandSenderName = event.player.getCommandSenderName();
                                    WorldServer[] ws = MinecraftServer.getServer().worldServers;
                                    for (WorldServer w : ws)
                                    {
                                        EntityPlayer p = w.getPlayerEntityByName(commandSenderName);
                                        if (w.playerEntities.contains(p))
                                        {
                                            player = p;
                                            break;
                                        }
                                    }
                                }

                                if(player != null)
                                    player.addChatComponentMessage(
                                            new ChatComponentTranslation("you.have.packets.to.receive", deliveries,
                                                    "/"+UniversalCoinsServer.instance.commandReceivePackets.getCommandName())
                                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN))
                                    );
                            }
                        });

                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }.start();

            //UniversalCoinsServer.cardDb.deliveryPackages(event.player);
        } catch (DataStoreException e)
        {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onSendPackage(PlayerSendPackage event)
    {
        if(event.targetPlayer != null)
            return;

        try
        {
            if(UniversalCoinsServer.cardDb.storePackage(event.getPackage(), event.sender, event.targetId))
            {
                event.setCanceled(true);
                event.delivered = true;
            }
        }
        catch (DataStoreException e)
        {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onPlayerLookup(PlayerLookupEvent event)
    {
        try
        {
            UUID exact = UniversalCoinsServer.cardDb.getPlayerIdByName(event.searchedName);
            if(exact != null)
            {
                event.exactResultId = exact;
                event.exactResultName = event.searchedName;
            }

            Map<UUID, String> result = UniversalCoinsServer.cardDb.findPlayerByName(event.searchedName);
            if(result == null)
                return;


            for(Map.Entry<UUID, String> entry: result.entrySet())
            {
                if(!event.matchedNames.contains(entry.getValue()))
                {
                    event.matchedNames.add(entry.getValue());
                    event.uuidMap.put(entry.getValue(), entry.getKey());
                }
            }
        } catch (DataStoreException e)
        {
            e.printStackTrace();
        }
    }
}
