package br.com.gamemods.universalcoinsserver;

import br.com.gamemods.universalcoinsserver.datastore.CardDataBase;
import br.com.gamemods.universalcoinsserver.datastore.DataBaseException;
import br.com.gamemods.universalcoinsserver.net.*;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Callable;

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

    Callable<Void> hook;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) throws IOException, SQLException
    {
        proxy.configs = proxy.new ConfigLoader(new Configuration(event.getSuggestedConfigurationFile()));
        proxy.configs.load();
        logger = event.getModLog();

        MinecraftForge.EVENT_BUS.register(new PlayerPickupEventHandler());
        if(proxy.configs.mobsDropCoins)
            MinecraftForge.EVENT_BUS.register(new MobDropEventHandler());

        network = NetworkRegistry.INSTANCE.newSimpleChannel("universalcoins");
        network.registerMessage(ButtonMessage.class, ButtonMessage.class, 0, Side.SERVER);
        network.registerMessage(VendorServerMessage.class, VendorServerMessage.class, 1, Side.SERVER);
        network.registerMessage(TileCardStationMessage.class, TileCardStationMessage.class, 3, Side.CLIENT);
        network.registerMessage(CardStationServerWithdrawalMessage.class, CardStationServerWithdrawalMessage.class, 4,
                Side.SERVER);
        network.registerMessage(CardStationServerCustomNameMessage.class, CardStationServerCustomNameMessage.class, 5,
                Side.SERVER);
        network.registerMessage(TextureMessage.class, TextureMessage.class, 7, Side.SERVER);
        network.registerMessage(SignMessage.class, SignMessage.class, 8, Side.CLIENT);
        network.registerMessage(SignServerMessage.class, SignServerMessage.class, 9, Side.SERVER);
        network.registerMessage(PackagerServerMessage.class, PackagerServerMessage.class, 11, Side.SERVER);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) throws SQLException, ClassNotFoundException, IOException, DataBaseException
    {
        proxy.configs.initConnection();
        proxy.registerBlocks();
        proxy.registerItems();
        proxy.registerTiles();
        proxy.registerGuis();
        proxy.registerRecipes();
        proxy.registerAchievements();

        if(proxy.configs.coinsInMineshaft)
            ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(
                    new WeightedRandomChestContent(new ItemStack(proxy.coins[proxy.configs.chestCoin]), proxy.configs.chestMinStack, proxy.configs.chestMaxStack, proxy.configs.mineshaftCoinChance));

        if (proxy.configs.coinsInDungeon)
            ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(
                    new WeightedRandomChestContent(new ItemStack(proxy.coins[proxy.configs.chestCoin]), proxy.configs.chestMinStack, proxy.configs.chestMaxStack, proxy.configs.dungeonCoinChance));

        proxy.configs = null;
    }

    @Mod.EventHandler
    public void postServerInit(FMLServerStartedEvent event) throws Exception
    {
        if(hook != null)
            hook.call();
    }
}
