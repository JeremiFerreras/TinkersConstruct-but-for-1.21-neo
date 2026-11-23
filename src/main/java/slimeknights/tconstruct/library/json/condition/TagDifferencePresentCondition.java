package slimeknights.tconstruct.library.json.condition;


import net.minecraft.core.registries.Registries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.tconstruct.TConstruct;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** @deprecated use {@link slimeknights.mantle.recipe.condition.TagCombinationCondition#difference(TagKey, TagKey)} */
@Deprecated(forRemoval = true)
public class TagDifferencePresentCondition<T> implements ICondition {
  public static final ResourceLocation NAME = TConstruct.getResource("tag_difference_present");
  /** // PORT: conditions are codec based in 1.21.1, register this to {@code NeoForgeRegistries.Keys.CONDITION_CODECS} */
  public static final MapCodec<TagDifferencePresentCondition<?>> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
    ResourceLocation.CODEC.fieldOf("registry").forGetter(c -> c.base.registry().location()),
    ResourceLocation.CODEC.fieldOf("base").forGetter(c -> c.base.location()),
    ResourceLocation.CODEC.listOf().fieldOf("subtracted").forGetter(c -> c.subtracted.stream().map(TagKey::location).toList())
  ).apply(inst, TagDifferencePresentCondition::readGeneric));
  /** @deprecated use {@link #CODEC} */
  @Deprecated(forRemoval = true)
  public static final MapCodec<TagDifferencePresentCondition<?>> SERIALIZER = CODEC;

  private final TagKey<T> base;
  private final List<TagKey<T>> subtracted;

  public TagDifferencePresentCondition(TagKey<T> base, List<TagKey<T>> subtracted) {
    if (subtracted.isEmpty()) {
      throw new IllegalArgumentException("Cannot create a condition with no subtracted");
    }
    this.base = base;
    this.subtracted = subtracted;
  }

  /** Creates a condition from a set of keys */
  @SafeVarargs
  public static <T> TagDifferencePresentCondition<T> ofKeys(TagKey<T> base, TagKey<T>... subtracted) {
    return new TagDifferencePresentCondition<>(base, Arrays.asList(subtracted));
  }

  /** Creates a condition from a registry and a set of names */
  public static <T> TagDifferencePresentCondition<T> ofNames(ResourceKey<? extends Registry<T>> registry, ResourceLocation base, ResourceLocation... subtracted) {
    TagKey<T> baseKey = TagKey.create(registry, base);
    return new TagDifferencePresentCondition<>(baseKey, Arrays.stream(subtracted).map(name -> TagKey.create(registry, name)).toList());
  }

  /** Reads with generics happy */
  private static <T> TagDifferencePresentCondition<T> readGeneric(ResourceLocation registryName, ResourceLocation base, List<ResourceLocation> subtracted) {
    ResourceKey<Registry<T>> registry = ResourceKey.createRegistryKey(registryName);
    return new TagDifferencePresentCondition<>(
      TagKey.create(registry, base),
      subtracted.stream().map(name -> TagKey.<T>create(registry, name)).toList());
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(IContext context) {
    // get the base tag
    Collection<Holder<T>> base = context.getTag(this.base);
    if (base == null || base.isEmpty()) {
      return false;
    }

    // no subtracted tags? we good
    if (subtracted.isEmpty()) {
      return true;
    }
    // all tags have something, so find the first item that is in all tags
    itemLoop:
    for (Holder<T> entry : base) {
      // find the first item contained in no subtracted tags
      for (TagKey<T> tag : subtracted) {
        if (context.getTag(tag).contains(entry)) {
          continue itemLoop;
        }
      }
      // no subtracted contains the item? success
      return true;
    }
    // no item not in any subtracted
    return false;
  }

  @Override
  public String toString() {
    return "TagDifferencePresentCondition(" + base + ", " + subtracted + ")";
  }
}
