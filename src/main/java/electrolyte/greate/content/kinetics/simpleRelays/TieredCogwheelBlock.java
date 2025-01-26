package electrolyte.greate.content.kinetics.simpleRelays;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import electrolyte.greate.registry.ModBlockEntityTypes;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TieredCogwheelBlock extends CogWheelBlock implements ITieredBlock {

    private int tier;

    protected TieredCogwheelBlock(boolean large, Properties properties) {
        super(large, properties);
    }

    public static TieredCogwheelBlock small(Properties properties) {
        return new TieredCogwheelBlock(false, properties);
    }

    public static TieredCogwheelBlock large(Properties properties) {
        return new TieredCogwheelBlock(true, properties);
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public void setTier(int tier) {
        this.tier = tier;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return ModBlockEntityTypes.TIERED_BRACKETED_KINETIC.get();
    }
}
