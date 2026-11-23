package slimeknights.tconstruct.smeltery.block.entity.module;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;
import slimeknights.mantle.block.entity.MantleBlockEntity;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Fuel module that supports multiple tanks, selecting just one for the fuel result */
public class MultitankFuelModule extends FuelModule implements IFluidHandler {
  /** Block position that will never be valid in world, used for sync */
  private static final BlockPos NULL_POS = new BlockPos(0, Short.MIN_VALUE, 0);

  /** Supplier for the list of valid tank positions */
  private final Supplier<List<BlockPos>> tankSupplier;
  /** Position of the last fluid handler */
  private BlockPos lastPos = NULL_POS;

  /** Map of all tank handlers at each relevant position. Used for fast switching between handlers, notably in the UI */
  private Map<BlockPos,IFluidHandler> tankHandlers;
  public MultitankFuelModule(MantleBlockEntity parent, Supplier<List<BlockPos>> tankSupplier) {
    super(parent);
    this.tankSupplier = tankSupplier;
  }

  private void clearLastListener() {
    fluidHandler = null;
  }

  public void clearFluidListeners() {
    tankHandlers = null;
    fluidHandler = null;
  }

  public void ensureTankPresent(BlockEntity be) {
    if (tankHandlers != null) {
      BlockPos pos = be.getBlockPos();
      Level level = be.getLevel();
      if (!tankHandlers.containsKey(pos) && level != null) {
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        if (handler != null) tankHandlers.put(pos, handler);
      }
    }
  }

  private Map<BlockPos,IFluidHandler> getTankHandlers() {
    if (tankHandlers == null) {
      tankHandlers = new LinkedHashMap<>();
      Level world = getLevel();
      for (BlockPos pos : tankSupplier.get()) {
        IFluidHandler handler = world.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        if (handler != null) tankHandlers.put(pos, handler);
      }
    }
    return tankHandlers;
  }


  /* Fuel finding */

  /**
   * Tries to consume fuel from the given position
   * @param pos  Position
   * @return   Temperature of the consumed fuel, 0 if none found
   */
  private int tryFuelPosition(BlockPos pos, boolean consume) {
    IFluidHandler handler = getTankHandlers().get(pos);
    if (handler != null) {
      int temp = tryLiquidFuel(handler, consume);
      if (temp > 0) {
        clearLastListener();
        fluidHandler = handler;
        lastPos = pos;
        return temp;
      }
    }
    return 0;
  }

  /**
   * Attempts to consume fuel from one of the tanks
   * @return  temperature of the found fluid, 0 if none
   */
  @Override
  public int findFuel(boolean consume) {
    if (fluidHandler != null) {
      int temperature = tryLiquidFuel(fluidHandler, consume);
      if (temperature > 0) return temperature;
    }
    if (lastPos != NULL_POS) {
      int temp = tryFuelPosition(lastPos, consume);
      if (temp > 0) return temp;
    }
    for (BlockPos pos : tankSupplier.get()) {
      if (!pos.equals(lastPos)) {
        int temp = tryFuelPosition(pos, consume);
        if (temp > 0) return temp;
      }
    }
    if (consume) {
      temperature = 0;
      rate = 0;
    }
    return 0;
  }


  /* NBT */
  private static final String TAG_LAST_FUEL = "last_fuel";

  @Override
  public void readFromTag(CompoundTag nbt) {
    super.readFromTag(nbt);
    if (nbt.contains(TAG_LAST_FUEL, Tag.TAG_COMPOUND)) {
      lastPos = NbtUtils.readBlockPos(nbt.getCompound(TAG_LAST_FUEL)).offset(parent.getBlockPos());
    }
  }

  @Override
  public CompoundTag writeToTag(CompoundTag nbt) {
    nbt = super.writeToTag(nbt);
    if (lastPos != NULL_POS) {
      nbt.put(TAG_LAST_FUEL, NbtUtils.writeBlockPos(lastPos.subtract(parent.getBlockPos())));
    }
    return nbt;
  }


  /* UI syncing */
  private static final int LAST_X = 4;
  private static final int LAST_Y = 5;
  private static final int LAST_Z = 6;

  @Override
  public int getCount() {
    return 7;
  }

  @Override
  public int get(int index) {
    return switch (index) {
      case LAST_X -> lastPos.getX();
      case LAST_Y -> lastPos.getY();
      case LAST_Z -> lastPos.getZ();
      default -> super.get(index);
    };
  }

  @Override
  public void set(int index, int value) {
    if (LAST_X <= index && index <= LAST_Z) {
      switch (index) {
        case LAST_X -> lastPos = new BlockPos(value, lastPos.getY(), lastPos.getZ());
        case LAST_Y -> lastPos = new BlockPos(lastPos.getX(), value, lastPos.getZ());
        case LAST_Z -> lastPos = new BlockPos(lastPos.getX(), lastPos.getY(), value);
      }
      clearLastListener();
    } else {
      super.set(index, value);
    }
  }

  @Override
  public FuelInfo getFuelInfo() {
    if (fluidHandler == null && lastPos.getY() != NULL_POS.getY()) {
      fluidHandler = getTankHandlers().get(lastPos);
    }
    if (fluidHandler == null || fluidHandler.getFluidInTank(0).isEmpty()) {
      for (IFluidHandler handler : getTankHandlers().values()) {
        if (!handler.getFluidInTank(0).isEmpty()) {
          clearLastListener();
          fluidHandler = handler;
          break;
        }
      }
    }
    FuelInfo info = super.getFuelInfo();
    if (!info.isEmpty()) {
      FluidStack current = info.getFluid();
      for (IFluidHandler handler : getTankHandlers().values()) {
        if (handler != fluidHandler) {
          FluidStack fluid = handler.getFluidInTank(0);
          if (fluid.isEmpty()) info.add(0, handler.getTankCapacity(0));
          else if (current.isFluidEqual(fluid)) info.add(fluid.getAmount(), handler.getTankCapacity(0));
        }
      }
    }
    return info;
  }


  /* Fluid handler */

  /** Gets the most recently used fluid */
  public FluidStack getLastFluid() {
    if (fluidHandler != null) return fluidHandler.getFluidInTank(0);
    BlockPos pos;
    if (lastPos.getY() != NULL_POS.getY()) {
      pos = lastPos;
    } else {
      List<BlockPos> positions = tankSupplier.get();
      if (positions.isEmpty()) return FluidStack.EMPTY;
      pos = positions.get(0);
    }
    IFluidHandler handler = getTankHandlers().get(pos);
    return handler == null ? FluidStack.EMPTY : handler.getFluidInTank(0);
  }

  @Override
  public int getTanks() {
    return tankSupplier.get().size();
  }

  /** Gets the tank at the given index */
  private IFluidHandler getTank(int tank) {
    if (tank >= 0) {
      List<BlockPos> positions = tankSupplier.get();
      if (tank < positions.size()) {
        IFluidHandler handler = getTankHandlers().get(positions.get(tank));
        return handler == null ? EmptyFluidHandler.INSTANCE : handler;
      }
    }
    return EmptyFluidHandler.INSTANCE;
  }

  @Nonnull
  @Override
  public FluidStack getFluidInTank(int tank) {
    return getTank(tank).getFluidInTank(tank);
  }

  @Override
  public int getTankCapacity(int tank) {
    return getTank(tank).getTankCapacity(tank);
  }

  @Override
  public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
    return getTank(tank).isFluidValid(0, stack);
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    int totalFilled = 0;
    resource = resource.copy();
    // try each handler, updating the amount we filled as we go
    // note the map internally is a linked hash map so order is consistent
    for (IFluidHandler handler : getTankHandlers().values()) {
      int filled = handler.fill(resource, action);
      if (filled > 0) {
        // if we finished filling, we are done, return that value
        // this is a quick exit that might save us a copy
        totalFilled += filled;
        if (filled >= resource.getAmount()) {
          break;
        }
        // if this was our first fill, copy the resource
        if (totalFilled == filled) {
          resource = new FluidStack(resource, resource.getAmount() - filled);
        } else {
          resource.shrink(filled);
        }
        // resource will never be empty, as if it was the above break would be hit
      }
    }
    return totalFilled;
  }

  @Nonnull
  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    FluidStack drainedSoFar = FluidStack.EMPTY;
    // try each handler, updating the amount we filled as we go
    // note the map internally is a linked hash map so order is consistent
    for (IFluidHandler handler : getTankHandlers().values()) {
      FluidStack drained = handler.drain(resource, action);
      if (!drained.isEmpty()) {
        // if we managed to drain something, add it into our current drained stack, and decrease the amount we still want to drain
        if (drainedSoFar.isEmpty()) {
          drainedSoFar = drained;
          // if the first success, make a copy of the resource before shrinking it, need to shrink to prevent passing in too much to future hooks
          // though we can skip copying if the first one is all we need
          // note the >= part is just for redundancy, practically its always either = or less than
          if (drained.getAmount() >= resource.getAmount()) {
            break;
          }
          resource = new FluidStack(resource, resource.getAmount() - drained.getAmount());
        } else {
          // resource is guaranteed a copy, and drainedSoFar is a newly created stack, both safe to mutate
          drainedSoFar.grow(drained.getAmount());
          resource.shrink(drained.getAmount());
          // if we drained everything desired, we are done
          if (resource.isEmpty()) {
            break;
          }
        }
      }
    }
    return drainedSoFar;
  }

  @Nonnull
  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    FluidStack drainedSoFar = FluidStack.EMPTY;
    FluidStack toDrain = FluidStack.EMPTY;
    // try each handler, updating the amount we filled as we go
    // note the map internally is a linked hash map so order is consistent
    for (IFluidHandler handler : getTankHandlers().values()) {
      // if we have not drained anything yet, can use typeless hook
      if (toDrain.isEmpty()) {
        FluidStack drained = handler.drain(maxDrain, action);
        if (!drained.isEmpty()) {
          drainedSoFar = drained;
          // if we finished draining, we are done, otherwise we need to create a filter for future drain attempts
          // note the >= part is just for redundancy, practically its always either = or less than
          if (drained.getAmount() >= maxDrain) {
            break;
          }
          toDrain = new FluidStack(drained, maxDrain - drained.getAmount());
        }
      } else {
        // if we already drained some fluid, type sensitive and increase our results
        FluidStack drained = handler.drain(toDrain, action);
        if (!drained.isEmpty()) {
          drainedSoFar.grow(drained.getAmount());
          toDrain.shrink(drained.getAmount());
          // if we drained everything desired, we are done
          if (toDrain.isEmpty()) {
            break;
          }
        }
      }
    }
    return drainedSoFar;
  }
}
