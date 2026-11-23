package slimeknights.tconstruct.common;


import net.minecraft.core.registries.Registries;
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
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.CastingBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.CastingTankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.ChannelBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.FluidCannonBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.HeaterBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.ProxyTankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.DuctBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.SmelteryInputOutputBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.TankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.AlloyerBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.MelterBlockEntity;
import slimeknights.tconstruct.smeltery.item.CopperCanFluidHandler;
import slimeknights.tconstruct.smeltery.item.CopperCanItem;
import slimeknights.tconstruct.smeltery.item.TankItem;
import slimeknights.tconstruct.smeltery.item.TankItemFluidHandler;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.block.entity.chest.AbstractChestBlockEntity;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;
import slimeknights.tconstruct.tables.block.entity.table.ModifierWorktableBlockEntity;
import slimeknights.tconstruct.tables.block.entity.table.PartBuilderBlockEntity;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;

/**
 * Central RegisterCapabilitiesEvent wiring for items and block entities.
 */
public final class TinkerCapabilityEvents {
  private TinkerCapabilityEvents() {}

  public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
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
    event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new ConstantFluidContainerWrapper(
      new FluidStack(TinkerFluids.powderedSnow.get(), FluidType.BUCKET_VOLUME), stack, Items.BUCKET.getDefaultInstance()), Items.POWDER_SNOW_BUCKET);

    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.melter.get(), (be, side) -> be.getTank());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.melter.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.alloyer.get(), (be, side) -> be.getTank());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.smeltery.get(), (be, side) -> be.getMeltingInventory());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.foundry.get(), (be, side) -> be.getMeltingInventory());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.tank.get(), (be, side) -> be.getTank());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.basin.get(), (be, side) -> be.getTank());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.table.get(), (be, side) -> be.getTank());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.channel.get(), ChannelBlockEntity::getExposedFluidHandler);
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.castingTank.get(), (be, side) -> be.getTank());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.heater.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.fluidCannon.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.fluidCannon.get(), (be, side) -> be.getTank());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.proxyTank.get(), (be, side) -> be.getItemTank());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.drain.get(), (be, side) -> be.getCachedHandler());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.chute.get(), (be, side) -> be.getCachedHandler());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.duct.get(), (be, side) -> be.getCachedHandler());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.duct.get(), (be, side) -> be.getItemHandler());

    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerTables.craftingStationTile.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerTables.tinkerStationTile.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerTables.partBuilderTile.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerTables.modifierWorktableTile.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerTables.tinkersChestTile.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerTables.partChestTile.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerTables.castChestTile.get(), (be, side) -> be.getItemHandler());
  }
}
