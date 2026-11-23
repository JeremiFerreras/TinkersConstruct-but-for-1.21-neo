package slimeknights.tconstruct.tools.network;

import net.minecraft.network.codec.StreamCodec;
import slimeknights.tconstruct.TConstruct;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.tools.menu.ToolContainerMenu;

/** Packet used when a fluid is changed inside a tool container menu */
public record ToolContainerFluidUpdatePacket(FluidStack fluid) implements ISimplePacket {
  public ToolContainerFluidUpdatePacket(RegistryFriendlyByteBuf buffer) {
    this(FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer));
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluid);
  }

  @Override
  public void handle(IPayloadContext context) {
    Player player = SafeClientAccess.getPlayer();
    if (player != null && player.containerMenu instanceof ToolContainerMenu toolMenu) {
      toolMenu.getTank().setFluid(fluid);
    }
  }
  public static final Type<ToolContainerFluidUpdatePacket> TYPE = new Type<>(TConstruct.getResource("tool_container_fluid_update"));
  public static final StreamCodec<RegistryFriendlyByteBuf, ToolContainerFluidUpdatePacket> CODEC =
    StreamCodec.ofMember(ToolContainerFluidUpdatePacket::encode, ToolContainerFluidUpdatePacket::new);

  @Override
  public Type<ToolContainerFluidUpdatePacket> type() {
    return TYPE;
  }
}
