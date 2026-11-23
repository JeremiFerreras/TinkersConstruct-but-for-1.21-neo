package slimeknights.tconstruct.gadgets.block;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/** @deprecated tinkers no longer does rails. If you need this reimplement it */
@Deprecated(forRemoval = true)
public class DropperRailBlock extends RailBlock {
  public DropperRailBlock(Properties properties) {
    super(properties);
  }

  @Override
  public void onMinecartPass(BlockState state, Level world, BlockPos pos, AbstractMinecart cart) {
    if (!(cart instanceof Hopper) || cart.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, Direction.DOWN) == null) {
      return;
    }
    BlockPos target = pos.below();
    if (world.getCapability(Capabilities.ItemHandler.BLOCK, target, Direction.DOWN) == null) {
      return;
    }
    IItemHandler itemHandlerCart = cart.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, Direction.UP);
    IItemHandler itemHandlerTE = world.getCapability(Capabilities.ItemHandler.BLOCK, target, Direction.UP);
    if (itemHandlerCart == null || itemHandlerTE == null) {
      return;
    }

    for (int i = 0; i < itemHandlerCart.getSlots(); i++) {
      ItemStack itemStack = itemHandlerCart.extractItem(i, 1, true);
      if (itemStack.isEmpty()) {
        continue;
      }
      if (ItemHandlerHelper.insertItem(itemHandlerTE, itemStack, true).isEmpty()) {
        itemStack = itemHandlerCart.extractItem(i, 1, false);
        ItemHandlerHelper.insertItem(itemHandlerTE, itemStack, false);
        break;
      }
    }
  }

}
