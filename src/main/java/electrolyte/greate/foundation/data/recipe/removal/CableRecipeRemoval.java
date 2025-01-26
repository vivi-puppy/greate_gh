package electrolyte.greate.foundation.data.recipe.removal;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.M;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.wireGtDouble;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.wireGtHex;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.wireGtOctal;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.wireGtQuadruple;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.wireGtSingle;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.resources.ResourceLocation;

public class CableRecipeRemoval {

        public static void disableAssemblerRecipes(Consumer<ResourceLocation> recipe) {
                wireGtSingle.executeHandler(null, PropertyKey.WIRE, (tagPrefix, material, property,
                                consumer) -> CableRecipeRemoval.removeRecipe(tagPrefix, material, property, recipe));
                wireGtDouble.executeHandler(null, PropertyKey.WIRE, (tagPrefix, material, property,
                                consumer) -> CableRecipeRemoval.removeRecipe(tagPrefix, material, property, recipe));
                wireGtQuadruple.executeHandler(null, PropertyKey.WIRE, (tagPrefix, material, property,
                                consumer) -> CableRecipeRemoval.removeRecipe(tagPrefix, material, property, recipe));
                wireGtOctal.executeHandler(null, PropertyKey.WIRE, (tagPrefix, material, property,
                                consumer) -> CableRecipeRemoval.removeRecipe(tagPrefix, material, property, recipe));
                wireGtHex.executeHandler(null, PropertyKey.WIRE,
                                (tagPrefix, material, property, consumer) -> CableRecipeRemoval
                                                .removeRecipe(tagPrefix, material, property, recipe));
        }

        public static void removeRecipe(TagPrefix wirePrefix, Material material, WireProperties property,
                        Consumer<ResourceLocation> recipe) {
                if (property.isSuperconductor())
                        return;
                int voltageTier = GTUtil.getTierByVoltage(property.getVoltage());
                int factor = (int) (wirePrefix.getMaterialAmount(material) * 2 / M);
        }
}
