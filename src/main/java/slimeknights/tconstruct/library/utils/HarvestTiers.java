package slimeknights.tconstruct.library.utils;

import com.google.common.collect.Maps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import slimeknights.mantle.client.ResourceColorManager;
import slimeknights.mantle.data.listener.ISafeManagerReloadListener;
import slimeknights.tconstruct.TConstruct;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Harvest level display names
 */
public class HarvestTiers {
  private HarvestTiers() {}

  /**
   * NeoForge 1.21 removed the tier sorting registry. Tinkers only serializes the
   * vanilla tiers, so keep their stable IDs and the ordering Forge previously
   * supplied locally.
   */
  private static final List<Tier> SORTED_TIERS = List.of(
    Tiers.WOOD, Tiers.GOLD, Tiers.STONE, Tiers.IRON, Tiers.DIAMOND, Tiers.NETHERITE);
  private static final Map<Tier,ResourceLocation> TIER_IDS = new IdentityHashMap<>();
  private static final Map<ResourceLocation,Tier> TIERS_BY_ID = Maps.newHashMap();
  static {
    register(Tiers.WOOD, "wood");
    register(Tiers.STONE, "stone");
    register(Tiers.IRON, "iron");
    register(Tiers.DIAMOND, "diamond");
    register(Tiers.GOLD, "gold");
    register(Tiers.NETHERITE, "netherite");
  }

  private static void register(Tier tier, String name) {
    ResourceLocation id = ResourceLocation.withDefaultNamespace(name);
    TIER_IDS.put(tier, id);
    TIERS_BY_ID.put(id, tier);
  }

  public static ResourceLocation getId(Tier tier) {
    return TIER_IDS.get(tier);
  }

  public static Tier byId(ResourceLocation id) {
    return TIERS_BY_ID.get(id);
  }

  public static boolean isCorrectTierForDrops(Tier tier, net.minecraft.world.level.block.state.BlockState state) {
    return !state.is(tier.getIncorrectBlocksForDrops());
  }

  /** Cache of name for each tier */
  private static final Map<Tier, Component> harvestLevelNames = Maps.newHashMap();
  /** Listener to clear name cache so we get new colors */
  public static final ISafeManagerReloadListener RELOAD_LISTENER = manager -> harvestLevelNames.clear();

  /** Makes a translation key for the given name */
  private static MutableComponent makeLevelKey(Tier tier) {
    String key = Util.makeTranslationKey("harvest_tier", getId(tier));
    TextColor color = ResourceColorManager.getTextColor(key);
    return TConstruct.makeTranslation("stat", key).withStyle(style -> style.withColor(color));
  }

  /**
   * Gets the harvest level name for the given level number
   * @param tier  Tier
   * @return  Level name
   */
  public static Component getName(Tier tier) {
    return harvestLevelNames.computeIfAbsent(tier, n ->  makeLevelKey(tier));
  }

  /** Gets the larger of two tiers */
  public static Tier max(Tier a, Tier b) {
    List<Tier> sorted = SORTED_TIERS;
    // note indexOf returns -1 if the tier is missing, so the larger of an unsorted tier and a sorted one is the sorted one
    if (sorted.indexOf(b) > sorted.indexOf(a)) {
      return b;
    }
    return a;
  }

  /** Gets the smaller of two tiers */
  public static Tier min(Tier a, Tier b) {
    List<Tier> sorted = SORTED_TIERS;
    // note indexOf returns -1 if the tier is missing, so the smaller of an unsorted tier and a sorted one is the unsorted one
    if (sorted.indexOf(b) < sorted.indexOf(a)) {
      return b;
    }
    return a;
  }

  /** Gets the smallest tier in the sorting registry */
  public static Tier minTier() {
    List<Tier> sortedTiers = SORTED_TIERS;
    if (sortedTiers.isEmpty()) {
      TConstruct.LOG.error("No sorted tiers exist, this should not happen");
      return Tiers.WOOD;
    }
    return sortedTiers.get(0);
  }
}
