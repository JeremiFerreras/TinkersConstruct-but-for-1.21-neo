package slimeknights.tconstruct.plugin.jei.util;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import javax.annotation.Nullable;

/** Common logic for subtype interpreter between the fluid and item form of our potion. */
public interface PotionSubtypeInterpreter<T> extends IIngredientSubtypeInterpreter<T> {
  @Nullable
  PotionContents getPotion(T ingredient);

  @Override
  default String apply(T ingredient, UidContext context) {
    PotionContents contents = getPotion(ingredient);
    if (contents == null || contents == PotionContents.EMPTY) {
      return IIngredientSubtypeInterpreter.NONE;
    }
    StringBuilder stringBuilder = new StringBuilder(Potion.getName(contents.potion(), ""));
    for (MobEffectInstance effect : contents.getAllEffects()) {
      stringBuilder.append(";").append(effect);
    }
    return stringBuilder.toString();
  }
}
