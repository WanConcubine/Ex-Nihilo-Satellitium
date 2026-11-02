package com.kaifengcrt.exnihilosatellitium.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

/**
 * The 1.20.1 stand-in for NeoForge's {@code RecipeOutput} wrapper:
 *
 * <ul>
 *   <li>never emits an advancement — this mod ships no {@code advancement/recipes/**},
 *       and adding 133 of them would change recipe-book unlocks;</li>
 *   <li>can attach the bare {@code conditions} array that Forge's {@code RecipeManager}
 *       reads <em>before</em> parsing the recipe body (so a variant for an absent mod
 *       may point at items that do not exist).</li>
 * </ul>
 */
final class DatagenRecipe implements FinishedRecipe {

    private final ResourceLocation id;
    private final RecipeSerializer<?> type;
    private final JsonObject data;
    @Nullable
    private final JsonArray conditions;

    private DatagenRecipe(final ResourceLocation id, final RecipeSerializer<?> type,
                          final JsonObject data, @Nullable final JsonArray conditions) {
        this.id = id;
        this.type = type;
        this.data = data;
        this.conditions = conditions;
    }

    /** Wraps a builder result: captures its payload, drops its advancement. */
    static DatagenRecipe of(final FinishedRecipe original, @Nullable final JsonArray conditions) {
        final JsonObject data = new JsonObject();
        original.serializeRecipeData(data);
        return new DatagenRecipe(original.getId(), original.getType(), data, conditions);
    }

    /**
     * A cooking recipe written by hand: {@code SimpleCookingRecipeBuilder} would derive
     * {@code food}/{@code blocks} book categories from the result, while the shipped
     * JSON has always been {@code misc}.
     */
    static DatagenRecipe cooking(final ResourceLocation id, final RecipeSerializer<?> type,
                                 final Ingredient ingredient, final ItemStack result,
                                 final float experience, final int cookingTime,
                                 @Nullable final JsonArray conditions) {
        final JsonObject data = new JsonObject();
        data.addProperty("group", "");
        data.addProperty("category", CookingBookCategory.MISC.getSerializedName());
        data.add("ingredient", ingredient.toJson());
        data.add("result", itemStack(result));
        data.addProperty("experience", experience);
        data.addProperty("cookingtime", cookingTime);
        return new DatagenRecipe(id, type, data, conditions);
    }

    private static JsonObject itemStack(final ItemStack stack) {
        final JsonObject json = new JsonObject();
        json.addProperty("item", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
        if (stack.getCount() != 1) {
            json.addProperty("count", stack.getCount());
        }
        if (stack.hasTag()) {
            json.addProperty("tag", stack.getTag().toString());
        }
        return json;
    }

    @Override
    public void serializeRecipeData(final JsonObject json) {
        for (final var entry : this.data.entrySet()) {
            json.add(entry.getKey(), entry.getValue());
        }
        if (this.conditions != null) {
            json.add("conditions", this.conditions);
        }
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getType() {
        return this.type;
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return null;
    }
}
