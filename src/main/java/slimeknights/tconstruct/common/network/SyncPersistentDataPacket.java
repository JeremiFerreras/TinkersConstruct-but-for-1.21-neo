package slimeknights.tconstruct.common.network;

import net.minecraft.network.codec.StreamCodec;
import slimeknights.tconstruct.TConstruct;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;

/** Packet to sync player persistent data to the client */
@RequiredArgsConstructor
public class SyncPersistentDataPacket implements ISimplePacket {
  private final CompoundTag data;

  public SyncPersistentDataPacket(RegistryFriendlyByteBuf buffer) {
    data = buffer.readNbt();
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeNbt(data);
  }

  @Override
  public void handle(IPayloadContext context) {
    HandleClient.handle(this);
  }

  /** Handles client side only code safely */
  private static class HandleClient {
    private static void handle(SyncPersistentDataPacket packet) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
        PersistentDataCapability.getOrWarn(player).copyFrom(packet.data);
      }
    }
  }
  public static final Type<SyncPersistentDataPacket> TYPE = new Type<>(TConstruct.getResource("sync_persistent_data"));
  public static final StreamCodec<RegistryFriendlyByteBuf, SyncPersistentDataPacket> CODEC =
    StreamCodec.ofMember(SyncPersistentDataPacket::encode, SyncPersistentDataPacket::new);

  @Override
  public Type<SyncPersistentDataPacket> type() {
    return TYPE;
  }
}
