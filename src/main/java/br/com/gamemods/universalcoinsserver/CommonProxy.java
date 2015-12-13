package br.com.gamemods.universalcoinsserver;

import br.com.gamemods.universalcoinsserver.blocks.*;
import br.com.gamemods.universalcoinsserver.item.*;
import br.com.gamemods.universalcoinsserver.tile.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;

public class CommonProxy
{
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
}
