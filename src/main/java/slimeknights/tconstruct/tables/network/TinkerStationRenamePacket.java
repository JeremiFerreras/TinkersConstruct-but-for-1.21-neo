package slimeknights.tconstruct.tables.network;

import net.minecraft.network.codec.StreamCodec;
import slimeknights.tconstruct.TConstruct;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.ISimplePacket;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;

/** Packet to send to the server to update the name in the UI */
@RequiredArgsConstructor
public class TinkerStationRenamePacket implements ISimplePacket {
  private final String name;

  public TinkerStationRenamePacket(RegistryFriendlyByteBuf buf) {
    this.name = buf.readUtf(Short.MAX_VALUE);
  }

  public void encode(RegistryFriendlyByteBuf buf) {
    buf.writeUtf(name);
  }

  @Override
  public void handle(IPayloadContext context) {
    if (context.player() instanceof ServerPlayer sender && sender.containerMenu instanceof TinkerStationContainerMenu station) {
      TinkerStationBlockEntity tile = station.getTile();
      if (tile != null) {
        station.getTile().setItemName(name);
      }
    }
  }
  public static final Type<TinkerStationRenamePacket> TYPE = new Type<>(TConstruct.getResource("tinker_station_rename"));
  public static final StreamCodec<RegistryFriendlyByteBuf, TinkerStationRenamePacket> CODEC =
    StreamCodec.ofMember(TinkerStationRenamePacket::encode, TinkerStationRenamePacket::new);

  @Override
  public Type<TinkerStationRenamePacket> type() {
    return TYPE;
  }
}
