package slimeknights.tconstruct.library.json.condition;


import net.minecraft.core.registries.Registries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.shared.TinkerCommons;

/** @deprecated use {@link slimeknights.mantle.recipe.condition.TagFilledCondition} */
@Deprecated(forRemoval = true)
@RequiredArgsConstructor
public class TagNotEmptyCondition<T> implements LootItemCondition, ICondition {
  public static final ResourceLocation NAME = TConstruct.getResource("tag_not_empty");
  /**
   * Codec for both the recipe condition and the loot condition; replaces the old {@code ConditionSerializer}.
   * // PORT: register to {@code NeoForgeRegistries.Keys.CONDITION_CODECS} and to {@code Registries.LOOT_CONDITION_TYPE}.
   */
  public static final MapCodec<TagNotEmptyCondition<?>> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
    ResourceLocation.CODEC.fieldOf("registry").forGetter(c -> c.tag.registry().location()),
    ResourceLocation.CODEC.fieldOf("tag").forGetter(c -> c.tag.location())
  ).apply(inst, TagNotEmptyCondition::createGeneric));

  private final TagKey<T> tag;

  /** Helper to deal with generics */
  private static <T> TagNotEmptyCondition<T> createGeneric(ResourceLocation registryName, ResourceLocation tag) {
    ResourceKey<Registry<T>> registry = ResourceKey.createRegistryKey(registryName);
    return new TagNotEmptyCondition<>(TagKey.create(registry, tag));
  }

  @SuppressWarnings("removal")
  @Override
  public LootItemConditionType getType() {
    return TinkerCommons.lootTagNotEmptyCondition.get();
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(IContext context) {
    return !context.getTag(tag).isEmpty();
  }

  @Override
  public boolean test(LootContext context) {
    Registry<T> registry = RegistryHelper.getRegistry(tag.registry());
    return registry != null && registry.getTagOrEmpty(tag).iterator().hasNext();
  }

  @Override
  public String toString() {
    return "TagNotEmptyCondition(" + tag + ")";
  }
}
