package slimeknights.tconstruct.library.recipe.ingredient;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableIngredientSerializer;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.item.IModifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Ingredient that only matches tools with a specific hook */
@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ToolHookIngredient implements net.neoforged.neoforge.common.crafting.ICustomIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("tool_hook");
  public static final RecordLoadable<ToolHookIngredient> LOADER = RecordLoadable.create(
    Loadables.ITEM_TAG.defaultField("tag", TinkerTags.Items.MODIFIABLE, i -> i.tag),
    ToolHooks.LOADER.requiredField("hook", i -> i.hook),
    ToolHookIngredient::new);
  public static final IngredientType<ToolHookIngredient> TYPE = LoadableIngredientSerializer.type(LOADER);

  private final TagKey<Item> tag;
  private final ModuleHook<?> hook;

  public static ToolHookIngredient of(TagKey<Item> tag, ModuleHook<?> hook) {
    return new ToolHookIngredient(tag, hook);
  }

  public static ToolHookIngredient of(ModuleHook<?> hook) {
    return of(TinkerTags.Items.MODIFIABLE, hook);
  }

  @Override
  public boolean test(ItemStack stack) {
    return !stack.isEmpty() && stack.is(tag) && stack.getItem() instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public Stream<ItemStack> getItems() {
    List<ItemStack> list = new ArrayList<>();
    // filtered version of tag values
    for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
      if (holder.value() instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook)) {
        list.add(new ItemStack(modifiable));
      }
    }
    return list.stream();
  }
}
