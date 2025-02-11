package electrolyte.greate.foundation.recipe;

import java.util.ArrayList;
import java.util.List;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import electrolyte.greate.content.processing.recipe.TieredProcessingOutput;
import electrolyte.greate.content.processing.recipe.TieredProcessingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class TieredRecipeHelper {

    public static final TieredRecipeHelper INSTANCE = new TieredRecipeHelper();

    public int findDuration(Recipe<?> recipe) {
        if (recipe instanceof ProcessingRecipe<?> pr) {
            return pr.getProcessingDuration();
        } else if (recipe instanceof GTRecipe gtr) {
            return gtr.duration;
        }
        return 100;
    }

    public boolean firstIngredientMatches(Recipe<?> recipe, RecipeWrapper wrapper) {
        if (recipe instanceof ProcessingRecipe<?> pr) {
            return !pr.getIngredients().isEmpty()
                    && pr.getIngredients().get(0).getItems()[0].is(wrapper.getItem(0).getItem());
        } else if (recipe instanceof GTRecipe gtr) {
            List<Content> inputIngredients = gtr.getInputContents(ItemRecipeCapability.CAP);
            Ingredient ing = (Ingredient) inputIngredients.get(0).getContent();
            return !inputIngredients.isEmpty() && ing.test(wrapper.getItem(0));
        }
        return false;
    }

    public List<ItemStack> getItemResults(Recipe<?> recipe, int machineTier) {
        List<ProcessingOutput> newResults = new ArrayList<>();
        int recipeTier = 0;

        if (recipe instanceof TieredProcessingRecipe<?> tpr) {
            recipeTier = tpr.getRecipeTier();
            List<ProcessingOutput> oldResults = tpr.getRollableResults();
            for (ProcessingOutput oldResult : oldResults) {
                if (oldResult instanceof TieredProcessingOutput tpo) {
                    float chance = tpo.getChance();
                    float extraTierChance = 0;
                    if (machineTier >= 3) {
                        extraTierChance = getExtraPercent(tpo.getExtraTierChance(), recipeTier, machineTier);
                    }
                    newResults.add(new TieredProcessingOutput(tpo.getStack(), chance, extraTierChance));
                } else {
                    newResults.add(oldResult);
                }
            }
            return new ArrayList<>(tpr.rollResults(newResults));
        } else if (recipe instanceof ProcessingRecipe<?> pr) {
            List<ProcessingOutput> oldResults = pr.getRollableResults();
            for (ProcessingOutput oldResult : oldResults) {
                if (oldResult.getChance() < 1) {
                    continue;
                }
                newResults.add(oldResult);
            }
            return new ArrayList<>(pr.rollResults(newResults));
        } else if (recipe instanceof GTRecipe gtr) {
            List<Content> outputs = gtr.getOutputContents(ItemRecipeCapability.CAP);
            if (machineTier < 3) {
                // For lower tiers, only process the first output (main product) with 100%
                // chance
                if (!outputs.isEmpty()) {
                    Content mainOutput = outputs.get(0);
                    ItemStack[] items = ((Ingredient) mainOutput.content).getItems();
                    if (items.length > 0) {
                        newResults.add(new TieredProcessingOutput(items[0], 10000f, 0));
                    }
                }
            } else {
                // For stainless steel and above, process all outputs with their chances
                for (int outputIndex = 0; outputIndex < outputs.size(); outputIndex++) {
                    Content c = outputs.get(outputIndex);
                    ItemStack[] items = ((Ingredient) c.content).getItems();
                    for (ItemStack item : items) {
                        if (outputIndex == 0) {
                            // Main output always has 100% chance (10000 in GT's range)
                            newResults.add(new TieredProcessingOutput(item, 10000f, 0));
                        } else {
                            // Use GT chances directly without conversion
                            float chance = (float) c.chance;
                            if (chance <= 0) { // Skip if no chance
                                continue;
                            }
                            float tierBonus = (float) c.tierChanceBoost;
                            newResults.add(new TieredProcessingOutput(item, chance, tierBonus));
                        }
                    }
                }
            }
            return new ArrayList<>(getItemResults(newResults));
        }
        return List.of(ItemStack.EMPTY);
    }

    public List<FluidStack> getFluidResults(Recipe<?> recipe) {
        if (recipe instanceof ProcessingRecipe<?> pr) {
            return pr.getFluidResults();
        } else if (recipe instanceof GTRecipe gtr) {
            List<Content> contents = gtr.getOutputContents(FluidRecipeCapability.CAP);
            List<FluidStack> results = new ArrayList<>();
            for (Content c : contents) {
                com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient ing = (com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient) c
                        .getContent();
                results.add(new FluidStack(ing.getStacks()[0].getFluid(), (int) ing.getAmount()));
            }
            return results;
        }
        return List.of(FluidStack.EMPTY);
    }

    private float getExtraPercent(float baseExtraPercent, int recipeTier, int machineTier) {
        return baseExtraPercent * (machineTier - recipeTier);
    }

    private List<ItemStack> getItemResults(List<ProcessingOutput> rollableResults) {
        List<ItemStack> results = new ArrayList<>();
        for (ProcessingOutput output : rollableResults) {
            ItemStack stack = output.rollOutput();
            if (!stack.isEmpty()) {
                results.add(stack);
            }
        }
        return results;
    }
}
