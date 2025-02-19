package electrolyte.greate.content.kinetics.simpleRelays;

import com.simibubi.create.foundation.utility.Lang;
import electrolyte.greate.Greate;
import electrolyte.greate.infrastructure.config.GConfigUtility;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import java.util.List;

public interface ITieredKineticBlockEntity {

    default float getMaxCapacityFromBlock(Block block) {
        boolean tieredMachine = block instanceof ITieredBlock;
        if(tieredMachine) {
            int tier = ((ITieredBlock) block).getTier();
            if(tier != -1) {
                return GConfigUtility.getMaxCapacityFromTier(tier);
            }
        }
        return Integer.MAX_VALUE;
    }

    default boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, int tier, double capacity, double stress) {
        if(tier != -1) {
            if(!tooltip.isEmpty()) {
                Lang.builder().space();
            } else {
                Lang.translate("gui.goggles.kinetic_stats").forGoggles(tooltip);
            }
            Lang.builder(Greate.MOD_ID).translate("tooltip.capacity").style(ChatFormatting.GRAY).forGoggles(tooltip);
            Lang.builder(Greate.MOD_ID).add(Lang.number(GConfigUtility.getMaxCapacityFromTier(tier)).style(ChatFormatting.AQUA).space().add(Lang.text("su")).space().add(Lang.text("at current shaft tier").style(ChatFormatting.DARK_GRAY))).forGoggles(tooltip, 1);
            Lang.builder(Greate.MOD_ID).translate("tooltip.networkStatistics").style(ChatFormatting.GRAY).forGoggles(tooltip);
            Lang.number(stress).style(ChatFormatting.AQUA).add(Lang.text("su")).space().add(Lang.text("consumed").style(ChatFormatting.DARK_GRAY)).space().add(Lang.text("/").style(ChatFormatting.AQUA)).space().add(Lang.number(capacity).style(ChatFormatting.AQUA)).add(Lang.text("su").space().add(Lang.text("generated").style(ChatFormatting.DARK_GRAY))).forGoggles(tooltip, 1);
            return true;
        }
        return false;
    }
}
