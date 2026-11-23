package slimeknights.tconstruct.library.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;

import java.util.List;

/** Translates item-based cures in tool and fluid definitions to NeoForge cures. */
public final class EffectCureUtil {
  private EffectCureUtil() {}

  public static EffectCure get(ItemStack stack) {
    return get(stack.getItem());
  }

  private static EffectCure get(Item item) {
    if (item == Items.MILK_BUCKET) return EffectCures.MILK;
    if (item == Items.HONEY_BOTTLE) return EffectCures.HONEY;
    return EffectCure.get("tconstruct:item/" + BuiltInRegistries.ITEM.getKey(item));
  }

  public static void set(MobEffectInstance instance, List<Item> items) {
    var cures = instance.getCures();
    cures.clear();
    for (Item item : items) {
      cures.add(get(item));
    }
  }

  public static boolean cure(LivingEntity entity, ItemStack stack) {
    return entity.removeEffectsCuredBy(get(stack));
  }
}
