package br.com.gamemods.universalcoinsserver;

import br.com.gamemods.universalcoinsserver.datastore.CardDataBase;
import br.com.gamemods.universalcoinsserver.datastore.PropertiesDB;
import br.com.gamemods.universalcoinsserver.net.*;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

@Mod(modid = "universalcoins", name = "Universal Coins Server", version = "1.7.10-1.6.38-gamemods")
@SideOnly(Side.SERVER)
public class UniversalCoinsServer
{
    @SidedProxy(serverSide = "br.com.gamemods.universalcoinsserver.CommonProxy")
    public static CommonProxy proxy = new CommonProxy();
    public static Logger logger;

    @Mod.Instance("universalcoins")
    public static UniversalCoinsServer instance;

    public static SimpleNetworkWrapper network;
    public static CardDataBase cardDb;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) throws IOException
    {
        proxy.config = new Configuration(event.getSuggestedConfigurationFile());
        proxy.loadConfig();
        logger = event.getModLog();

        network = NetworkRegistry.INSTANCE.newSimpleChannel("universalcoins");
        network.registerMessage(ButtonMessage.class, ButtonMessage.class, 0, Side.SERVER);
        network.registerMessage(VendorServerMessage.class, VendorServerMessage.class, 1, Side.SERVER);
        network.registerMessage(TileCardStationMessage.class, TileCardStationMessage.class, 3, Side.CLIENT);
        network.registerMessage(TextureMessage.class, TextureMessage.class, 7, Side.SERVER);
        network.registerMessage(SignMessage.class, SignMessage.class, 8, Side.CLIENT);
        network.registerMessage(SignServerMessage.class, SignServerMessage.class, 9, Side.SERVER);
        network.registerMessage(PackagerServerMessage.class, PackagerServerMessage.class, 11, Side.SERVER);

        cardDb = new PropertiesDB(new File(event.getModConfigurationDirectory(), "database"));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.registerBlocks();
        proxy.registerItems();
        proxy.registerTiles();
        proxy.registerGuis();
        proxy.registerRecipes();
    }
}
