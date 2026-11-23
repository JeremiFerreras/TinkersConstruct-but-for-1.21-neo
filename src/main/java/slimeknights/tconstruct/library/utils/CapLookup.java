package slimeknights.tconstruct.library.utils;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * PORT: NeoForge 1.21 returns the handler or {@code null} instead of {@code LazyOptional}.
 * Use these helpers in place of {@code te.getCapability(cap, side)}.
 */
public final class CapLookup {
  private CapLookup() {}

  @Nullable
  public static <T, C> T block(@Nullable BlockEntity te, BlockCapability<T, C> cap, @Nullable C context) {
    if (te == null || te.getLevel() == null) {
      return null;
    }
    return te.getLevel().getCapability(cap, te.getBlockPos(), te.getBlockState(), te, context);
  }

  @Nullable
  public static IFluidHandler fluid(@Nullable BlockEntity te, @Nullable Direction side) {
    return block(te, Capabilities.FluidHandler.BLOCK, side);
  }

  @Nullable
  public static IItemHandler item(@Nullable BlockEntity te, @Nullable Direction side) {
    return block(te, Capabilities.ItemHandler.BLOCK, side);
  }

  @Nullable
  public static IItemHandler item(@Nullable Entity entity, @Nullable Direction side) {
    return entity == null ? null : entity.getCapability(Capabilities.ItemHandler.ENTITY, null);
  }

  @Nullable
  public static <T, C> T entity(@Nullable Entity entity, EntityCapability<T, C> cap, @Nullable C context) {
    return entity == null ? null : entity.getCapability(cap, context);
  }

  @Nullable
  public static <T, C> T item(@Nullable ItemStack stack, ItemCapability<T, C> cap, @Nullable C context) {
    return stack == null || stack.isEmpty() ? null : stack.getCapability(cap, context);
  }

  @Nullable
  public static IFluidHandlerItem fluidItem(ItemStack stack) {
    return stack.getCapability(Capabilities.FluidHandler.ITEM);
  }

  @Nullable
  public static IItemHandler itemItem(ItemStack stack) {
    return stack.getCapability(Capabilities.ItemHandler.ITEM);
  }
}
