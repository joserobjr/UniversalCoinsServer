package br.com.gamemods.universalcoinsserver.datastore;

import br.com.gamemods.universalcoinsserver.blocks.PlayerOwned;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class BlockOperator implements Operator
{
    private final int x, y, z;
    @Nullable
    private final Integer dim;
    @Nullable
    private final String blockId;
    @Nullable
    private final Integer blockMeta;
    @Nullable
    private final UUID owner;

    public BlockOperator(int x, int y, int z, @Nullable Integer dim, @Nullable String blockId, @Nullable Integer blockMeta, @Nullable UUID owner)
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
        if(tileEntity.hasWorldObj())
        {
            this.dim = tileEntity.getWorldObj().provider.dimensionId;
            this.blockId = GameData.getBlockRegistry().getNameForObject(tileEntity.getBlockType());
            this.blockMeta = tileEntity.getBlockMetadata();
        }
        else
        {
            this.dim = null;
            this.blockId = null;
            this.blockMeta = null;
        }
        if(tileEntity instanceof PlayerOwned)
            this.owner = ((PlayerOwned) tileEntity).getOwnerId();
        else
            this.owner = null;
    }

    @Nullable
    public String getBlockId()
    {
        return blockId;
    }

    @Nullable
    public Integer getBlockMeta()
    {
        return blockMeta;
    }

    @Nullable
    public Integer getDim()
    {
        return dim;
    }

    @Nullable
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
