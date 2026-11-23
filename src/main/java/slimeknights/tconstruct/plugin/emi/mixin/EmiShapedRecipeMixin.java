package slimeknights.tconstruct.plugin.emi.mixin;

import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.recipe.material.ShapedMaterialsRecipe;

import java.util.List;

/** Avoid simulating container returns on a crafting grid with a missing material row. */
@Pseudo
@Mixin(targets = "dev.emi.emi.recipe.EmiShapedRecipe", remap = false)
abstract class EmiShapedRecipeMixin {
  @Inject(method = "setRemainders", at = @At("HEAD"), cancellable = true)
  private static void skipUnavailableMaterialInputs(List<EmiIngredient> inputs, CraftingRecipe recipe, CallbackInfo ci) {
    if (recipe instanceof ShapedMaterialsRecipe shaped) {
      for (Ingredient part : shaped.getParts()) {
        if (part.getItems().length == 0) {
          // No valid grid exists in this pack. EMI otherwise indexes a cropped row as a full grid.
          ci.cancel();
          return;
        }
      }
    }
  }
}
