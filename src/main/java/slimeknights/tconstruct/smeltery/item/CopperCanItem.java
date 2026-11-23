package slimeknights.tconstruct.smeltery.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.tconstruct.library.recipe.FluidValues;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import slimeknights.mantle.registration.deferred.DataComponentTypeDeferredRegister;
import slimeknights.mantle.registration.holder.DeferredDataComponent;

import java.util.List;
import java.util.function.Supplier;
import java.util.function.Consumer;

/**
 * Fluid container holding 1 ingot of fluid
 */
public class CopperCanItem extends Item {
  static final String FLUID_ID = "item.tconstruct.tank.fluid_id";

  private static final DataComponentTypeDeferredRegister COMPONENTS =
    new DataComponentTypeDeferredRegister(Registries.DATA_COMPONENT_TYPE, "tconstruct");
  private static final DeferredDataComponent<SimpleFluidContent> CONTENTS =
    COMPONENTS.registerPersistent("can_contents", SimpleFluidContent.CODEC, SimpleFluidContent.STREAM_CODEC);

  public CopperCanItem(Properties properties) {
    super(properties);
  }

  public static void register(IEventBus bus, Supplier<? extends Item> can) {
    COMPONENTS.register(bus);
    bus.addListener(RegisterCapabilitiesEvent.class, event ->
      event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new CopperCanFluidHandler(stack), can.get()));
  }

  @Override
  public boolean hasCraftingRemainingItem(ItemStack stack) {
    return getFluid(stack) != Fluids.EMPTY;
  }

  @Override
  public ItemStack getCraftingRemainingItem(ItemStack stack) {
    if (hasCraftingRemainingItem(stack)) {
      return new ItemStack(this);
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    FluidStack fluid = contents(stack).copy();
    if (fluid.isEmpty()) {
      tooltip.add(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    } else {
      tooltip.add(Component.translatable(getDescriptionId() + ".contents", fluid.getHoverName()).withStyle(ChatFormatting.GRAY));
      if (flag.isAdvanced()) {
        tooltip.add(Component.translatable(FLUID_ID, BuiltInRegistries.FLUID.getKey(fluid.getFluid())).withStyle(ChatFormatting.DARK_GRAY));
      }
    }
  }

  static SimpleFluidContent contents(ItemStack stack) {
    return stack.getOrDefault(CONTENTS.get(), SimpleFluidContent.EMPTY);
  }

  public static void removeFluid(ItemStack stack) {
    stack.remove(CONTENTS.get());
  }

  /** Each can holds one ingot. The handler accounts for the number of cans during transfers. */
  public static ItemStack setFluid(ItemStack stack, FluidStack fluid) {
    if (fluid.isEmpty()) {
      removeFluid(stack);
    } else {
      stack.set(CONTENTS.get(), SimpleFluidContent.copyOf(fluid.copyWithAmount(FluidValues.INGOT)));
    }
    return stack;
  }

  public static ItemStack setFluid(ItemStack stack, ResourceLocation fluid, @Nullable CompoundTag tag) {
    return setFluid(stack, BuiltInRegistries.FLUID.get(fluid), tag);
  }

  public static ItemStack setFluid(ItemStack stack, Fluid fluid, @Nullable CompoundTag tag) {
    if (fluid == null || fluid == Fluids.EMPTY) {
      removeFluid(stack);
      return stack;
    }
    FluidStack contents = new FluidStack(fluid, FluidValues.INGOT);
    if (tag != null) {
      contents.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    return setFluid(stack, contents);
  }

  @Nullable
  public static CompoundTag getFluidTag(ItemStack stack) {
    CustomData data = contents(stack).get(DataComponents.CUSTOM_DATA);
    return data == null ? null : data.copyTag();
  }

  public static Fluid getFluid(ItemStack stack) {
    return contents(stack).getFluid();
  }

  /** Adds filled variants of the copper can to the given consumer */
  @SuppressWarnings("deprecation")
  public static void addFilledVariants(Item can, TagKey<Fluid> hidden, Consumer<ItemStack> output) {
    BuiltInRegistries.FLUID.holders().filter(holder -> {
      Fluid fluid = holder.value();
      return fluid.isSource(fluid.defaultFluidState()) && !holder.is(hidden);
    }).forEachOrdered(holder -> {
      output.accept(CopperCanItem.setFluid(new ItemStack(can), new FluidStack(holder, FluidValues.INGOT)));
    });
  }

  public static String getSubtype(ItemStack stack) {
    Fluid fluid = getFluid(stack);
    return fluid == Fluids.EMPTY ? "" : BuiltInRegistries.FLUID.getKey(fluid).toString();
  }
}
