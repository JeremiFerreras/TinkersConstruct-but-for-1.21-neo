package slimeknights.tconstruct.common;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.fluids.item.ContainerFoodItem;
import slimeknights.tconstruct.fluids.item.MagmaBottleItem;
import slimeknights.tconstruct.fluids.item.PotionBucketItem;
import slimeknights.tconstruct.fluids.util.ConstantFluidContainerWrapper;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.tools.capability.ToolCapabilityProvider;
import slimeknights.tconstruct.smeltery.item.CopperCanFluidHandler;
import slimeknights.tconstruct.smeltery.item.CopperCanItem;
import slimeknights.tconstruct.smeltery.item.TankItem;
import slimeknights.tconstruct.smeltery.item.TankItemFluidHandler;

/**
 * Central registration for item capabilities removed from ItemStack#initCapabilities in NeoForge 1.21.
 */
public final class TinkerCapabilityEvents {
  private TinkerCapabilityEvents() {}

  public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
    // Tool modifiers provide item, fluid, energy, and block-item capabilities dynamically.
    ToolCapabilityProvider.registerItemCaps(event);

    for (Item item : BuiltInRegistries.ITEM) {
      if (item instanceof TankItem tankItem) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new TankItemFluidHandler(tankItem, stack), item);
      }
      if (item instanceof CopperCanItem) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new CopperCanFluidHandler(stack), item);
      }
      if (item instanceof PotionBucketItem) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new PotionBucketItem.PotionBucketWrapper(stack), item);
      }
      if (item instanceof MagmaBottleItem) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new ConstantFluidContainerWrapper(
          new FluidStack(TinkerFluids.magma.get(), FluidValues.BOTTLE), stack), item);
      }
      if (item instanceof ContainerFoodItem.FluidContainerFoodItem food) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new ConstantFluidContainerWrapper(food.getFluidForCap(), stack), item);
      }
    }

    // Vanilla's powder snow bucket does not expose a fluid capability by itself.
    event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new ConstantFluidContainerWrapper(
      new FluidStack(TinkerFluids.powderedSnow.get(), FluidType.BUCKET_VOLUME), stack, Items.BUCKET.getDefaultInstance()), Items.POWDER_SNOW_BUCKET);
  }
}
