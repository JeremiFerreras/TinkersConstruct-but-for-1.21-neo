package slimeknights.tconstruct.world.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/** Soil block used by Tinkers' slime vegetation; individual plants validate their support blocks. */
public class SlimeDirtBlock extends Block {
  public SlimeDirtBlock(Properties properties) {
    super(properties);
  }
}
