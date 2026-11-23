package slimeknights.tconstruct.smeltery.network;

import net.minecraft.network.codec.StreamCodec;
import slimeknights.tconstruct.TConstruct;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.BlockEntityPacket;
import slimeknights.tconstruct.smeltery.block.entity.FaucetBlockEntity;

/**
 * Sent to clients to activate the faucet animation clientside.
 * TODO 1.21: make record.
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public class FaucetActivationPacket implements BlockEntityPacket<FaucetBlockEntity> {
  protected final BlockPos pos;
  protected final FluidStack fluid;
  private final boolean isPouring;

  public FaucetActivationPacket(RegistryFriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.fluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
    this.isPouring = buffer.readBoolean();
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluid);
    buffer.writeBoolean(isPouring);
  }

  @Override
  public BlockPos pos() {
    return pos;
  }

  @Override
  public Class<FaucetBlockEntity> blockEntityType() {
    return FaucetBlockEntity.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, FaucetBlockEntity be) {
    be.onActivationPacket(fluid, isPouring);
  }
  public static final Type<FaucetActivationPacket> TYPE = new Type<>(TConstruct.getResource("faucet_activation"));
  public static final StreamCodec<RegistryFriendlyByteBuf, FaucetActivationPacket> CODEC =
    StreamCodec.ofMember(FaucetActivationPacket::encode, FaucetActivationPacket::new);

  @Override
  public Type<FaucetActivationPacket> type() {
    return TYPE;
  }
}
