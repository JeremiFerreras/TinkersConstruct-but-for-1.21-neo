package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.Optional;
import java.util.stream.Stream;

/** Ingredient matching an instrument-bearing item with a particular instrument or excluding a tag. */
public class InstrumentIngredient implements ICustomIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("instrument");
  public static final MapCodec<InstrumentIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ingredient -> ingredient.item),
    ResourceLocation.CODEC.optionalFieldOf("instrument").forGetter(ingredient -> Optional.ofNullable(ingredient.instrument).map(ResourceKey::location)),
    ResourceLocation.CODEC.optionalFieldOf("ignore").forGetter(ingredient -> Optional.ofNullable(ingredient.ignore).map(TagKey::location))
  ).apply(instance, (item, instrument, ignore) -> new InstrumentIngredient(
    item,
    instrument.map(id -> ResourceKey.create(Registries.INSTRUMENT, id)).orElse(null),
    ignore.map(id -> TagKey.create(Registries.INSTRUMENT, id)).orElse(null)
  )));
  public static final IngredientType<InstrumentIngredient> TYPE = new IngredientType<>(CODEC);

  private final Item item;
  private final ResourceKey<Instrument> instrument;
  private final TagKey<Instrument> ignore;

  private InstrumentIngredient(Item item, ResourceKey<Instrument> instrument, TagKey<Instrument> ignore) {
    if ((instrument == null) == (ignore == null)) {
      throw new IllegalArgumentException("Instrument ingredient must define exactly one of 'instrument' or 'ignore'");
    }
    this.item = item;
    this.instrument = instrument;
    this.ignore = ignore;
  }

  public static InstrumentIngredient of(Item item, ResourceKey<Instrument> instrument) {
    return new InstrumentIngredient(item, instrument, null);
  }

  public static InstrumentIngredient of(Item item, TagKey<Instrument> ignore) {
    return new InstrumentIngredient(item, null, ignore);
  }

  @Override
  public boolean test(ItemStack stack) {
    if (stack.isEmpty() || !stack.is(item)) {
      return false;
    }
    Holder<Instrument> held = stack.get(DataComponents.INSTRUMENT);
    if (held != null) {
      return instrument != null ? held.is(instrument) : !held.is(ignore);
    }
    return instrument == null;
  }

  @Override
  public Stream<ItemStack> getItems() {
    ItemStack stack = new ItemStack(item);
    if (instrument != null) {
      BuiltInRegistries.INSTRUMENT.getHolder(instrument).ifPresent(holder -> stack.set(DataComponents.INSTRUMENT, holder));
    }
    return Stream.of(stack);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }
}
