package electrolyte.greate.content.kinetics.fan.processing;

import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes.HauntingType;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes.SplashingType;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import electrolyte.greate.Greate;
import electrolyte.greate.content.kinetics.fan.processing.TieredHauntingRecipe.TieredHauntingWrapper;
import electrolyte.greate.content.kinetics.fan.processing.TieredSplashingRecipe.TieredSplashingWrapper;
import electrolyte.greate.foundation.recipe.TieredRecipeApplier;
import electrolyte.greate.registry.ModRecipeTypes;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GreateFanProcessingTypes {

    public static final TieredHauntingType TIERED_HAUNTING = register("haunting", new TieredHauntingType());
    public static final TieredSplashingType TIERED_SPLASHING = register("splashing", new TieredSplashingType());

    private static final Map<String, FanProcessingType> LEGACY_NAME_MAP;

    static {
        Object2ReferenceOpenHashMap<String, FanProcessingType> map = new Object2ReferenceOpenHashMap<>();
        map.put("TIERED_HAUNTING", TIERED_HAUNTING);
        map.put("TIERED_SPLASHING", TIERED_SPLASHING);
        map.trim();
        LEGACY_NAME_MAP = map;
    }

    private static <T extends FanProcessingType> T register(String id, T type) {
        FanProcessingTypeRegistry.register(Greate.id(id), type);
        return type;
    }

    @Nullable
    public static FanProcessingType ofLegacyName(String name) {
        return LEGACY_NAME_MAP.get(name);
    }

    public static FanProcessingType parseLegacy(String name) {
        FanProcessingType type = ofLegacyName(name);
        if(type != null) {
            return type;
        }
        return FanProcessingType.parse(name);
    }

    public static void register() {}

    public static class TieredHauntingType extends HauntingType {
        private static final TieredHauntingWrapper TIERED_HAUNTING_WRAPPER = new TieredHauntingWrapper();

        @Override
        public int getPriority() {
            return 350;
        }

        public boolean canProcess(ItemStack stack, Level level, int machineTier) {
            if(super.canProcess(stack, level)) return true;
            TIERED_HAUNTING_WRAPPER.setItem(0, stack);
            Optional<TieredHauntingRecipe> tieredRecipe = ModRecipeTypes.HAUNTING.find(TIERED_HAUNTING_WRAPPER, level, machineTier);
            return tieredRecipe.isPresent();
        }

        @Nullable
        public List<ItemStack> process(ItemStack stack, Level level, int machineTier) {
            List<ItemStack> result = super.process(stack, level);
            if(result != null) return result;
            TIERED_HAUNTING_WRAPPER.setItem(0, stack);
            Optional<TieredHauntingRecipe> tieredRecipe = ModRecipeTypes.HAUNTING.find(TIERED_HAUNTING_WRAPPER, level, machineTier);
            return tieredRecipe.map(tieredHauntingRecipe ->
                    TieredRecipeApplier.applyRecipeOn(level, stack, tieredHauntingRecipe, machineTier)).orElse(null);
        }
    }

    public static class TieredSplashingType extends SplashingType {
        private static final TieredSplashingWrapper TIERED_SPLASHING_WRAPPER = new TieredSplashingWrapper();

        @Override
        public int getPriority() {
            return 450;
        }

        public boolean canProcess(ItemStack stack, Level level, int machineTier) {
            if(super.canProcess(stack, level)) return true;
            TIERED_SPLASHING_WRAPPER.setItem(0, stack);
            Optional<TieredSplashingRecipe> tieredRecipe = ModRecipeTypes.SPLASHING.find(TIERED_SPLASHING_WRAPPER, level, machineTier);
            return tieredRecipe.isPresent();
        }

        @Nullable
        public List<ItemStack> process(ItemStack stack, Level level, int machineTier) {
            List<ItemStack> result = super.process(stack, level);
            if(result != null) return result;
            TIERED_SPLASHING_WRAPPER.setItem(0, stack);
            Optional<TieredSplashingRecipe> tieredRecipe = ModRecipeTypes.SPLASHING.find(TIERED_SPLASHING_WRAPPER, level, machineTier);
            return tieredRecipe.map(tieredSplashingRecipe ->
                    TieredRecipeApplier.applyRecipeOn(level, stack, tieredSplashingRecipe, machineTier)).orElse(null);
        }
    }
}
