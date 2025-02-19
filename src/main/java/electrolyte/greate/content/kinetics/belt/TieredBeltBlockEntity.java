package electrolyte.greate.content.kinetics.belt;

import com.jozufozu.flywheel.light.LightListener;
import com.jozufozu.flywheel.light.LightUpdater;
import com.jozufozu.flywheel.util.box.GridAlignedBB;
import com.jozufozu.flywheel.util.box.ImmutableBox;
import com.simibubi.create.content.kinetics.belt.*;
import com.simibubi.create.content.kinetics.belt.transport.BeltMovementHandler;
import electrolyte.greate.content.kinetics.simpleRelays.ITieredKineticBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class TieredBeltBlockEntity extends BeltBlockEntity implements ITieredKineticBlockEntity {

    private int tier;

    @OnlyIn(Dist.CLIENT)
    public BeltLighter lighter;

    public TieredBeltBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.tier = ((TieredBeltBlock) state.getBlock()).getTier();
    }

    @Override
    public void tick() {
        if(beltLength == 0) {
            TieredBeltBlock.initBelt(level, worldPosition);
        }
        super.tick();
        if(!(level.getBlockState(worldPosition).getBlock() instanceof TieredBeltBlock)) return;
        initializeItemHandler();
        if(!isController()) return;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if(beltLength > 0 && lighter == null) {
                lighter = new BeltLighter();
            }
        });

        invalidateRenderBoundingBox();
        getInventory().tick();

        if(getSpeed() == 0) return;
        if(passengers == null) {
            passengers = new HashMap<>();
        }

        List<Entity> toRemove = new ArrayList<>();
        passengers.forEach((entity, info) -> {
            boolean canBeTransported = BeltMovementHandler.canBeTransported(entity);
            boolean leftTheBelt = info.getTicksSinceLastCollision() > ((getBlockState().getValue(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL) ? 3 : 1);
            if(!canBeTransported || leftTheBelt) {
                toRemove.add(entity);
                return;
            }
            info.tick();
            BeltMovementHandler.transportEntity(this, entity, info);
        });
        toRemove.forEach(passengers::remove);
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        compound.putInt("Tier", this.tier);
        super.write(compound, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        int prevBeltLength = beltLength;
        super.read(compound, clientPacket);
        this.tier = compound.getInt("Tier");
        beltLength = compound.getInt("Length");
        if(!wasMoved) {
            if(prevBeltLength != beltLength) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    if(lighter != null) {
                        lighter.initializeLight();
                    }
                });
            }
        }
    }

    @Override
    public boolean applyColor(DyeColor colorIn) {
        if(colorIn == null) {
            if(color.isEmpty()) return false;
        } else if(color.isPresent() && color.get() == colorIn) return false;
        if(level.isClientSide()) return true;

        for(BlockPos pos : TieredBeltBlock.getBeltChain(level, getController())) {
            BeltBlockEntity bbe = BeltHelper.getSegmentBE(level, pos);
            if(bbe == null) continue;
            bbe.color = Optional.ofNullable(colorIn);
            bbe.setChanged();
            bbe.sendData();
        }
        return true;
    }

    @Override
    public boolean hasPulley() {
        if(!(getBlockState().getBlock() instanceof TieredBeltBlock)) return false;
        return getBlockState().getValue(BeltBlock.PART) != BeltPart.MIDDLE;
    }

    @Override
    protected Direction getBeltFacing() {
        return super.getBeltFacing();
    }

    @OnlyIn(Dist.CLIENT)
    class BeltLighter implements LightListener {

        private byte[] light;

        public BeltLighter() {
            initializeLight();
            LightUpdater.get(level).addListener(this);
        }

        public int lightSegments() {
            return light == null ? 0 : light.length / 2;
        }

        public int getPackedLight(int segment) {
            return light == null ? 0 : LightTexture.pack(light[segment * 2], light[segment * 2 + 1]);
        }

        @Override
        public GridAlignedBB getVolume() {
            BlockPos endPos = BeltHelper.getPositionForOffset(TieredBeltBlockEntity.this, beltLength - 1);
            GridAlignedBB bb = GridAlignedBB.from(worldPosition, endPos);
            bb.fixMinMax();
            return bb;
        }

        @Override
        public boolean isListenerInvalid() {
            return remove;
        }

        @Override
        public void onLightUpdate(LightLayer type, ImmutableBox changed) {
            if(remove) return;
            if(level == null) return;
            GridAlignedBB beltVolume = getVolume();
            if(beltVolume.intersects(changed)) {
                if(type == LightLayer.BLOCK) updateBlockLight();
                if(type == LightLayer.SKY) updateSkyLight();
            }
        }

        private void initializeLight() {
            light = new byte[beltLength * 2];
            Vec3i vec = getBeltFacing().getNormal();
            BeltSlope slope = getBlockState().getValue(BeltBlock.SLOPE);
            int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
            MutableBlockPos pos = new MutableBlockPos(controller.getX(), controller.getY(), controller.getZ());
            for (int i = 0; i < beltLength * 2; i += 2) {
                light[i] = (byte) level.getBrightness(LightLayer.BLOCK, pos);
                light[i + 1] = (byte) level.getBrightness(LightLayer.SKY, pos);
                pos.move(vec.getX(), verticality, vec.getZ());
            }
        }

        private void updateBlockLight() {
            Vec3i vec = getBeltFacing().getNormal();
            BeltSlope slope = getBlockState().getValue(BeltBlock.SLOPE);
            int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
            MutableBlockPos pos = new MutableBlockPos(controller.getX(), controller.getY(), controller.getZ());
            for (int i = 0; i < beltLength * 2; i += 2) {
                light[i] = (byte) level.getBrightness(LightLayer.BLOCK, pos);
                pos.move(vec.getX(), verticality, vec.getZ());
            }
        }

        private void updateSkyLight() {
            Vec3i vec = getBeltFacing().getNormal();
            BeltSlope slope = getBlockState().getValue(BeltBlock.SLOPE);
            int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
            MutableBlockPos pos = new MutableBlockPos(controller.getX(), controller.getY(), controller.getZ());
            for (int i = 1; i < beltLength * 2; i += 2) {
                light[i] = (byte) level.getBrightness(LightLayer.SKY, pos);
                pos.move(vec.getX(), verticality, vec.getZ());
            }
        }
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return ITieredKineticBlockEntity.super.addToGoggleTooltip(tooltip, isPlayerSneaking, this.getTier(), capacity, stress);
    }

    @Override
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        super.updateFromNetwork(maxStress, currentStress, networkSize);
        notifyUpdate();
    }

}
