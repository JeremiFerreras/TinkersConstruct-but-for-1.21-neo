package slimeknights.tconstruct.smeltery.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import slimeknights.tconstruct.library.recipe.FluidValues;

/** Transfers an entire stack of copper cans at once. */
public class CopperCanFluidHandler implements IFluidHandlerItem {
  private final ItemStack container;

  public CopperCanFluidHandler(ItemStack container) {
    this.container = container;
  }

  @Override
  public ItemStack getContainer() {
    return container;
  }

  @Override
  public int getTanks() {
    return 1;
  }

  @Override
  public int getTankCapacity(int tank) {
    return FluidValues.INGOT * container.getCount();
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack fluid) {
    return true;
  }

  @Override
  public FluidStack getFluidInTank(int tank) {
    return CopperCanItem.contents(container).copy().copyWithAmount(getTankCapacity(tank));
  }

  @Override
  public int fill(FluidStack fluid, FluidAction action) {
    int capacity = getTankCapacity(0);
    if (capacity == 0 || !CopperCanItem.contents(container).isEmpty() || fluid.getAmount() < capacity) {
      return 0;
    }
    if (action.execute()) {
      CopperCanItem.setFluid(container, fluid);
    }
    return capacity;
  }

  @Override
  public FluidStack drain(FluidStack requested, FluidAction action) {
    if (requested.isEmpty() || !CopperCanItem.contents(container).isSameFluidSameComponents(requested)) {
      return FluidStack.EMPTY;
    }
    return drain(requested.getAmount(), action);
  }

  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    FluidStack output = getFluidInTank(0);
    if (output.isEmpty() || maxDrain < output.getAmount()) {
      return FluidStack.EMPTY;
    }
    if (action.execute()) {
      CopperCanItem.removeFluid(container);
    }
    return output;
  }
}
