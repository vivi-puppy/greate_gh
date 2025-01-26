package electrolyte.greate.foundation.data.recipe.machine;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import electrolyte.greate.Greate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.screw;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.wireGtSingle;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;

public class GreateAssemblerRecipes {

    public static void register(Consumer<FinishedRecipe> provider) {

        ASSEMBLER_RECIPES
                .recipeBuilder(Greate.id("redstone_link"))
                .inputItems(AllBlocks.BRASS_CASING.asStack(), new ItemStack(Blocks.REDSTONE_TORCH))
                .outputItems(AllBlocks.REDSTONE_LINK)
                .duration(300)
                .EUt(VA[LV])
                .save(provider);

        ASSEMBLER_RECIPES
                .recipeBuilder(Greate.id("electron_tube"))
                .inputItems(GTItems.GLASS_TUBE.asStack())
                .inputItems(new UnificationEntry(wireGtSingle, Steel), 2)
                .outputItems(new ItemStack(AllItems.ELECTRON_TUBE, 2))
                .duration(180)
                .EUt(VA[ULV])
                .save(provider);

        ASSEMBLER_RECIPES
                .recipeBuilder(Greate.id("fluid_tank"))
                .inputItems(new UnificationEntry(screw, Copper), 2)
                .inputItems(GTMachines.WOODEN_DRUM)
                .inputFluids(Glass.getFluid(L * 2))
                .outputItems(AllBlocks.FLUID_TANK)
                .duration(100)
                .EUt(VA[ULV])
                .save(provider);

        ASSEMBLER_RECIPES
                .recipeBuilder(Greate.id("fluid_tank_annealed"))
                .inputItems(new UnificationEntry(screw, AnnealedCopper), 2)
                .inputItems(GTMachines.WOODEN_DRUM)
                .inputFluids(Glass.getFluid(L * 2))
                .outputItems(AllBlocks.FLUID_TANK, 2)
                .duration(100)
                .EUt(VA[ULV])
                .save(provider);
    }
}
