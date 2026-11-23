package slimeknights.tconstruct.tables.network;

import net.minecraft.network.codec.StreamCodec;
import slimeknights.tconstruct.TConstruct;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraftforge.network.NetworkHooks;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.tables.block.ITabbedBlock;

@RequiredArgsConstructor
public class StationTabPacket implements ISimplePacket {
  private final BlockPos pos;

  public StationTabPacket(RegistryFriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
  }

  @Override
  public void handle(IPayloadContext context) {
    ServerPlayer sender = (ServerPlayer) context.player();
    if (sender != null) {
      ItemStack heldStack = sender.containerMenu.getCarried();
      if (!heldStack.isEmpty()) {
        // set it to empty, so it's doesn't get dropped
        sender.containerMenu.setCarried(ItemStack.EMPTY);
      }

      Level world = sender.getCommandSenderWorld();
      if (!world.hasChunkAt(pos)) {
        return;
      }
      BlockState state = world.getBlockState(pos);
      if (state.getBlock() instanceof ITabbedBlock) {
        ((ITabbedBlock) state.getBlock()).openGui(sender, sender.getCommandSenderWorld(), pos);
      } else {
        MenuProvider provider = state.getMenuProvider(sender.getCommandSenderWorld(), pos);
        if (provider != null) {
          NetworkHooks.openScreen(sender, provider, pos);
        }
      }

      if (!heldStack.isEmpty()) {
        sender.containerMenu.setCarried(heldStack);
        TinkerNetwork.getInstance().sendVanillaPacket(sender, new ClientboundContainerSetSlotPacket(-1, -1, -1, heldStack));
      }
    }
  }
  public static final Type<StationTabPacket> TYPE = new Type<>(TConstruct.getResource("station_tab"));
  public static final StreamCodec<RegistryFriendlyByteBuf, StationTabPacket> CODEC =
    StreamCodec.ofMember(StationTabPacket::encode, StationTabPacket::new);

  @Override
  public Type<StationTabPacket> type() {
    return TYPE;
  }
}
