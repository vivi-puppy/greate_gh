package electrolyte.greate.content.kinetics.millstone;

import com.simibubi.create.content.kinetics.millstone.MillstoneBlock;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;
import electrolyte.greate.registry.ModBlockEntityTypes;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TieredMillstoneBlock extends MillstoneBlock implements ITieredBlock {

    private int tier;

    public TieredMillstoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends MillstoneBlockEntity> getBlockEntityType() {
        return ModBlockEntityTypes.TIERED_MILLSTONE.get();
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public void setTier(int tier) {
        this.tier = tier;
    }
}
