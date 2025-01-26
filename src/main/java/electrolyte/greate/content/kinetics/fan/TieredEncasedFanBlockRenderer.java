package electrolyte.greate.content.kinetics.fan;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlock;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import static electrolyte.greate.registry.GreatePartialModels.FAN_INNER_MODELS;
import static electrolyte.greate.registry.GreatePartialModels.SHAFT_HALF_MODELS;

public class TieredEncasedFanBlockRenderer extends KineticBlockEntityRenderer<TieredEncasedFanBlockEntity> {
    public TieredEncasedFanBlockRenderer(Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(TieredEncasedFanBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        if(Backend.canUseInstancing(be.getLevel())) return;
        int tier = ((TieredEncasedFanBlock) be.getBlockState().getBlock()).getTier();
        Direction dir = be.getBlockState().getValue(EncasedFanBlock.FACING);
        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());
        int lightBehind = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(dir.getOpposite()));
        int lightFront = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(dir));
        SuperByteBuffer fanInner = CachedBufferer.partialFacing(FAN_INNER_MODELS[tier], be.getBlockState(), dir.getOpposite());
        SuperByteBuffer halfShaftModel = CachedBufferer.partialFacing(SHAFT_HALF_MODELS[tier], be.getBlockState(), dir.getOpposite());
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float speed = be.getSpeed() * 5;
        if(speed > 0) {
            speed = Mth.clamp(speed, 80, 64 * 20);
        }
        if(speed < 0) {
            speed = Mth.clamp(speed, -64 * 20, -80);
        }
        float angle = (time * speed * 3 / 10f) % 360;
        angle = angle / 180f * (float) Math.PI;

        standardKineticRotationTransform(halfShaftModel, be, lightBehind).renderInto(ms, vb);
        kineticRotationTransform(fanInner, be, dir.getAxis(), angle, lightFront).renderInto(ms, vb);
    }
}
