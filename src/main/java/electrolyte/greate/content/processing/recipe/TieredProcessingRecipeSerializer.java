package electrolyte.greate.content.processing.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import electrolyte.greate.content.processing.recipe.TieredProcessingRecipeBuilder.TieredProcessingRecipeFactory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TieredProcessingRecipeSerializer<T extends TieredProcessingRecipe<?>> implements RecipeSerializer<T> {

    private final TieredProcessingRecipeFactory<T> factory;

    public TieredProcessingRecipeSerializer(TieredProcessingRecipeFactory<T> factory) {
        this.factory = factory;
    }

    protected void writeToJson(JsonObject json, T recipe) {
        JsonArray jsonIngredients = new JsonArray();
        JsonArray jsonOutputs = new JsonArray();

        recipe.getIngredients().forEach(i -> jsonIngredients.add(i.toJson()));
        recipe.getFluidIngredients().forEach(i -> jsonIngredients.add(i.serialize()));

        recipe.getRollableResults().forEach(o -> jsonOutputs.add(o.serialize()));
        recipe.getFluidResults().forEach(o -> jsonOutputs.add(FluidHelper.serializeFluidStack(o)));

        json.add("ingredients", jsonIngredients);
        json.add("results", jsonOutputs);

        int processingDuration = recipe.getProcessingDuration();
        if (processingDuration > 0)
            json.addProperty("processingTime", processingDuration);

        HeatCondition requiredHeat = recipe.getRequiredHeat();
        if (requiredHeat != HeatCondition.NONE)
            json.addProperty("heatRequirement", requiredHeat.serialize());
        int recipeTier = recipe.getRecipeTier();
        json.addProperty("recipeTier", recipeTier);
        int circuitNumber = recipe.getCircuitNumber();
        json.addProperty("circuitNumber", circuitNumber);
        recipe.writeAdditional(json);
    }

    protected T readFromJson(ResourceLocation recipeId, JsonObject json) {
        TieredProcessingRecipeBuilder<T> builder = new TieredProcessingRecipeBuilder<>(factory, recipeId);
        NonNullList<Ingredient> ingredients = NonNullList.create();
        NonNullList<FluidIngredient> fluidIngredients = NonNullList.create();
        NonNullList<ProcessingOutput> results = NonNullList.create();
        NonNullList<FluidStack> fluidResults = NonNullList.create();

        for (JsonElement je : GsonHelper.getAsJsonArray(json, "ingredients")) {
            if (FluidIngredient.isFluidIngredient(je))
                fluidIngredients.add(FluidIngredient.deserialize(je));
            else
                ingredients.add(Ingredient.fromJson(je));
        }

        for (JsonElement je : GsonHelper.getAsJsonArray(json, "results")) {
            JsonObject jsonObject = je.getAsJsonObject();
            if (GsonHelper.isValidNode(jsonObject, "fluid"))
                fluidResults.add(FluidHelper.deserializeFluidStack(jsonObject));
            else
                results.add(TieredProcessingOutput.deserialize(je));
        }

        builder.withItemIngredients(ingredients)
                .withItemOutputs(results)
                .withFluidIngredients(fluidIngredients)
                .withFluidOutputs(fluidResults);

        if (GsonHelper.isValidNode(json, "processingTime"))
            builder.duration(GsonHelper.getAsInt(json, "processingTime"));
        if (GsonHelper.isValidNode(json, "heatRequirement"))
            builder.requiresHeat(HeatCondition.deserialize(GsonHelper.getAsString(json, "heatRequirement")));
        if (GsonHelper.isValidNode(json, "recipeTier"))
            builder.recipeTier(GsonHelper.getAsInt(json, "recipeTier"));
        if (GsonHelper.isValidNode(json, "circuitNumber"))
            builder.recipeCircuit(GsonHelper.getAsInt(json, "circuitNumber"));
        T recipe = builder.build();
        recipe.readAdditional(json);
        return recipe;
    }

    protected void writeToBuffer(FriendlyByteBuf buffer, T recipe) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        NonNullList<FluidIngredient> fluidIngredients = recipe.getFluidIngredients();
        NonNullList<ProcessingOutput> outputs = recipe.getRollableResults();
        NonNullList<FluidStack> fluidOutputs = recipe.getFluidResults();

        buffer.writeVarInt(ingredients.size());
        ingredients.forEach(i -> i.toNetwork(buffer));
        buffer.writeVarInt(fluidIngredients.size());
        fluidIngredients.forEach(i -> i.write(buffer));

        buffer.writeVarInt(outputs.size());
        outputs.forEach(o -> o.write(buffer));
        buffer.writeVarInt(fluidOutputs.size());
        fluidOutputs.forEach(o -> o.writeToPacket(buffer));

        buffer.writeVarInt(recipe.getProcessingDuration());
        buffer.writeVarInt(recipe.getRequiredHeat()
                .ordinal());
        buffer.writeVarInt(recipe.getRecipeTier());
        buffer.writeVarInt(recipe.getCircuitNumber());

        recipe.writeAdditional(buffer);
    }

    protected T readFromBuffer(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        NonNullList<FluidIngredient> fluidIngredients = NonNullList.create();
        NonNullList<ProcessingOutput> results = NonNullList.create();
        NonNullList<FluidStack> fluidResults = NonNullList.create();

        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            ingredients.add(Ingredient.fromNetwork(buffer));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            fluidIngredients.add(FluidIngredient.read(buffer));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            results.add(TieredProcessingOutput.read(buffer));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            fluidResults.add(FluidStack.readFromPacket(buffer));

        T recipe = new TieredProcessingRecipeBuilder<>(factory, recipeId).withItemIngredients(ingredients)
                .withItemOutputs(results)
                .withFluidIngredients(fluidIngredients)
                .withFluidOutputs(fluidResults)
                .duration(buffer.readVarInt())
                .requiresHeat(HeatCondition.values()[buffer.readVarInt()])
                .recipeTier(buffer.readVarInt())
                .recipeCircuit(buffer.readVarInt())
                .build();
        recipe.readAdditional(buffer);
        return recipe;
    }

    public final void write(JsonObject json, T recipe) {
        writeToJson(json, recipe);
    }

    @Override
    public final T fromJson(ResourceLocation id, JsonObject json) {
        return readFromJson(id, json);
    }

    @Override
    public final void toNetwork(FriendlyByteBuf buffer, T recipe) {
        writeToBuffer(buffer, recipe);
    }

    @Override
    public final T fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return readFromBuffer(id, buffer);
    }

    public TieredProcessingRecipeFactory<T> getFactory() {
        return factory;
    }
}
