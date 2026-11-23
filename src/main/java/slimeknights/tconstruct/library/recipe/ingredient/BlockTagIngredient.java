package slimeknights.tconstruct.library.recipe.ingredient;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableIngredientSerializer;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Item ingredient matching items with a block form in the given tag */
@RequiredArgsConstructor
public class BlockTagIngredient implements net.neoforged.neoforge.common.crafting.ICustomIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("block_tag");
  public static final RecordLoadable<BlockTagIngredient> LOADER = RecordLoadable.create(
    Loadables.BLOCK_TAG.requiredField("tag", i -> i.tag),
    BlockTagIngredient::new);
  public static final IngredientType<BlockTagIngredient> TYPE = LoadableIngredientSerializer.type(LOADER);

  private final TagKey<Block> tag;
  @Nullable
  private Set<Item> matchingItems;

  @Override
  public boolean test(ItemStack stack) {
    return !stack.isEmpty() && getMatchingItems().contains(stack.getItem());
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  /** Gets the ordered matching items set */
  private Set<Item> getMatchingItems() {
    if (matchingItems == null) {
      matchingItems = RegistryHelper.getTagValueStream(BuiltInRegistries.BLOCK, tag)
                                    .map(Block::asItem)
                                    .filter(item -> item != Items.AIR)
                                    .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    return matchingItems;
  }

  @Override
  public Stream<ItemStack> getItems() {
    return getMatchingItems().stream().map(ItemStack::new);
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }
}
