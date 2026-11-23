package slimeknights.tconstruct.library.recipe.material;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Wraps vanilla crafting recipe datagen into Tinkers material crafting recipes.
 * PORT: 1.21 RecipeOutput accepts Recipe instances, not FinishedRecipe JSON wrappers.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MaterialsConsumerBuilder {
  private final String parts;
  private final int partCount;
  private final List<MaterialVariantId> materials = new ArrayList<>();

  /** Creates a new shaped recipe with the given ingredients as parts */
  public static MaterialsConsumerBuilder shaped(String parts) {
    if (parts.isEmpty()) {
      throw new IllegalArgumentException("Parts may not be empty");
    }
    return new MaterialsConsumerBuilder(parts, 0);
  }

  /** Creates a new shapeless recipe with the first ingredients as parts */
  public static MaterialsConsumerBuilder shapeless(int parts) {
    if (parts <= 0) {
      throw new IllegalArgumentException("Parts must be greater than 0");
    }
    return new MaterialsConsumerBuilder("", parts);
  }

  /** Adds a material to the builder */
  public MaterialsConsumerBuilder material(MaterialVariantId material) {
    materials.add(material);
    return this;
  }

  /** Builds the wrapped consumer */
  public RecipeOutput build(RecipeOutput consumer) {
    return new RecipeOutput() {
      @Override
      public Advancement.Builder advancement() {
        return consumer.advancement();
      }

      @Override
      public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        Recipe<?> converted = recipe;
        if (partCount > 0 && recipe instanceof ShapelessRecipe shapeless) {
          converted = new ShapelessMaterialsRecipe(shapeless, partCount, List.copyOf(materials));
        } else if (recipe instanceof ShapedRecipe shaped) {
          // PORT: needs AT `public net.minecraft.world.item.crafting.ShapedRecipePattern data` (same as Mantle)
          List<Ingredient> resolvedParts = resolveParts(shaped, parts);
          converted = new ShapedMaterialsRecipe(shaped, resolvedParts, List.copyOf(materials), parts);
        }
        consumer.accept(id, converted, advancement, conditions);
      }
    };
  }

  /** Maps the builder's part-symbol string onto the shaped recipe key. */
  private List<Ingredient> resolveParts(ShapedRecipe shaped, String partSymbols) {
    if (partSymbols.isEmpty()) {
      return List.of();
    }
    ShapedRecipePattern.Data data = shaped.pattern.data.orElseThrow(() ->
      new IllegalStateException("Shaped recipe " + shaped.getGroup() + " has no pattern data; add AT for ShapedRecipePattern.data"));
    return ShapedMaterialsRecipe.Serializer.resolveParts(data, partSymbols);
  }
}
