package slimeknights.tconstruct.library.recipe.ingredient;


import net.minecraft.core.registries.Registries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Ingredient matching material items with the given value. Typically, matches ingots or blocks.
 * <p>
 * // PORT: {@link Ingredient} is final in 1.21.1 so this is now a {@link ICustomIngredient}. The {@code value} field is
 * always an object with {@code min}/{@code max} now, as the old serializer's "primitive or object" handling relied on
 * raw GSON. Register {@link #SERIALIZER} to {@code NeoForgeRegistries.Keys.INGREDIENT_TYPES} under {@link #ID}.
 */
@Getter
@RequiredArgsConstructor
public class MaterialValueIngredient implements ICustomIngredient {
  /** Registry ID of this ingredient type */
  public static final ResourceLocation ID = TConstruct.getResource("material_value");
  /** Codec for the material predicate, the loadable handles both the compact string and object forms */
  private static final Codec<IJsonPredicate<MaterialVariantId>> MATERIAL_CODEC = MaterialPredicate.LOADER.codec();
  /** Codec instance */
  public static final MapCodec<MaterialValueIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
    MATERIAL_CODEC.optionalFieldOf("material", MaterialPredicate.ANY).forGetter(i -> i.material),
    Codec.FLOAT.optionalFieldOf("min", 0f).forGetter(i -> i.minValue),
    Codec.FLOAT.optionalFieldOf("max", Float.POSITIVE_INFINITY).forGetter(i -> i.maxValue)
  ).apply(inst, MaterialValueIngredient::new));
  /** Ingredient type, register this to {@code NeoForgeRegistries.Keys.INGREDIENT_TYPES} */
  public static final IngredientType<MaterialValueIngredient> SERIALIZER = new IngredientType<>(CODEC);

  private final IJsonPredicate<MaterialVariantId> material;
  private final float minValue;
  private final float maxValue;
  @Nullable
  private ItemStack[] items;

  /** Creates an ingredient matching a range of values */
  public static MaterialValueIngredient of(IJsonPredicate<MaterialVariantId> materials, float minValue, float maxValue) {
    return new MaterialValueIngredient(materials, minValue, maxValue);
  }

  /** Creates an ingredient matching an exact value */
  public static MaterialValueIngredient of(IJsonPredicate<MaterialVariantId> materials, float value) {
    return of(materials, value, value);
  }

  /** Checks the given material recipe against our filters */
  public boolean test(MaterialRecipe material) {
    float value = material.getValue() / (float) material.getNeeded();
    return minValue <= value && value <= maxValue && this.material.matches(material.getMaterial().getVariant());
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null) {
      return false;
    }
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe);
  }

  @Override
  public Stream<ItemStack> getItems() {
    if (items == null) {
      items = MaterialRecipeCache.getAllRecipes().stream()
        .filter(this::test)
        .flatMap(material -> Arrays.stream(material.getIngredient().getItems()))
        .toArray(ItemStack[]::new);
    }
    return Stream.of(items);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public IngredientType<?> getType() {
    return SERIALIZER;
  }


  /* Helpers for ShapedMaterialRecipe */

  /** Checks if this ingredient fully contains the range of the other */
  private boolean contains(MaterialValueIngredient other) {
    return this.minValue <= other.minValue && other.maxValue <= this.maxValue;
  }

  /** Creates an ingredient that matches anything either of the two ingredients matches */
  public MaterialValueIngredient merge(MaterialValueIngredient other) {
    if (this == other) return this;

    // if we have the same predicate, we can possibly skip creating a new instance
    IJsonPredicate<MaterialVariantId> predicate = this.material;
    if (this.material.equals(other.material)) {
      if (this.contains(other)) {
        return this;
      }
      if (other.contains(this)) {
        return other;
      }
    } else {
      predicate = MaterialPredicate.or(this.material, other.material);
    }
    return new MaterialValueIngredient(predicate, Math.min(this.minValue, other.minValue), Math.max(this.maxValue, other.maxValue));
  }

  /** Gets the material matching this recipe */
  @Nullable
  public MaterialVariantId getMaterial(ItemStack stack) {
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe) ? recipe.getMaterial().getVariant() : null;
  }

  @Override
  public boolean equals(Object other) {
    return this == other || other instanceof MaterialValueIngredient ingredient
                            && material.equals(ingredient.material)
                            && minValue == ingredient.minValue
                            && maxValue == ingredient.maxValue;
  }

  @Override
  public int hashCode() {
    return (31 * material.hashCode() + Float.hashCode(minValue)) * 31 + Float.hashCode(maxValue);
  }

  @Override
  public String toString() {
    return "MaterialValueIngredient(" + material + ", " + minValue + ", " + maxValue + ")";
  }
}
