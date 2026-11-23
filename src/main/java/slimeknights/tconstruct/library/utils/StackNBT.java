package slimeknights.tconstruct.library.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;

/**
 * PORT: {@code ItemStack} no longer has a mutable NBT tag on 1.21; arbitrary mod data lives in the
 * {@link DataComponents#CUSTOM_DATA} data component. Tinkers' tool system (see
 * {@link slimeknights.tconstruct.library.tools.nbt.ToolStack}) is built around reading and mutating a
 * {@link CompoundTag} in place, so this helper reproduces the old {@code ItemStack} NBT methods on top of
 * that component.
 * <p>
 * The tag returned by {@link #getOrCreateTag(ItemStack)} is the component's live backing tag
 * ({@link CustomData#getUnsafe()}), so mutating it mutates the stack, exactly like the old
 * {@code ItemStack#getOrCreateTag()}. That intentionally bypasses the immutability contract of data
 * components; it is the smallest change that keeps the whole NBT-based tool/modifier system working.
 * Callers that only read should prefer {@link #getTag(ItemStack)}.
 */
public final class StackNBT {
  private StackNBT() {}

  /** Gets the mod data tag of the stack, or null if the stack has no mod data */
  @Nullable
  public static CompoundTag getTag(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data == null) {
      return null;
    }
    return data.getUnsafe();
  }

  /** Gets the mod data tag of the stack, creating it if missing. The result is mutable and writes through to the stack. */
  public static CompoundTag getOrCreateTag(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data == null) {
      // CustomData.of copies the tag, so fetch the component back to get the instance the stack actually holds
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()));
      data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
    }
    return data.getUnsafe();
  }

  /**
   * Replacement for the old {@code stack.tag = tag} raw field write. Stores the tag instance itself rather
   * than a copy (which is what {@link CustomData#of(CompoundTag)} would do), so the caller keeps a live
   * handle on the stack's data. Requires the access transformer on {@link CustomData}'s constructor.
   */
  public static void setTagRaw(ItemStack stack, CompoundTag tag) {
    stack.set(DataComponents.CUSTOM_DATA, new CustomData(tag));
  }

  /** Checks if the stack has any mod data */
  public static boolean hasTag(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null && !data.isEmpty();
  }

  /** Sets the mod data tag on the stack, removing the component if null or empty */
  public static void setTag(ItemStack stack, @Nullable CompoundTag tag) {
    if (tag == null || tag.isEmpty()) {
      stack.remove(DataComponents.CUSTOM_DATA);
    } else {
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
  }

  /** Removes the given key from the stack's mod data */
  public static void removeTagKey(ItemStack stack, String key) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data != null && data.contains(key)) {
      CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(key));
    }
  }

  /** Gets a compound child of the stack's mod data, or null if missing */
  @Nullable
  public static CompoundTag getTagElement(ItemStack stack, String key) {
    CompoundTag tag = getTag(stack);
    if (tag == null || !tag.contains(key, Tag.TAG_COMPOUND)) {
      return null;
    }
    return tag.getCompound(key);
  }

  /** Gets a compound child of the stack's mod data, creating both it and the parent tag if missing */
  public static CompoundTag getOrCreateTagElement(ItemStack stack, String key) {
    CompoundTag tag = getOrCreateTag(stack);
    if (!tag.contains(key, Tag.TAG_COMPOUND)) {
      CompoundTag child = new CompoundTag();
      tag.put(key, child);
      return child;
    }
    return tag.getCompound(key);
  }
}
