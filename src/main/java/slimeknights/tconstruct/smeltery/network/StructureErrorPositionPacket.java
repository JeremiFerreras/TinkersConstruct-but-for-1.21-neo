package slimeknights.tconstruct.smeltery.network;

import net.minecraft.network.codec.StreamCodec;
import slimeknights.tconstruct.TConstruct;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.BlockEntityPacket;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;

import javax.annotation.Nullable;

/**
 * Packet to tell a multiblock to render a specific position as the cause of the error
 */
@RequiredArgsConstructor
public class StructureErrorPositionPacket implements BlockEntityPacket<HeatingStructureBlockEntity> {
  private final BlockPos controllerPos;
  @Nullable
  private final BlockPos errorPos;

  public StructureErrorPositionPacket(RegistryFriendlyByteBuf buffer) {
    this.controllerPos = buffer.readBlockPos();
    if (buffer.readBoolean()) {
      this.errorPos = buffer.readBlockPos();
    } else {
      this.errorPos = null;
    }
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeBlockPos(controllerPos);
    if (errorPos != null) {
      buffer.writeBoolean(true);
      buffer.writeBlockPos(errorPos);
    } else {
      buffer.writeBoolean(false);
    }
  }

  @Override
  public BlockPos pos() {
    return controllerPos;
  }

  @Override
  public Class<HeatingStructureBlockEntity> blockEntityType() {
    return HeatingStructureBlockEntity.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, HeatingStructureBlockEntity be) {
    be.setErrorPos(errorPos);
  }
  public static final Type<StructureErrorPositionPacket> TYPE = new Type<>(TConstruct.getResource("structure_error_position"));
  public static final StreamCodec<RegistryFriendlyByteBuf, StructureErrorPositionPacket> CODEC =
    StreamCodec.ofMember(StructureErrorPositionPacket::encode, StructureErrorPositionPacket::new);

  @Override
  public Type<StructureErrorPositionPacket> type() {
    return TYPE;
  }
}
