package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import br.com.gamemods.universalcoinsserver.tile.TileAdvSign;
import net.minecraft.block.BlockSign;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Random;

public class BlockAdvSign extends BlockSign
{
    public boolean standing;

    public BlockAdvSign(boolean standing)
    {
        super(TileAdvSign.class, standing);
        this.standing = standing;
        float f = 0.25F;
        float f1 = 1.0F;
        this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f1, 0.5F + f);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileAdvSign();
    }

    @Override
    public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_)
    {
        return UniversalCoinsServer.proxy.itemAdvSign;
    }
}
