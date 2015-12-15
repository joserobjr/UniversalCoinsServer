package br.com.gamemods.universalcoinsserver.datastore;

import br.com.gamemods.universalcoinsserver.blocks.PlayerOwned;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public class BlockOperator implements Operator
{
    private final int x, y, z, dim;
    private final String blockId;
    private final int blockMeta;
    private final UUID owner;

    public BlockOperator(int x, int y, int z, int dim, String blockId, int blockMeta, UUID owner)
    {
        this.blockId = blockId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
        this.blockMeta = blockMeta;
        this.owner = owner;
    }

    public BlockOperator(TileEntity tileEntity)
    {
        this.x = tileEntity.xCoord;
        this.y = tileEntity.yCoord;
        this.z = tileEntity.zCoord;
        this.dim = tileEntity.getWorldObj().provider.dimensionId;
        this.blockId = GameData.getBlockRegistry().getNameForObject(tileEntity.getBlockType());
        this.blockMeta = tileEntity.getBlockMetadata();
        if(tileEntity instanceof PlayerOwned)
            this.owner = ((PlayerOwned) tileEntity).getOwnerId();
        else
            this.owner = null;
    }

    public String getBlockId()
    {
        return blockId;
    }

    public int getBlockMeta()
    {
        return blockMeta;
    }

    public int getDim()
    {
        return dim;
    }

    public UUID getOwner()
    {
        return owner;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }

    @Override
    public String toString()
    {
        return "BlockOperator{" +
                "blockId='" + blockId + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", dim=" + dim +
                ", blockMeta=" + blockMeta +
                ", owner=" + owner +
                '}';
    }
}
