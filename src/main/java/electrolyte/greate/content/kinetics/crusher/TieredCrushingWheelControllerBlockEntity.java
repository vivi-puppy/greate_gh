package electrolyte.greate.content.kinetics.crusher;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingInventory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.recipe.RecipeConditions;
import electrolyte.greate.foundation.data.recipe.TieredRecipeConditions;
import electrolyte.greate.foundation.recipe.TieredRecipeFinder;
import electrolyte.greate.foundation.recipe.TieredRecipeHelper;
import electrolyte.greate.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TieredCrushingWheelControllerBlockEntity extends CrushingWheelControllerBlockEntity {

    private final RecipeWrapper wrapper;
    private static final Object CRUSHING_RECIPES_CACHE_KEY = new Object();
    private int tier;

    public TieredCrushingWheelControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory = new ProcessingInventory(this::itemInserted) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return super.isItemValid(slot, stack) && processingEntity == null;
            }
        };
        wrapper = new RecipeWrapper(inventory);
        tier = ((TieredCrushingWheelControllerBlock) state.getBlock()).getTier();
    }

    private Optional<Recipe<?>> findValidRecipe() {
        return TieredRecipeFinder.findRecipe(CRUSHING_RECIPES_CACHE_KEY, level, wrapper,
                RecipeConditions.isOfType(GTRecipeTypes.MACERATOR_RECIPES, ModRecipeTypes.CRUSHING.getType(),
                        ModRecipeTypes.MILLING.getType(), AllRecipeTypes.MILLING.getType()).and(TieredRecipeConditions.firstIngredientMatches(wrapper.getItem(0))),
                TieredRecipeConditions.isEqualOrAboveTier(tier));
    }

    public void applyValidRecipe() {
        Optional<Recipe<?>> recipe = findValidRecipe();
        List<ItemStack> list = new ArrayList<>();
        if(recipe.isPresent()) {
            int rolls = inventory.getStackInSlot(0).getCount();
            inventory.clear();
            for(int roll = 0; roll < rolls; roll++) {
                List<ItemStack> rolledResults = TieredRecipeHelper.INSTANCE.getItemResults(recipe.get(), tier);
                for(ItemStack stack : rolledResults) {
                    ItemHelper.addToList(stack, list);
                }
            }
            for(int slot = 0; slot < list.size() && slot + 1 < inventory.getSlots(); slot++) {
                inventory.setStackInSlot(slot + 1, list.get(slot));
            }
        } else {
            inventory.clear();
        }
    }

    private void itemInserted(ItemStack stack) {
        Optional<Recipe<?>> recipe = findValidRecipe();
        int remainingTime = 100;
        if(recipe.isPresent()) {
            if(recipe.get() instanceof ProcessingRecipe<?> pr) {
                remainingTime = pr.getProcessingDuration();
            } else if(recipe.get() instanceof GTRecipe gtr) {
                remainingTime = gtr.duration;
            }
        }
        inventory.remainingTime = remainingTime;
        inventory.appliedRecipe = false;
    }

    private void intakeItem(ItemEntity itemEntity) {
        inventory.clear();
        inventory.setStackInSlot(0, itemEntity.getItem().copy());
        itemInserted(inventory.getStackInSlot(0));
        itemEntity.discard();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);
    }
}
