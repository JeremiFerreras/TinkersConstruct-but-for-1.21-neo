package slimeknights.tconstruct.tables.network;

import net.minecraft.network.codec.StreamCodec;
import slimeknights.tconstruct.TConstruct;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;

@RequiredArgsConstructor
public class TinkerStationSelectionPacket implements ISimplePacket {
  private final ResourceLocation layoutName;
  public TinkerStationSelectionPacket(RegistryFriendlyByteBuf buffer) {
    this.layoutName = buffer.readResourceLocation();
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeResourceLocation(this.layoutName);
  }

  @Override
  public void handle(IPayloadContext context) {
    ServerPlayer sender = (ServerPlayer) context.player();
    if (sender != null) {
      AbstractContainerMenu container = sender.containerMenu;
      if (container instanceof TinkerStationContainerMenu tinker) {
        tinker.setToolSelection(StationSlotLayoutLoader.getInstance().get(layoutName));
      }
    }
  }
  public static final Type<TinkerStationSelectionPacket> TYPE = new Type<>(TConstruct.getResource("tinker_station_selection"));
  public static final StreamCodec<RegistryFriendlyByteBuf, TinkerStationSelectionPacket> CODEC =
    StreamCodec.ofMember(TinkerStationSelectionPacket::encode, TinkerStationSelectionPacket::new);

  @Override
  public Type<TinkerStationSelectionPacket> type() {
    return TYPE;
  }
}
