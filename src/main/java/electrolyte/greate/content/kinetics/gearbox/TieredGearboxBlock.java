package electrolyte.greate.content.kinetics.gearbox;

import com.simibubi.create.content.kinetics.gearbox.GearboxBlock;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlockEntity;
import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;
import electrolyte.greate.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class TieredGearboxBlock extends GearboxBlock implements ITieredBlock {

    private int tier;

    public TieredGearboxBlock(Properties properties) {
        super(properties);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, Builder pBuilder) {
        if(pState.getValue(AXIS).isVertical()) {
            return super.getDrops(pState, pBuilder);
        }
        return List.of(TieredVerticalGearboxItem.MAP.get(this).getDefaultInstance());
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if(state.getValue(AXIS).isVertical()) {
            return super.getCloneItemStack(state, target, level, pos, player);
        }
        return TieredVerticalGearboxItem.MAP.get(this).getDefaultInstance();
    }

    @Override
    public BlockEntityType<? extends GearboxBlockEntity> getBlockEntityType() {
        return ModBlockEntityTypes.TIERED_GEARBOX.get();
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
