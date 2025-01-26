package electrolyte.greate.foundation.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.utility.Components;
import electrolyte.greate.GreateValues;
import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;
import electrolyte.greate.infrastructure.config.GConfigUtility;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GreateKineticStats implements TooltipModifier {

    private final Block block;

    public GreateKineticStats(Block block) {
        this.block = block;
    }

    @Nullable
    public static GreateKineticStats create(Item item) {
        if(item instanceof BlockItem bi) {
            if(bi.getBlock() instanceof IRotate) {
                return new GreateKineticStats(bi.getBlock());
            }
        }
        return null;
    }

    @Override
    public void modify(ItemTooltipEvent context) {
        List<Component> kineticStats = getKineticStats(block, context.getEntity());
        if(!kineticStats.isEmpty()) {
            List<Component> tooltip = context.getToolTip();
            tooltip.add(Components.immutableEmpty());
            tooltip.addAll(kineticStats);
        }
    }

    public static List<Component> getKineticStats(Block block, Player player) {
        List<Component> stats = KineticStats.getKineticStats(block, player);
        if(block instanceof ITieredBlock tb) {
            stats.add(0, Component.translatable("greate.tooltip.max_capacity").append(Component.literal(String.valueOf(GConfigUtility.getMaxCapacityFromTier(tb.getTier()))).withStyle(Style.EMPTY.withColor(GTValues.VC[tb.getTier()])))
                    .append(" (")
                    .append(Component.literal(GreateValues.SNF[tb.getTier()]))
                    .append(")").withStyle(ChatFormatting.GRAY));
        }
        return stats;
    }
}
