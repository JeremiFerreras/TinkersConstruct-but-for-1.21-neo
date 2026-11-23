package slimeknights.tconstruct.smeltery.network;

import net.minecraft.network.codec.StreamCodec;
import slimeknights.tconstruct.TConstruct;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.BlockEntityPacket;
import slimeknights.tconstruct.smeltery.network.FluidUpdatePacket.IFluidPacketReceiver;

/**
 * Packet for when the fluid changes in a block entity.
 * TODO 1.21: make record.
 */
@RequiredArgsConstructor
@ToString
public class FluidUpdatePacket implements BlockEntityPacket<IFluidPacketReceiver> {
  protected final BlockPos pos;
  protected final FluidStack fluid;

  public FluidUpdatePacket(RegistryFriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.fluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluid);
  }

  @Override
  public BlockPos pos() {
    return pos;
  }

  @Override
  public Class<IFluidPacketReceiver> blockEntityType() {
    return IFluidPacketReceiver.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, IFluidPacketReceiver be) {
    be.updateFluidTo(fluid);
  }

  /** Interface to implement for anything wishing to receive fluid updates */
  public interface IFluidPacketReceiver {
    /**
     * Updates the current fluid to the specified value
     * @param fluid New fluidstack
     */
    void updateFluidTo(FluidStack fluid);
  }
  public static final Type<FluidUpdatePacket> TYPE = new Type<>(TConstruct.getResource("fluid_update"));
  public static final StreamCodec<RegistryFriendlyByteBuf, FluidUpdatePacket> CODEC =
    StreamCodec.ofMember(FluidUpdatePacket::encode, FluidUpdatePacket::new);

  @Override
  public Type<FluidUpdatePacket> type() {
    return TYPE;
  }
}
