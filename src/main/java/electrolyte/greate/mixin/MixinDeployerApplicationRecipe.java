package electrolyte.greate.mixin;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeployerApplicationRecipe.class)
public abstract class MixinDeployerApplicationRecipe extends ItemApplicationRecipe implements IAssemblyRecipe {

    public MixinDeployerApplicationRecipe(AllRecipeTypes type, ProcessingRecipeParams params) {
        super(type, params);
    }

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "getDescriptionForAssembly", at = @At("HEAD"), remap = false, cancellable = true)
    private void greate_getDescriptionForAssembly(CallbackInfoReturnable<Component> cir) {
        ItemStack[] matchingStacks = ingredients.get(1).getItems();
        if(matchingStacks.length == 0) cir.setReturnValue(Components.literal("Invalid"));
        cir.setReturnValue(Lang.translateDirect("recipe.assembly.deploying_item",
                Components.translatable(matchingStacks[0].getHoverName().getString())));
    }
}
