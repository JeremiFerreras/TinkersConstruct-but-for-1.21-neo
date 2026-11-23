package slimeknights.tconstruct.tables.network;

import net.minecraft.network.codec.StreamCodec;
import slimeknights.tconstruct.TConstruct;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.ISimplePacket;
import slimeknights.tconstruct.tables.client.inventory.BaseTabbedScreen;

public class UpdateStationScreenPacket implements ISimplePacket {
  public static final UpdateStationScreenPacket INSTANCE = new UpdateStationScreenPacket();

  private UpdateStationScreenPacket() {}

  public void encode(RegistryFriendlyByteBuf packetBuffer) {}

  @Override
  public void handle(IPayloadContext context) {
    HandleClient.handle();
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle() {
      Screen screen = Minecraft.getInstance().screen;
      if (screen != null) {
        if (screen instanceof BaseTabbedScreen) {
          ((BaseTabbedScreen<?,?>) screen).updateDisplay();
        }
      }
    }
  }
  public static final Type<UpdateStationScreenPacket> TYPE = new Type<>(TConstruct.getResource("update_station_screen"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateStationScreenPacket> CODEC =
    StreamCodec.ofMember(UpdateStationScreenPacket::encode, buf -> UpdateStationScreenPacket.INSTANCE);

  @Override
  public Type<UpdateStationScreenPacket> type() {
    return TYPE;
  }
}
