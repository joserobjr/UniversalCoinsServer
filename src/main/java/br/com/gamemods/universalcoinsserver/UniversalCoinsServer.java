package br.com.gamemods.universalcoinsserver;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

@Mod(modid = "universalcoins", name = "Universal Coins Server", version = "1.7.10-1.6.38")
@SideOnly(Side.SERVER)
public class UniversalCoinsServer
{
    @SidedProxy(serverSide = "br.com.gamemods.universalcoinsserver.CommonProxy")
    public static CommonProxy proxy = new CommonProxy();
    public static Logger logger;

    @Mod.Instance("universalcoins")
    public static UniversalCoinsServer instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.config = new Configuration(event.getSuggestedConfigurationFile());
        proxy.loadConfig();
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.registerBlocks();
        proxy.registerItems();
        proxy.registerTiles();
        proxy.registerGuis();
    }
}
