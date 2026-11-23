package slimeknights.tconstruct.library.recipe.ingredient;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.tconstruct.TConstruct;

/** Registers Tinkers' Construct custom ingredient types for NeoForge 1.21.1. */
public final class TinkerIngredients {
  private TinkerIngredients() {}

  private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
    DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, TConstruct.MOD_ID);

  static {
    INGREDIENT_TYPES.register("material", () -> MaterialIngredient.TYPE);
    INGREDIENT_TYPES.register("no_container", () -> NoContainerIngredient.TYPE);
    INGREDIENT_TYPES.register("tool_hook", () -> ToolHookIngredient.TYPE);
    INGREDIENT_TYPES.register("block_tag", () -> BlockTagIngredient.TYPE);
    INGREDIENT_TYPES.register("instrument", () -> InstrumentIngredient.TYPE);
    INGREDIENT_TYPES.register("material_value", () -> MaterialValueIngredient.SERIALIZER);
  }

  public static void init(IEventBus modBus) {
    INGREDIENT_TYPES.register(modBus);
  }
}
