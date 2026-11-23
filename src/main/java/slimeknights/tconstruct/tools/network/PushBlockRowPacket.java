package slimeknights.tconstruct.tools.network;

import net.minecraft.network.codec.StreamCodec;
import slimeknights.tconstruct.TConstruct;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.library.modifiers.fluid.block.MoveBlocksFluidEffect;

/** Packet handling {@link MoveBlocksFluidEffect} syncing to the client */
public record PushBlockRowPacket(BlockPos pos, Direction direction, boolean push, int moving) implements ISimplePacket {
  public PushBlockRowPacket(RegistryFriendlyByteBuf buffer) {
    this(buffer.readBlockPos(), buffer.readEnum(Direction.class), buffer.readBoolean(), buffer.readVarInt());
  }

  /** Gets the facing value for this packet */
  private Direction facing() {
    return push ? direction : direction.getOpposite();
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    buffer.writeEnum(direction);
    buffer.writeBoolean(push);
    buffer.writeVarInt(moving);
  }

  @Override
  public void handle(IPayloadContext context) {
    HandleClient.handle(this);
  }

  /** Accesses client only safely */
  private static class HandleClient {
    public static void handle(PushBlockRowPacket packet) {
      Level level = Minecraft.getInstance().level;
      if (level != null) {
        MoveBlocksFluidEffect.moveBlocks(level, packet.pos, level.getBlockState(packet.pos), packet.facing(), packet.direction, packet.moving);
      }
    }
  }
  public static final Type<PushBlockRowPacket> TYPE = new Type<>(TConstruct.getResource("push_block_row"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PushBlockRowPacket> CODEC =
    StreamCodec.ofMember(PushBlockRowPacket::encode, PushBlockRowPacket::new);

  @Override
  public Type<PushBlockRowPacket> type() {
    return TYPE;
  }
}
