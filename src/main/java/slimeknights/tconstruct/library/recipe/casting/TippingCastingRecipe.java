package slimeknights.tconstruct.library.recipe.casting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.casting.potion.ModifierPotionCastingRecipe;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Casting recipe applying a potion to a tool.
 * TODO 1.21: move to {@link slimeknights.tconstruct.library.recipe.casting.potion}
 */
public class TippingCastingRecipe extends ModifierPotionCastingRecipe {
  protected static final LoadableField<Ingredient, ModifierPotionCastingRecipe> TOOL_FIELD = ModifierPotionCastingRecipe.TOOL_FIELD;
  public static final RecordLoadable<TippingCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPED_SERIALIZER.requiredField(), ContextKey.ID.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP,
    TOOL_FIELD, FLUID_FIELD, COOLING_TIME_FIELD, MODIFIER_FIELD, TippingCastingRecipe::new);

  public TippingCastingRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient tool, FluidIngredient fluid, int coolingTime, ModifierId modifier) {
    super(serializer, id, group, tool, fluid, Items.AIR, coolingTime, modifier);
  }

  @Override
  public boolean matches(ICastingContainer inv, Level level) {
    if (!super.matches(inv, level)) {
      return false;
    }
    Holder<Potion> potion = getPotion(inv);
    return potion != null && !ModifierUtil.getPersistentString(inv.getStack(), modifier).equals(getPotionId(potion));
  }

  /** Gets the potion stored on the fluid stack, or null if no potion component is present. */
  @javax.annotation.Nullable
  private static Holder<Potion> getPotion(ICastingContainer inv) {
    java.util.Optional<? extends PotionContents> contents = inv.getFluidData().get(DataComponents.POTION_CONTENTS);
    if (contents != null && contents.isPresent()) {
      return contents.get().potion().orElse(null);
    }
    return null;
  }

  private static String getPotionId(Holder<Potion> potion) {
    return potion.unwrapKey().map(key -> key.location().toString()).orElse("");
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    ItemStack result = inv.getStack().copy();
    Holder<Potion> potion = getPotion(inv);
    if (potion != null) {
      ToolStack.from(result).getPersistentData().putString(modifier.getId(), getPotionId(potion));
    }
    return result;
  }

  /* JEI */
  @Override
  public List<IDisplayableCastingRecipe> getRecipes(RegistryAccess access) {
    if (displayRecipes == null) {
      List<Holder<Potion>> potions = BuiltInRegistries.POTION.holders()
        .filter(holder -> !holder.is(Potions.EMPTY_ID))
        .toList();
      List<String> potionIds = potions.stream().map(TippingCastingRecipe::getPotionId).toList();
      List<FluidStack> basePotions = this.fluid.getFluids();
      List<FluidStack> fluids = basePotions.stream()
        .flatMap(fluid -> potions.stream().map(potion -> {
          DataComponentPatch potionData = DataComponentPatch.builder()
            .set(DataComponents.POTION_CONTENTS, new PotionContents(potion))
            .build();
          return new FluidStack(fluid.getFluidHolder(), fluid.getAmount(), potionData);
        }))
        .toList();

      int basePotionCount = basePotions.size();
      displayRecipes = Arrays.stream(bottle.getItems()).map(stack -> {
        ItemStack withModifier = addModifier(stack);
        List<ItemStack> withPotion = addPotion(withModifier, potionIds);
        if (basePotionCount > 1) {
          List<ItemStack> list = new ArrayList<>(withPotion.size() * basePotionCount);
          for (int i = 0; i < basePotionCount; i++) {
            list.addAll(withPotion);
          }
          withPotion = List.copyOf(list);
        }
        return DisplayCastingRecipe.from(this)
          .cast(withModifier).consumed()
          .results(withPotion).fluids(fluids).linkFluidsToOutput()
          .coolingTime(coolingTime).build();
      }).toList();
    }
    return displayRecipes;
  }
}
