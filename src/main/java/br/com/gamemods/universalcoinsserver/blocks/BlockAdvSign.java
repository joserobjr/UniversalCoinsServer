package br.com.gamemods.universalcoinsserver.blocks;

import br.com.gamemods.universalcoinsserver.tile.TileAdvSign;
import net.minecraft.block.BlockSign;

public class BlockAdvSign extends BlockSign
{
    public boolean standing;

    public BlockAdvSign(boolean standing) {
        super(TileAdvSign.class, standing);
        this.standing = standing;
        float f = 0.25F;
        float f1 = 1.0F;
        this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f1, 0.5F + f);
    }
}
