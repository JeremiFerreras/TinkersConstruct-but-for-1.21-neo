package slimeknights.tconstruct.library.utils;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

/** Compatibility access to legacy arbitrary fluid NBT through 1.21 data components. */
public final class FluidStackNBT {
  private FluidStackNBT() {}

  @Nullable
  public static CompoundTag getTag(FluidStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data == null ? null : data.getUnsafe();
  }

  public static CompoundTag getOrCreateTag(FluidStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data == null) {
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()));
      data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
    }
    return data.getUnsafe();
  }

  public static boolean hasTag(FluidStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null && !data.isEmpty();
  }

  public static void setTag(FluidStack stack, @Nullable CompoundTag tag) {
    if (tag == null || tag.isEmpty()) {
      stack.remove(DataComponents.CUSTOM_DATA);
    } else {
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
  }

  /** Creates a fluid stack carrying legacy custom data. */
  public static FluidStack create(Fluid fluid, int amount, @Nullable CompoundTag tag) {
    FluidStack stack = new FluidStack(fluid, amount);
    setTag(stack, tag);
    return stack;
  }

  /** PORT: FluidStack.loadFluidStackFromNBT(CompoundTag) */
  public static FluidStack parseOptional(HolderLookup.Provider registries, CompoundTag tag) {
    return FluidStack.parseOptional(registries, tag);
  }

  /** PORT: FluidStack.loadFluidStackFromNBT when no lookup is available. */
  public static FluidStack parseOptional(CompoundTag tag) {
    return FluidStack.parseOptional(RegistryAccess.EMPTY, tag);
  }

  /** PORT: FluidStack.writeToNBT(CompoundTag) — returns a new tag, does not mutate the argument. */
  public static CompoundTag saveOptional(FluidStack stack, HolderLookup.Provider registries) {
    Tag tag = stack.saveOptional(registries);
    return tag instanceof CompoundTag compound ? compound : new CompoundTag();
  }

  public static CompoundTag saveOptional(FluidStack stack) {
    return saveOptional(stack, RegistryAccess.EMPTY);
  }
}
