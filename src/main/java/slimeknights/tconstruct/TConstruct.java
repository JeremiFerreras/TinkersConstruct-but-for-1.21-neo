package slimeknights.tconstruct;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.library.recipe.ingredient.TinkerIngredients;
import slimeknights.tconstruct.common.TinkerCapabilityEvents;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.common.data.AdvancementsProvider;
import slimeknights.tconstruct.common.data.ConfigurationDataProvider;
import slimeknights.tconstruct.common.data.DamageTypeProvider;
import slimeknights.tconstruct.common.data.advancement.FunctionProvider;
import slimeknights.tconstruct.common.data.loot.GlobalLootModifiersProvider;
import slimeknights.tconstruct.common.data.loot.LootTableInjectionProvider;
import slimeknights.tconstruct.common.data.loot.TConstructLootTableProvider;
import slimeknights.tconstruct.common.data.tags.BiomeTagProvider;
import slimeknights.tconstruct.common.data.tags.BlockEntityTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.BlockTagProvider;
import slimeknights.tconstruct.common.data.tags.CreativeTabTagProvider;
import slimeknights.tconstruct.common.data.tags.DamageTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.EnchantmentTagProvider;
import slimeknights.tconstruct.common.data.tags.EntityTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.FluidTagProvider;
import slimeknights.tconstruct.common.data.tags.InstrumentTagProvider;
import slimeknights.tconstruct.common.data.tags.ItemTagProvider;
import slimeknights.tconstruct.common.data.tags.MenuTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.PotionTagProvider;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability.ComputableDataKey;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability.TinkerDataKey;
import slimeknights.tconstruct.library.tools.definition.ToolDefinitionLoader;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import slimeknights.tconstruct.library.utils.Util;
import slimeknights.tconstruct.shared.TinkerAttributes;
import slimeknights.tconstruct.shared.TinkerClient;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerEffects;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.item.CopperCanItem;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.data.material.TrimMaterialProvider;
import slimeknights.tconstruct.world.TinkerStructures;
import slimeknights.tconstruct.world.TinkerWorld;
import slimeknights.tconstruct.world.data.WorldgenProvider;

import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * TConstruct, the tool mod. Craft your tools with style, then modify until the original is gone!
 *
 * @author mDiyo
 */

@Mod(TConstruct.MOD_ID)
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class TConstruct {

  public static final String MOD_ID = "tconstruct";
  public static final Logger LOG = LogManager.getLogger(MOD_ID);
  public static final Random RANDOM = new Random();

  /* Instance of this mod, used for grabbing prototype fields */
  public static TConstruct instance;

  public TConstruct(IEventBus bus, ModContainer container) {
    instance = this;

    Config.init(container);
    MaterialRegistry.init();

    // NeoForge 1.21 data attachments must be registered on the mod bus before registries freeze.
    TinkerDataCapability.register(bus);
    PersistentDataCapability.register(bus);
    EntityModifierCapability.register(bus);
    // NeoForge 1.21 requires item capability providers to be registered during RegisterCapabilitiesEvent.
    bus.addListener(TinkerCapabilityEvents::onRegisterCapabilities);

    // initialize modules, done this way rather than with annotations to give us control over the order
    bus.addListener(TConstruct::registerAliases);
    // base
    bus.register(new TinkerCommons());
    bus.register(new TinkerMaterials());
    bus.register(new TinkerEffects(bus));
    bus.register(new TinkerGadgets(bus));
    bus.register(new TinkerAttributes(bus));
    // world
    bus.register(new TinkerWorld());
    bus.register(new TinkerStructures(bus));
    // tools
    bus.register(new TinkerTables());
    bus.register(new TinkerModifiers(bus));
    bus.register(new TinkerToolParts());
    bus.register(new TinkerTools());
    // smeltery
    bus.register(new TinkerSmeltery());
    CopperCanItem.register(bus, TinkerSmeltery.copperCan);
    bus.register(new TinkerFluids());

    // init deferred registers
    TinkerModule.initRegisters(bus);
    TinkerIngredients.init(bus);
    bus.addListener(TinkerNetwork::registerPackets);
    TinkerTags.init();
    // init client logic
    if (FMLEnvironment.dist == Dist.CLIENT) {
      TinkerClient.onConstruct();
    }

    // Optional compat integrations are disabled in this port until their
    // matching NeoForge 1.21.1 compile APIs have been migrated.
  }

  @SubscribeEvent
  static void commonSetup(final FMLCommonSetupEvent event) {
    ToolDefinitionLoader.init();
    StationSlotLayoutLoader.init();
  }

  @SubscribeEvent
  static void gatherData(final GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    PackOutput packOutput = generator.getPackOutput();
    ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
    CompletableFuture<Provider> lookupProvider = event.getLookupProvider();
    boolean server = event.includeServer();

    // its sometimes cleaner to splitup different registry sets to their own classes, combine them here into a single provider
    RegistrySetBuilder registrySetBuilder = new RegistrySetBuilder();
    DamageTypeProvider.register(registrySetBuilder);
    WorldgenProvider.register(registrySetBuilder);
    TrimMaterialProvider.register(registrySetBuilder);
    DatapackBuiltinEntriesProvider datapackRegistryProvider = new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, registrySetBuilder, Set.of(MOD_ID));
    generator.addProvider(server, datapackRegistryProvider);

    // tags
    BlockTagProvider blockTags = new BlockTagProvider(packOutput, lookupProvider, existingFileHelper);
    generator.addProvider(server, blockTags);
    generator.addProvider(server, new ItemTagProvider(packOutput, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
    generator.addProvider(server, new FluidTagProvider(packOutput, lookupProvider, existingFileHelper));
    generator.addProvider(server, new EntityTypeTagProvider(packOutput, lookupProvider, existingFileHelper));
    generator.addProvider(server, new BlockEntityTypeTagProvider(packOutput, lookupProvider, existingFileHelper));
    generator.addProvider(server, new BiomeTagProvider(packOutput, lookupProvider, existingFileHelper));
    generator.addProvider(server, new EnchantmentTagProvider(packOutput, lookupProvider, existingFileHelper));
    generator.addProvider(server, new MenuTypeTagProvider(packOutput, lookupProvider, existingFileHelper));
    generator.addProvider(server, new PotionTagProvider(packOutput, lookupProvider, existingFileHelper));
    generator.addProvider(server, new CreativeTabTagProvider(packOutput, lookupProvider, existingFileHelper));
    generator.addProvider(server, new InstrumentTagProvider(packOutput, lookupProvider, existingFileHelper));
    generator.addProvider(server, new DamageTypeTagProvider(packOutput, datapackRegistryProvider.getRegistryProvider(), existingFileHelper));

    // other datagen
    generator.addProvider(server, new TConstructLootTableProvider(packOutput));
    generator.addProvider(server, new AdvancementsProvider(packOutput));
    generator.addProvider(server, new GlobalLootModifiersProvider(packOutput));
    generator.addProvider(server, new LootTableInjectionProvider(packOutput));
    generator.addProvider(server, new ConfigurationDataProvider(packOutput));
    generator.addProvider(server, new FunctionProvider(packOutput));
  }

  /** Handles missing mappings of all types */
  private static void registerAliases(RegisterEvent event) {
    if (event.getRegistryKey() == Registries.BLOCK) {
      var blocks = event.getRegistry(Registries.BLOCK);
      blocks.addAlias(getResource("silky_jewel_block"), ResourceLocation.withDefaultNamespace("emerald_block"));
      blocks.addAlias(getResource("piglin_head"), ResourceLocation.withDefaultNamespace("piglin_head"));
      blocks.addAlias(getResource("piglin_wall_head"), ResourceLocation.withDefaultNamespace("piglin_wall_head"));
    } else if (event.getRegistryKey() == Registries.ITEM) {
      var items = event.getRegistry(Registries.ITEM);
      items.addAlias(getResource("silky_jewel"), ResourceLocation.withDefaultNamespace("emerald"));
      items.addAlias(getResource("silky_jewel_block"), ResourceLocation.withDefaultNamespace("emerald_block"));
      items.addAlias(getResource("piglin_head"), ResourceLocation.withDefaultNamespace("piglin_head"));
      items.addAlias(getResource("round_plate"), getResource("adze_head"));
      items.addAlias(getResource("round_plate_cast"), getResource("adze_head_cast"));
      items.addAlias(getResource("round_plate_sand_cast"), getResource("adze_head_sand_cast"));
      items.addAlias(getResource("round_plate_red_sand_cast"), getResource("adze_head_red_sand_cast"));
      items.addAlias(getResource("slime_chestplate"), getResource("slime_wings"));
    }
  }

  /* Utils */

  /**
   * Gets a resource location for Tinkers
   * @param name  Resource path
   * @return  Location for tinkers
   */
  @SuppressWarnings("removal")
  public static ResourceLocation getResource(String name) {
    return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
  }

  /**
   * Gets a data key for the capability, mainly used for modifier markers
   * @param name  Resource path
   * @return  Location for tinkers
   */
  public static <T> TinkerDataKey<T> createKey(String name) {
    return TinkerDataKey.of(getResource(name));
  }

  /**
   * Gets a data key for the capability, mainly used for modifier markers
   * @param name         Resource path
   * @param constructor  Constructor for compute if absent
   * @return  Location for tinkers
   */
  public static <T> ComputableDataKey<T> createKey(String name, Supplier<T> constructor) {
    return ComputableDataKey.of(getResource(name), constructor);
  }

  /**
   * Returns the given Resource prefixed with tinkers resource location. Use this function instead of hardcoding
   * resource locations.
   */
  public static String resourceString(String res) {
    return MOD_ID + ':' + res;
  }

  /**
   * Prefixes the given unlocalized name with tinkers prefix. Use this when passing unlocalized names for a uniform
   * namespace.
   */
  public static String prefix(String name) {
    return MOD_ID + "." + name.toLowerCase(Locale.US);
  }

  /** Makes a Tinker's description ID */
  public static String makeDescriptionId(String type, String name) {
    return type + "." + MOD_ID + "." + name;
  }

  /**
   * Makes a translation key for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static String makeTranslationKey(String base, String name) {
    return Util.makeTranslationKey(base, getResource(name));
  }

  /**
   * Makes a translation text component for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static MutableComponent makeTranslation(String base, String name) {
    return Component.translatable(makeTranslationKey(base, name));
  }

  /**
   * Makes a translation text component for the given name
   * @param base       Base name, such as "block" or "gui"
   * @param name       Object name
   * @param arguments  Additional arguments to the translation
   * @return  Translation key
   */
  public static MutableComponent makeTranslation(String base, String name, Object... arguments) {
    return Component.translatable(makeTranslationKey(base, name), arguments);
  }

  /**
   * This function is called in the constructor in some internal classes that are a common target for addons to wrongly extend.
   * These classes will cause issues if blindly used by the addon, and are typically trivial for the addon to implement
   * the parts they need if they just put in some effort understanding the code they are copying.
   *
   * As a reminder for addon devs, anything that is not in the library package can and will change arbitrarily. If you need to use a feature outside library, request it on our github.
   * @param self  Class to validate
   */
  public static void sealTinkersClass(Object self, String base, String solution) {
    // note for future maintainers: this does not use Java 9's sealed classes as unless you use modules those are restricted to the same package.
    // Dumb restriction but not like we can change it.
    String name = self.getClass().getName();
    if (!name.startsWith("slimeknights.tconstruct.")) {
      throw new IllegalStateException(base + " being extended from invalid package " + name + ". " + solution);
    }
  }
}
