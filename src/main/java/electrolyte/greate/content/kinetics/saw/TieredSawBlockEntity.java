package electrolyte.greate.content.kinetics.saw;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.recipe.RecipeConditions;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;
import com.simibubi.create.infrastructure.config.AllConfigs;
import electrolyte.greate.Greate;
import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;
import electrolyte.greate.content.kinetics.simpleRelays.ITieredKineticBlockEntity;
import electrolyte.greate.foundation.data.recipe.TieredRecipeConditions;
import electrolyte.greate.foundation.recipe.TieredRecipeHelper;
import electrolyte.greate.mixin.MixinSawBlockEntityAccessor;
import electrolyte.greate.registry.ModRecipeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TieredSawBlockEntity extends SawBlockEntity implements ITieredKineticBlockEntity {

    private int tier;
    private SmartFluidTankBehaviour inputTank;
    private LazyOptional<IFluidHandler> fluidCapability;

    public TieredSawBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tier = ((ITieredBlock) state.getBlock()).getTier();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        inputTank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 1, 16000, false);
        behaviours.add(inputTank);

        fluidCapability = LazyOptional.of(() -> {
            LazyOptional<? extends IFluidHandler> inputCap = inputTank.getCapability();
            return new CombinedTankWrapper(inputCap.orElse(null));
        });
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        ITieredKineticBlockEntity.super.addToGoggleTooltip(tooltip, isPlayerSneaking, tier, capacity, stress);
        if(canProcess()) {
            IFluidHandler fluid = fluidCapability.orElse(new FluidTank(0));
            LangBuilder mb = Lang.translate("generic.unit.millibuckets");
            FluidStack fluidStack = fluid.getFluidInTank(0);
            if(!fluidStack.isEmpty()) {
                Lang.builder(Greate.MOD_ID).translate("gui.goggles.saw_contents").style(ChatFormatting.GRAY).forGoggles(tooltip);
                Lang.text("")
                        .add(Lang.fluidName(fluidStack)
                                .add(Lang.text(" ")).style(ChatFormatting.GRAY)
                                .add(Lang.number(fluidStack.getAmount()).add(mb).style(ChatFormatting.BLUE)))
                        .forGoggles(tooltip, 1);
            } else {
                tooltip.remove(0);
            }
        }
        return true;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if(fluidCapability != null) {
            fluidCapability.invalidate();
        }
    }

    public List<Recipe<?>> getValidRecipes() {
        TieredSawBlockEntity be = (TieredSawBlockEntity) level.getBlockEntity(this.getBlockPos());
        Optional<CuttingRecipe> assemblyRecipe = SequencedAssemblyRecipe.getRecipe(level, inventory.getStackInSlot(0), AllRecipeTypes.CUTTING.getType(), CuttingRecipe.class);
        Optional<TieredCuttingRecipe> tieredAssemblyRecipe = SequencedAssemblyRecipe.getRecipe(level, inventory.getStackInSlot(0), ModRecipeTypes.CUTTING.getType(), TieredCuttingRecipe.class);
        FilteringBehaviour filtering = ((MixinSawBlockEntityAccessor) this).getFilteringBehaviour();
        Object cuttingRecipesKey = ((MixinSawBlockEntityAccessor) this).getCuttingRecipesKey();
        if(assemblyRecipe.isPresent() && filtering.test(assemblyRecipe.get().getResultItem(level.registryAccess()))) {
            return ImmutableList.of(assemblyRecipe.get());
        }
        if(tieredAssemblyRecipe.isPresent() && filtering.test(tieredAssemblyRecipe.get().getResultItem(level.registryAccess()))) {
            Predicate<Recipe<?>> predicate = TieredRecipeConditions.isEqualOrAboveTier(tier);
            if(predicate.test(tieredAssemblyRecipe.get())) {
                return ImmutableList.of(tieredAssemblyRecipe.get());
            }
        }
        Predicate<Recipe<?>> recipeTypes = RecipeConditions.isOfType(AllRecipeTypes.CUTTING.getType(), ModRecipeTypes.CUTTING.getType(), GTRecipeTypes.CUTTER_RECIPES,
                AllConfigs.server().recipes.allowStonecuttingOnSaw.get() ? RecipeType.STONECUTTING : null,
                AllConfigs.server().recipes.allowWoodcuttingOnSaw.get() ? woodcuttingRecipeType.get() : null);
        List<Recipe<?>> startedSearch = RecipeFinder.get(cuttingRecipesKey, level, recipeTypes);
        IFluidHandler availableFluid = be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
        if(availableFluid == null) return List.of();
        return startedSearch.stream()
                .filter(TieredRecipeConditions.outputMatchesFilter(filtering))
                .filter(TieredRecipeConditions.firstIngredientMatches(inventory.getStackInSlot(0)))
                .filter(TieredRecipeConditions.firstFluidMatches(availableFluid.getFluidInTank(0)))
                .filter(TieredRecipeConditions.isEqualOrAboveTier(tier))
                .filter(r -> !AllRecipeTypes.shouldIgnoreInAutomation(r))
                .filter(r -> !ModRecipeTypes.shouldIgnoreInAutomation(r))
                .collect(Collectors.toList());
    }

    public void applyValidRecipe() {
        List<? extends Recipe<?>> recipes = getValidRecipes();
        int recipeIndex = ((MixinSawBlockEntityAccessor) this).getRecipeIndex();
        if(recipes.isEmpty()) return;
        if(recipeIndex >= recipes.size()) recipeIndex = 0;

        Recipe<?> recipe = recipes.get(recipeIndex);
        int rolls = inventory.getStackInSlot(0).getCount();
        IFluidHandler availableFluid = this.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
        if(availableFluid == null) return;
        inventory.clear();
        List<ItemStack> list = new ArrayList<>();
        for(int roll = 0; roll < rolls; roll++) {
            List<ItemStack> results = new LinkedList<>();
            if(recipe instanceof ProcessingRecipe<?> pr) {
                results = pr.rollResults();
                if(!pr.getFluidIngredients().isEmpty()) {
                    availableFluid.drain(pr.getFluidIngredients().get(0).getRequiredAmount(), FluidAction.EXECUTE);
                }
            } else if (recipe instanceof GTRecipe gtr) {
                results.addAll(TieredRecipeHelper.INSTANCE.getItemResults(gtr, tier));
                if(!gtr.getInputContents(FluidRecipeCapability.CAP).isEmpty()) {
                    Content c = gtr.getInputContents(FluidRecipeCapability.CAP).get(0);
                    availableFluid.drain((int) ((FluidIngredient) c.getContent()).getAmount(), FluidAction.EXECUTE);
                }
            } else if(recipe instanceof StonecutterRecipe || recipe.getType() == woodcuttingRecipeType.get()) {
                results.add(recipe.getResultItem(level.registryAccess()).copy());
            }

            for(ItemStack stack : results) {
                ItemHelper.addToList(stack, list);
            }
        }

        for(int slot = 0; slot < list.size() && slot + 1 < inventory.getSlots(); slot++) {
            inventory.setStackInSlot(slot + 1, list.get(slot));
        }
        award(AllAdvancements.SAW_PROCESSING);
    }

    @Override
    public void start(ItemStack inserted) {
        if(!canProcess()) return;
        if(inventory.isEmpty()) return;
        if(level.isClientSide && !isVirtual()) return;

        List<? extends Recipe<?>> recipes = getValidRecipes();
        boolean valid = !recipes.isEmpty();
        int time = 50;

        if(recipes.isEmpty()) {
            inventory.remainingTime = inventory.recipeDuration = 10;
            inventory.appliedRecipe = false;
            sendData();
            return;
        }

        int recipeIndex = ((MixinSawBlockEntityAccessor) this).getRecipeIndex();
        if(valid) {
            recipeIndex++;
            if(recipeIndex >= recipes.size()) {
                ((MixinSawBlockEntityAccessor) this).setRecipeIndex(0);
            }
        }

        Recipe<?> recipe = recipes.get(((MixinSawBlockEntityAccessor) this).getRecipeIndex());
        if(recipe instanceof ProcessingRecipe<?> pr) {
            time = pr.getProcessingDuration();
        } else if(recipe instanceof GTRecipe gtr) {
            time = gtr.duration;
        } else if(recipe instanceof CuttingRecipe cr) {
            time = cr.getProcessingDuration();
        }

        inventory.remainingTime = time * Math.max(1, (inserted.getCount()  / 5));
        inventory.recipeDuration = inventory.remainingTime;
        inventory.appliedRecipe = false;
        sendData();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if(cap == ForgeCapabilities.FLUID_HANDLER && side != Direction.DOWN) {
            return fluidCapability.cast();
        }
        return super.getCapability(cap, side);
    }
}
