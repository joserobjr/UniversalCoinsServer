package br.com.gamemods.universalcoinsserver;

import br.com.gamemods.universalcoinsserver.blocks.*;
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
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.RecipeSorter;

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

    public Configuration config;

    public Block blockTradeStation, blockVendor, blockVendorFrame, blockCardStation, blockBase, blockSafe,
            blockStandingAdvSign, blockWallAdvSign, blockSlots, blockSignal, blockPackager, blockPowerBase, blockPowerReceiver;

    public Item itemCoin, itemSmallCoinStack, itemLargeCoinStack, itemSmallCoinBag, itemLargeCoinBag, itemCard,
            itemEnderCard, itemSeller, itemVendorWrench, itemAdvSign, itemLinkCard, itemPackage;

    public CreativeTabs tabUniversalCoin = new CreativeTabs("tabUniversalCoins")
    {
        @Override
        public Item getTabIconItem()
        {
            return Item.getItemFromBlock(blockTradeStation);
        }
    };

    public void loadConfig()
    {
        config.load();
        config.save();
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
        itemCoin = new Item().setUnlocalizedName("itemCoin").setCreativeTab(tabUniversalCoin);
        itemSmallCoinStack = new Item().setUnlocalizedName("itemSmallCoinStack").setCreativeTab(tabUniversalCoin);
        itemLargeCoinStack = new Item().setUnlocalizedName("itemLargeCoinStack").setCreativeTab(tabUniversalCoin);
        itemSmallCoinBag = new Item().setUnlocalizedName("itemSmallCoinBag").setCreativeTab(tabUniversalCoin);
        itemLargeCoinBag = new Item().setUnlocalizedName("itemLargeCoinBag").setCreativeTab(tabUniversalCoin);
        itemCard = new ItemCard(tabUniversalCoin);
        itemEnderCard = new ItemEnderCard(tabUniversalCoin);
        itemSeller = new Item().setUnlocalizedName("itemSeller").setCreativeTab(tabUniversalCoin).setMaxStackSize(1);

        itemVendorWrench = new ItemVendorWrench(tabUniversalCoin);
        itemAdvSign = new ItemAdvSign(tabUniversalCoin);
        itemLinkCard = new ItemLinkCard(tabUniversalCoin);
        itemPackage = new ItemPackage(tabUniversalCoin);

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

        GameRegistry.addShapedRecipe(seller,
                "LGE",
                "PPP",
                'L', Items.leather,
                'G', Items.gold_ingot,
                'E', Items.ender_pearl,
                'P', Items.paper
        );

        GameRegistry.addShapedRecipe(new ItemStack(blockTradeStation),
                "IGI",
                "ICI",
                "III",
                'I', Items.iron_ingot,
                'G', Items.gold_ingot,
                'C', itemSeller
        );

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

        GameRegistry.addRecipe(new RecipeVendingFrame());
        RecipeSorter.register("universalcoins:vendingframe", RecipeVendingFrame.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");

        GameRegistry.addShapelessRecipe(new ItemStack(itemAdvSign), new ItemStack(Items.sign));
        GameRegistry.addShapelessRecipe(new ItemStack(Items.sign), new ItemStack(itemAdvSign));

        GameRegistry.addShapedRecipe(new ItemStack(blockCardStation),
                "III",
                "ICI",
                "III",
                'I', Items.iron_ingot,
                'C', itemSmallCoinBag
        );
        GameRegistry.addShapedRecipe(new ItemStack(blockBase),
                "III",
                "ICI",
                "III",
                'I', Items.iron_ingot,
                'C', itemCoin
        );

        GameRegistry.addShapedRecipe(new ItemStack(blockSafe),
                "III",
                "IEI",
                "III",
                'I', Items.iron_ingot,
                'E', itemEnderCard
        );

        GameRegistry.addRecipe(new RecipeEnderCard());
        RecipeSorter.register("universalcoins:endercard", RecipeEnderCard.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");

        GameRegistry.addShapedRecipe(new ItemStack(blockSlots),
                "IGI",
                "IRI",
                "III",
                'I', Items.iron_ingot,
                'R', Items.redstone,
                'G', Items.gold_ingot
        );

        GameRegistry.addShapedRecipe(new ItemStack(blockSignal),
                "IXI",
                "XRX",
                "IXI",
                'I',
                Items.iron_ingot,
                'R',
                Items.redstone
        );

        GameRegistry.addShapelessRecipe(new ItemStack(itemLinkCard), Items.paper, Items.paper, Items.ender_pearl);

        GameRegistry.addShapedRecipe(new ItemStack(blockPackager),
                "IPI",
                "SRS",
                "IRI",
                'I', Items.iron_ingot,
                'R', Items.redstone,
                'S', Items.string,
                'P', Items.paper
        );

        GameRegistry.addRecipe(new RecipePlankTextureChange());
        RecipeSorter.register("universalcoins:plankchange", RecipePlankTextureChange.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");

        GameRegistry.addShapedRecipe(new ItemStack(blockPowerBase),
                "III",
                "MRM",
                "III",
                'I', Items.iron_ingot,
                'R', Blocks.redstone_block,
                'M', Items.redstone
        );

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
