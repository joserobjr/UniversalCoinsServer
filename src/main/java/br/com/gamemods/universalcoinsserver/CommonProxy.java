package br.com.gamemods.universalcoinsserver;

import br.com.gamemods.universalcoinsserver.blocks.*;
import br.com.gamemods.universalcoinsserver.datastore.NbtDB;
import br.com.gamemods.universalcoinsserver.datastore.SqlDB;
import br.com.gamemods.universalcoinsserver.item.*;
import br.com.gamemods.universalcoinsserver.recipe.RecipeEnderCard;
import br.com.gamemods.universalcoinsserver.recipe.RecipePlankTextureChange;
import br.com.gamemods.universalcoinsserver.recipe.RecipeVendingFrame;
import br.com.gamemods.universalcoinsserver.tile.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.RecipeSorter;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommonProxy
{
    public static Block[] supports = { Blocks.stone, Blocks.cobblestone, Blocks.stonebrick, Blocks.planks,
            Blocks.crafting_table, Blocks.gravel, Blocks.jukebox, Blocks.sandstone, Blocks.gold_block,
            Blocks.iron_block, Blocks.brick_block, Blocks.mossy_cobblestone, Blocks.obsidian, Blocks.diamond_block,
            Blocks.emerald_block, Blocks.lapis_block, };
    static Object[] reagents = { Blocks.stone, Blocks.cobblestone, Blocks.stonebrick, Blocks.planks,
            Blocks.crafting_table, Blocks.gravel, Blocks.jukebox, Blocks.sandstone, Items.gold_ingot, Items.iron_ingot,
            Blocks.brick_block, Blocks.mossy_cobblestone, Blocks.obsidian, Items.diamond, Items.emerald,
            Blocks.lapis_block, };

    public Block blockTradeStation, blockVendor, blockVendorFrame, blockCardStation, blockBase, blockSafe,
            blockStandingAdvSign, blockWallAdvSign, blockSlots, blockSignal, blockPackager, blockPowerBase, blockPowerReceiver;

    public ItemCoin itemCoin, itemSmallCoinStack, itemLargeCoinStack, itemSmallCoinBag, itemLargeCoinBag;
    public Item itemCard, itemEnderCard, itemSeller, itemVendorWrench, itemAdvSign, itemLinkCard, itemPackage;

    public CreativeTabs tabUniversalCoin = new CreativeTabs("tabUniversalCoins")
    {
        @Override
        public Item getTabIconItem()
        {
            return Item.getItemFromBlock(blockTradeStation);
        }
    };
    public ItemCoin[] coins;
    ConfigLoader configs;
    public int fourMatchPayout = 100, fiveMatchPayout = 10000;
    public boolean forcePayoutConfig, forcePackagePriceConfig;
    public int smallPackagePrice = 10, medPackagePrice= 20, largePackagePrice = 40;
    public boolean enderDepositFromMachine, enderDepositFromTransaction, enderDepositFromInventory, enderCheckBalance;
    public boolean cardCheckBalance, cardDepositFromTransaction;
    public Achievement achievementCoin, achievementThousand, achievementMillion, achievementBillion, achievementMaxed;
    public AchievementPage achievementPage;

    class ConfigLoader
    {
        Configuration source;
        boolean recipeTradeStation, recipeVendor, recipeVendorFrame,
        recipeCardMachine, recipeSlots, recipeLinkCard, recipeSignal,
        recipePackager, recipeEnderCard, recipeAdvSign, recipeBase,
        recipeSafe, recipePowerBase, recipePowerReceiver, recipeSeller;
        boolean signAnyMaterial, vendorFrameAnyMaterial, changeSignMaterial, changeVendorFrameMaterial;
        Set<String> signNonWoodMaterials, signNonWoodDictionaries,
            vendorFrameNonWoodMaterials, vendorFrameNonWoodDictionaries;

        int mobDropMax, mobDropChance, enderDragonMultiplier, mineshaftCoinChance, dungeonCoinChance;
        boolean coinsInMineshaft, coinsInDungeon, mobsDropCoins;

        int chestCoin, chestMinStack, chestMaxStack;

        int databaseType;
        String sqlUrl;
        String sqlUser;
        String sqlPasswd;

        ConfigLoader(Configuration source){ this.source = source; }

        void load() throws SQLException
        {
            source.load();

            // Ender Card
            String category = "Ender Card";
            Property prop = source.get(category, "Deposit from Machines", true);
            prop.comment = "Set to true to allow depositing to ender cards directly from any machine.";
            enderDepositFromMachine = prop.getBoolean(true);

            prop = source.get(category, "Deposit from Transactions", true);
            prop.comment = "Set to true to allow ender cards to accept coins received from purchases on vendor blocks.";
            enderDepositFromTransaction = prop.getBoolean(true);

            prop = source.get(category, "Deposit from Inventory", true);
            prop.comment = "Set to true to deposit all coins from the player inventory to the ender card on right click.";
            enderDepositFromInventory = prop.getBoolean(true);

            prop = source.get(category, "Check Balance", true);
            prop.comment = "Set to true to show the account balance on right click";
            enderCheckBalance = prop.getBoolean(true);

            // Normal Card
            category = "Normal Card";
            prop = source.get(category, "Deposit from Transactions", false);
            prop.comment = "Set to true to allow normal cards to accept coins received from purchases on vendor blocks.";
            cardDepositFromTransaction = prop.getBoolean(false);

            prop = source.get(category, "Check Balance", true);
            prop.comment = "Set to true to show the account balance on right click";
            cardCheckBalance = prop.getBoolean(true);

            // Recipes
            category = "Recipes";
            prop = source.get(category, "Trade Station Recipes", true);
            prop.comment = "Set to false to disable crafting recipes for trade station.";
            recipeTradeStation = prop.getBoolean(true);

            prop = source.get(category, "Seller Recipes", true);
            prop.comment = "Set to false to disable crafting recipes for selling catalog.";
            recipeSeller = prop.getBoolean(true);

            prop = source.get(category, "Vending Block Recipes", true);
            prop.comment = "Set to false to disable crafting recipes for vending blocks.";
            recipeVendor = prop.getBoolean(true);

            prop = source.get(category, "Vending Frame Recipe", true);
            prop.comment = "Set to false to disable crafting recipes for Vending Frame.";
            recipeVendorFrame = prop.getBoolean(true);

            prop = source.get(category, "ATM Recipe", true);
            prop.comment = "Set to false to disable crafting recipes for ATM.";
            recipeCardMachine = prop.getBoolean(true);

            prop = source.get(category, "Ender Card Recipe", true);
            prop.comment = "Set to false to disable crafting recipes for Ender Card.";
            recipeEnderCard = prop.getBoolean(true);

            prop = source.get(category, "Slot Machine Recipe", true);
            prop.comment = "Set to false to disable crafting recipes for Slot Machine.";
            recipeSlots = prop.getBoolean(true);

            prop = source.get(category, "Redstone Signal Generator Recipe", true);
            prop.comment = "Set to false to disable crafting recipes for Redstone Signal Generator.";
            recipeSignal = prop.getBoolean(true);

            prop = source.get(category, "Remote Storage Linking Card Recipe", true);
            prop.comment = "Set to false to disable crafting recipes for Linking Card.";
            recipeLinkCard = prop.getBoolean(true);

            prop = source.get(category, "Packager Recipe", true);
            prop.comment = "Set to false to disable crafting recipes for Packager.";
            recipePackager = prop.getBoolean(true);

            prop = source.get(category, "Advanced Sign Recipe", true);
            prop.comment = "Set to false to disable crafting recipes for Advanced Sign.";
            recipeAdvSign = prop.getBoolean(true);

            prop = source.get(category, "Block Base Recipe", true);
            prop.comment = "Set to false to disable crafting recipes for Block Base.";
            recipeBase = prop.getBoolean(true);

            prop = source.get(category, "Block Safe Recipe", true);
            prop.comment = "Set to false to disable crafting recipe for Block Safe.";
            recipeSafe = prop.getBoolean(true);

            // advanced sign materials
            category = "Advanced Sign Materials";
            prop = source.get(category, "Allow Material Changing", true);
            prop.comment = "Set to true to enable recipes for changing the advanced sign material. Default: true";
            changeSignMaterial = prop.getBoolean(true);

            prop = source.get(category, "Accept Anything", false);
            prop.comment = "Set to true to allow advanced signs to be created with any material. Default: false";
            signAnyMaterial = prop.getBoolean(false);

            prop = source.get(category, "Accepted Materials", new String[0]);
            prop.comment = "List of allowed materials for advanced signs.\n" +
                    "This will allow signs to be crafted with different blocks to have different texture.\n" +
                    "Example: minecraft:redstone_block to allow creation of redstone advanced signs\n" +
                    "Default: empty";
            String[] stringList = prop.getStringList();
            signNonWoodMaterials = stringList==null||stringList.length==0?null:new HashSet<>(Arrays.asList(stringList));

            prop = source.get(category, "Accepted Dictionary Materials", new String[0]);
            prop.comment = "List of allowed ore dictionary entries for advanced signs.\n" +
                    "This will allow signs to be crafted with different blocks to have different texture.\n" +
                    "Example: blockCopper to allow creation of advanced signs using any type of copper block\n" +
                    "Default: empty";
            stringList = prop.getStringList();
            signNonWoodDictionaries = stringList==null||stringList.length==0?null:new HashSet<>(Arrays.asList(stringList));

            // vendor frame materials
            category = "Vendor Frame Materials";
            prop = source.get(category, "Allow Material Changing", true);
            prop.comment = "Set to true to enable recipes for changing the vendor frame material. Default: true";
            changeVendorFrameMaterial = prop.getBoolean(true);

            prop = source.get(category, "Accept Anything", false);
            prop.comment = "Set to true to allow vendor frames to be created with any material. Default: false";
            vendorFrameAnyMaterial = prop.getBoolean(false);

            prop = source.get(category, "Accepted Materials", new String[0]);
            prop.comment = "List of allowed materials for vendor frames.\n" +
                    "This will allow vendor frames to be crafted with different blocks to have different texture.\n" +
                    "Example: minecraft:redstone_block to allow creation of redstone vendor frames\n" +
                    "Default: empty";
            stringList = prop.getStringList();
            vendorFrameNonWoodMaterials = stringList==null||stringList.length==0?null:new HashSet<>(Arrays.asList(stringList));

            prop = source.get(category, "Accepted Dictionary Materials", new String[0]);
            prop.comment = "List of allowed ore dictionary entries for vendor frames.\n" +
                    "This will allow vendor frames to be crafted with different blocks to have different texture.\n" +
                    "Example: blockCopper to allow creation of vendor frames using any type of copper block\n" +
                    "Default: empty";
            stringList = prop.getStringList();
            vendorFrameNonWoodDictionaries = stringList==null||stringList.length==0?null:new HashSet<>(Arrays.asList(stringList));

            // rf utility (power company stuff)
            prop = source.get("RF Utility", "Power Base enabled", true);
            prop.comment = "Set to false to disable the power base block.";
            recipePowerBase = prop.getBoolean(true);

            prop = source.get("RF Utility", "RF Blocks enabled", true);
            prop.comment = "Set to false to disable the power receiver block.";
            recipePowerReceiver = prop.getBoolean(true);

            prop = source.get("RF Utility", "Wholesale rate", 12);
            prop.comment = "Set payment per 10 kRF of power sold. Default: 12";

            prop = source.get("RF Utility", "Retail rate", 15);
            prop.comment = "Set payment per 10 kRF of power bought. Default: 15";

            // slot machine
            category = "Slot Machine";
            prop = this.source.get(category, "Four of a kind payout", 100);
            prop.comment = "Set payout of slot machine when four of a kind is spun. Default: 100";
            fourMatchPayout = Math.max(0, prop.getInt(100));

            prop = this.source.get(category, "Five of a kind payout", 10000);
            prop.comment = "Set payout of slot machine when five of a kind is spun. Default: 10000";
            fiveMatchPayout = Math.max(0, prop.getInt(10000));

            prop = source.get(category, "Force payout config", false);
            prop.comment = "Set to true to force this configuration on all slot machines and disable per block configuration. Default: false";
            forcePayoutConfig = prop.getBoolean(false);

            // packager
            prop = this.source.get("Packager", "Small Package Price", 10);
            prop.comment = "Set the price of small package";
            smallPackagePrice = Math.max(1, prop.getInt(10));

            prop = this.source.get("Packager", "Medium Package Price", 20);
            prop.comment = "Set the price of medium package";
            medPackagePrice = Math.max(1, prop.getInt(20));

            prop = this.source.get("Packager", "Large Package Price", 40);
            prop.comment = "Set the price of large package";
            largePackagePrice = Math.max(1, prop.getInt(40));

            prop = source.get(category, "Force prices config", false);
            prop.comment = "Set to true to force this configuration on all packager machines and disable per block configuration. Default: false";
            forcePackagePriceConfig = prop.getBoolean(false);

            // loot
            category = "Loot";
            prop = source.get(category, "Mob Drops", true);
            prop.comment = "Set to false to disable mobs dropping coins on death.";
            mobsDropCoins = prop.getBoolean(true);

            prop = source.get(category, "Mob Drop Max", 39);
            prop.comment = "Max mob drop stacksize. Minimum 1. Maximum 200. Default 39.";
            mobDropMax = Math.max(1, Math.min(prop.getInt(39), 200));

            prop = source.get(category, "Mob Drop Chance", 3);
            prop.comment = "Chance of a mob dropping coins. Lower number means higher chance. Minimum 0 (always drop). Default 3 (1 in 4 chance).";
            mobDropChance = Math.max(0, Math.min(prop.getInt(3), 100));

            prop = source.get(category, "Ender Dragon Multiplier", 1000);
            prop.comment = "Drop multiplier for ender dragon kills. Minimum 1. Default 1,000. Max 100,000";
            enderDragonMultiplier = Math.max(1, Math.min(prop.getInt(1000), 100000));

            prop = source.get(category, "Mineshaft Coins", true);
            prop.comment = "Set to false to disable coins spawning in mineshaft chests.";
            coinsInMineshaft = prop.getBoolean(true);

            prop = source.get(category, "Mineshaft Coins Spawnrate", 20);
            prop.comment = "Rate of coins spawning in mineshaft chests. Higher value equals more common. Default is 20.";
            mineshaftCoinChance = Math.max(1, Math.min(prop.getInt(20), 100));

            prop = source.get(category, "Dungeon Coins", true);
            prop.comment = "Set to false to disable coins spawning in dungeon chests.";
            coinsInDungeon = prop.getBoolean(true);

            prop = source.get(category, "Dungeon Coins Spawnrate", 20);
            prop.comment = "Rate of coins spawning in dungeon chests. Higher value equals more common. Default is 20.";
            dungeonCoinChance = Math.max(1, Math.min(prop.getInt(20), 100));

            prop = source.get(category, "Chest Coin Type", 2);
            prop.comment = "Item spawned on chests. 1 = coins, 2 = small stack, 3 = large stack, 4 = small bag, 5 = large bag";
            chestCoin = Math.max(1, Math.min(prop.getInt(2), 5));

            prop = source.get(category, "Chest Coin Stack Minimum", 1);
            prop.comment = "The minimum stack size spawned on the chest";
            chestMinStack = Math.max(1, Math.min(prop.getInt(1), 64));

            prop = source.get(category, "Chest Coin Stack Maximum", 2);
            prop.comment = "The maximum stack size spawned on the chest";
            chestMaxStack = Math.max(1, Math.min(prop.getInt(64), 64));

            // Database
            category = "Database";
            prop = source.get(category, "Database Type", 1);
            prop.comment = "Defines how the bank accounts and transactions will be stored\n\n1: properties - A simple file-based implementation that " +
                    "saves the data as raw text. Simple but not reliable.\n" +
                    "2: sql - Uses an external database software like MySQL or an SQL library like SQLite. (IMPORTANT: The tables aren't created automatically on this version)\n" +
                    "3: nbt - Stores data using NBT Keys on world data. This type has limited functionality and is not recommended, use it for compatibility with data from the original mod";
            databaseType = prop.getInt(1);

            prop = source.get(category, "SQL URL", "jdbc:mysql://localhost:3306/database_name?autoReconnect=true");
            prop.comment = "The URL for the SQL server";
            sqlUrl = prop.getString();

            prop = source.get(category, "SQL User", "user");
            prop.comment="The username used to connect to the SQL Server.";
            sqlUser = prop.getString();

            prop = source.get(category, "SQL Password", "password");
            prop.comment="The password used to connect to the SQL Server.";
            sqlPasswd = prop.getString();

            this.source.save();
        }

        public void initConnection() throws ClassNotFoundException, SQLException
        {
            if(databaseType == 1)
                UniversalCoinsServer.cardDb = new SqlDB(DriverManager.getConnection(sqlUrl, sqlUser, sqlPasswd));
            else if(databaseType == 3)
                UniversalCoinsServer.cardDb = new NbtDB();

            sqlUser = null;
            sqlPasswd = null;
        }
    }

    public void registerBlocks()
    {
        GameRegistry.registerBlock(blockTradeStation = new BlockTradeStation(tabUniversalCoin), "blockTradeStation");
        GameRegistry.registerBlock(blockVendor = new BlockVendor(tabUniversalCoin), ItemBlockVendor.class, "blockVendor");
        GameRegistry.registerBlock(blockVendorFrame = new BlockVendorFrame(tabUniversalCoin), "blockVendorFrame");
        GameRegistry.registerBlock(blockCardStation = new BlockCardStation(tabUniversalCoin), "blockCardStation");
        GameRegistry.registerBlock(blockBase = new BlockBase(tabUniversalCoin), "blockBase");
        GameRegistry.registerBlock(blockSafe = new BlockSafe(tabUniversalCoin), "blockSafe");
        GameRegistry.registerBlock(blockStandingAdvSign = new BlockAdvSign(true), "standing_ucsign");
        GameRegistry.registerBlock(blockWallAdvSign = new BlockAdvSign(false), "wall_ucsign");
        GameRegistry.registerBlock(blockSlots = new BlockSlots(tabUniversalCoin), "blockBandit");
        GameRegistry.registerBlock(blockSignal = new BlockSignal(tabUniversalCoin), "blockSignal");
        GameRegistry.registerBlock(blockPackager = new BlockPackager(tabUniversalCoin), "blockPackager");
        GameRegistry.registerBlock(blockPowerBase = new BlockPowerBase(tabUniversalCoin), "blockPowerBase");
        GameRegistry.registerBlock(blockPowerReceiver = new BlockPowerReceiver(tabUniversalCoin), "blockPowerReceiver");
    }

    public void registerItems()
    {
        itemCoin = (ItemCoin) new ItemCoin(1).setUnlocalizedName("itemCoin").setCreativeTab(tabUniversalCoin);
        itemSmallCoinStack = (ItemCoin) new ItemCoin(9).setUnlocalizedName("itemSmallCoinStack").setCreativeTab(tabUniversalCoin);
        itemLargeCoinStack = (ItemCoin) new ItemCoin(9*9).setUnlocalizedName("itemLargeCoinStack").setCreativeTab(tabUniversalCoin);
        itemSmallCoinBag = (ItemCoin) new ItemCoin(9*9*9).setUnlocalizedName("itemSmallCoinBag").setCreativeTab(tabUniversalCoin);
        itemLargeCoinBag = (ItemCoin) new ItemCoin(9*9*9*9).setUnlocalizedName("itemLargeCoinBag").setCreativeTab(tabUniversalCoin);
        itemCard = new ItemCard(tabUniversalCoin);
        itemEnderCard = new ItemEnderCard(tabUniversalCoin);
        itemSeller = new Item().setUnlocalizedName("itemSeller").setCreativeTab(tabUniversalCoin).setMaxStackSize(1);
        itemVendorWrench = new ItemVendorWrench(tabUniversalCoin);
        itemAdvSign = new ItemAdvSign(tabUniversalCoin);
        itemLinkCard = new ItemLinkCard(tabUniversalCoin);
        itemPackage = new ItemPackage(tabUniversalCoin);

        coins = new ItemCoin[]{itemCoin, itemSmallCoinStack, itemLargeCoinStack, itemSmallCoinBag, itemLargeCoinBag};

        GameRegistry.registerItem(itemCoin, itemCoin.getUnlocalizedName());
        GameRegistry.registerItem(itemSmallCoinStack, itemSmallCoinStack.getUnlocalizedName());
        GameRegistry.registerItem(itemLargeCoinStack, itemLargeCoinStack.getUnlocalizedName());
        GameRegistry.registerItem(itemSmallCoinBag, itemSmallCoinBag.getUnlocalizedName());
        GameRegistry.registerItem(itemLargeCoinBag, itemLargeCoinBag.getUnlocalizedName());
        GameRegistry.registerItem(itemCard, itemCard.getUnlocalizedName());
        GameRegistry.registerItem(itemEnderCard, itemEnderCard.getUnlocalizedName());
        GameRegistry.registerItem(itemSeller, itemSeller.getUnlocalizedName());
        GameRegistry.registerItem(itemVendorWrench, itemVendorWrench.getUnlocalizedName());
        GameRegistry.registerItem(itemAdvSign, itemAdvSign.getUnlocalizedName());
        GameRegistry.registerItem(itemLinkCard, itemLinkCard.getUnlocalizedName());
        GameRegistry.registerItem(itemPackage, itemPackage.getUnlocalizedName());
    }

    public void registerTiles()
    {
        GameRegistry.registerTileEntity(TileTradeStation.class, "TileTradeStation");
        GameRegistry.registerTileEntity(TileVendor.class, "TileVendorBlock");
        GameRegistry.registerTileEntity(TileVendorFrame.class, "TileVendorFrame");
        GameRegistry.registerTileEntity(TileCardStation.class, "TileCardStation");
        GameRegistry.registerTileEntity(TileSafe.class, "TileSafe");
        GameRegistry.registerTileEntity(TileAdvSign.class, "TileUCSign");
        GameRegistry.registerTileEntity(TileSlots.class, "TileBandit");
        GameRegistry.registerTileEntity(TileSignal.class, "TileSignal");
        GameRegistry.registerTileEntity(TilePackager.class, "TilePackager");
        GameRegistry.registerTileEntity(TilePowerBase.class, "TilePowerBase");
        GameRegistry.registerTileEntity(TilePowerReceiver.class, "TilePowerReceiver");
    }

    public void registerGuis()
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(UniversalCoinsServer.instance, new GuiHandler());
    }

    public void registerAchievements()
    {
        achievementCoin = new Achievement("achievement.coin", "AchievementCoin", 0, 0, itemCoin, null);
        achievementThousand = new Achievement("achievement.thousand", "AchievementThousand", 2, 0, itemSmallCoinStack, achievementCoin);
        achievementMillion = new Achievement("achievement.million", "AchievementMillion", 4, 0, itemLargeCoinStack, achievementThousand);
        achievementBillion = new Achievement("achievement.billion", "AchievementBillion", 6, 0, itemSmallCoinBag, achievementMillion);
        achievementMaxed = new Achievement("achievement.maxed", "AchievementMaxed", 8, 0, itemLargeCoinBag, achievementBillion);
        achievementPage = new AchievementPage("Universal Coins", achievementCoin, achievementThousand, achievementMillion, achievementBillion, achievementMaxed);

        AchievementPage.registerAchievementPage(achievementPage);
        achievementCoin.registerStat();
        achievementThousand.registerStat();
        achievementMillion.registerStat();
        achievementBillion.registerStat();
        achievementMaxed.registerStat();
    }

    public void registerRecipes()
    {
        ItemStack seller = new ItemStack(itemSeller);
        ItemStack coin = new ItemStack(itemCoin);
        ItemStack smallStack = new ItemStack(itemSmallCoinStack);
        ItemStack largeStack = new ItemStack(itemLargeCoinStack);
        ItemStack smallBag = new ItemStack(itemSmallCoinBag);
        ItemStack largeBag = new ItemStack(itemLargeCoinBag);

        GameRegistry.addShapelessRecipe(new ItemStack(itemCoin, 9), smallStack );
        GameRegistry.addShapelessRecipe(new ItemStack(itemSmallCoinStack, 9), largeStack );
        GameRegistry.addShapelessRecipe(new ItemStack(itemLargeCoinStack, 9), smallBag );
        GameRegistry.addShapelessRecipe(new ItemStack(itemSmallCoinBag, 9), largeBag );

        GameRegistry.addShapelessRecipe(smallStack, coin, coin, coin, coin, coin, coin, coin, coin, coin );
        GameRegistry.addShapelessRecipe(largeStack, smallStack, smallStack, smallStack, smallStack, smallStack, smallStack, smallStack, smallStack, smallStack);
        GameRegistry.addShapelessRecipe(smallBag, largeStack, largeStack, largeStack, largeStack, largeStack, largeStack, largeStack, largeStack, largeStack );
        GameRegistry.addShapelessRecipe(largeBag, smallBag, smallBag, smallBag, smallBag, smallBag, smallBag, smallBag, smallBag, smallBag );

        if(configs.recipeSeller)
            GameRegistry.addShapedRecipe(seller,
                    "LGE",
                    "PPP",
                    'L', Items.leather,
                    'G', Items.gold_ingot,
                    'E', Items.ender_pearl,
                    'P', Items.paper
            );

        if(configs.recipeTradeStation)
            GameRegistry.addShapedRecipe(new ItemStack(blockTradeStation),
                    "IGI",
                    "ICI",
                    "III",
                    'I', Items.iron_ingot,
                    'G', Items.gold_ingot,
                    'C', itemSeller
            );

        if(configs.recipeVendor)
            for (int i = 0; i < supports.length; i++)
                GameRegistry.addShapedRecipe(new ItemStack(blockVendor, 1, i),
                        "XXX",
                        "XRX",
                        "*G*",
                        'X', Blocks.glass,
                        'G', Items.gold_ingot,
                        'R', Items.redstone,
                        '*', reagents[i]
                );

        if(configs.recipeVendorFrame)
        {
            GameRegistry.addRecipe(new RecipeVendingFrame());
            RecipeSorter.register("universalcoins:vendingframe", RecipeVendingFrame.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        }

        if(configs.recipeAdvSign)
            GameRegistry.addShapelessRecipe(new ItemStack(itemAdvSign), new ItemStack(Items.sign));

        GameRegistry.addShapelessRecipe(new ItemStack(Items.sign), new ItemStack(itemAdvSign));

        if(configs.recipeCardMachine)
            GameRegistry.addShapedRecipe(new ItemStack(blockCardStation),
                    "III",
                    "ICI",
                    "III",
                    'I', Items.iron_ingot,
                    'C', itemSmallCoinBag
            );

        if(configs.recipeBase)
            GameRegistry.addShapedRecipe(new ItemStack(blockBase),
                    "III",
                    "ICI",
                    "III",
                    'I', Items.iron_ingot,
                    'C', itemCoin
            );

        if(configs.recipeSafe)
            GameRegistry.addShapedRecipe(new ItemStack(blockSafe),
                    "III",
                    "IEI",
                    "III",
                    'I', Items.iron_ingot,
                    'E', itemEnderCard
            );

        if(configs.recipeEnderCard)
        {
            GameRegistry.addRecipe(new RecipeEnderCard());
            RecipeSorter.register("universalcoins:endercard", RecipeEnderCard.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        }

        if(configs.recipeSlots)
            GameRegistry.addShapedRecipe(new ItemStack(blockSlots),
                    "IGI",
                    "IRI",
                    "III",
                    'I', Items.iron_ingot,
                    'R', Items.redstone,
                    'G', Items.gold_ingot
            );

        if(configs.recipeSignal)
            GameRegistry.addShapedRecipe(new ItemStack(blockSignal),
                    "IXI",
                    "XRX",
                    "IXI",
                    'I',
                    Items.iron_ingot,
                    'R',
                    Items.redstone
            );

        if(configs.recipeLinkCard)
            GameRegistry.addShapelessRecipe(new ItemStack(itemLinkCard), Items.paper, Items.paper, Items.ender_pearl);

        if(configs.recipePackager)
            GameRegistry.addShapedRecipe(new ItemStack(blockPackager),
                    "IPI",
                    "SRS",
                    "IRI",
                    'I', Items.iron_ingot,
                    'R', Items.redstone,
                    'S', Items.string,
                    'P', Items.paper
            );

        if(configs.changeSignMaterial)
            GameRegistry.addRecipe(new RecipePlankTextureChange(configs.signAnyMaterial, true, false, configs.signNonWoodMaterials, configs.signNonWoodDictionaries));

        if(configs.changeVendorFrameMaterial)
            GameRegistry.addRecipe(new RecipePlankTextureChange(configs.vendorFrameAnyMaterial, false, true, configs.vendorFrameNonWoodMaterials, configs.vendorFrameNonWoodDictionaries));

        if(configs.changeSignMaterial || configs.changeVendorFrameMaterial)
            RecipeSorter.register("universalcoins:plankchange", RecipePlankTextureChange.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");

        if(configs.recipePowerBase)
            GameRegistry.addShapedRecipe(new ItemStack(blockPowerBase),
                    "III",
                    "MRM",
                    "III",
                    'I', Items.iron_ingot,
                    'R', Blocks.redstone_block,
                    'M', Items.redstone
            );

        if(configs.recipePowerReceiver)
            GameRegistry.addShapedRecipe(new ItemStack(blockPowerReceiver),
                    "III",
                    "MRM",
                    "III",
                    'I', Items.iron_ingot,
                    'R', Blocks.redstone_block,
                    'M', new ItemStack(Items.dye, 1, 4)
            );

    }
}
