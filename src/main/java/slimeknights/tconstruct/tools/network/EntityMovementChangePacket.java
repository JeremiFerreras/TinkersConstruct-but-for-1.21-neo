package slimeknights.tconstruct.tools.network;

import net.minecraft.network.codec.StreamCodec;
import slimeknights.tconstruct.TConstruct;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.ISimplePacket;

public class EntityMovementChangePacket implements ISimplePacket {
  private final int entityID;
  private final double x;
  private final double y;
  private final double z;
  private final float yRot;
  private final float xRot;

  public EntityMovementChangePacket(Entity entity) {
    this.entityID = entity.getId();
    this.x = entity.getDeltaMovement().x;
    this.y = entity.getDeltaMovement().y;
    this.z = entity.getDeltaMovement().z;
    this.yRot = entity.getYRot();
    this.xRot = entity.getXRot();
  }

  public EntityMovementChangePacket(RegistryFriendlyByteBuf buffer) {
    this.entityID = buffer.readInt();
    this.x = buffer.readDouble();
    this.y = buffer.readDouble();
    this.z = buffer.readDouble();
    this.yRot = buffer.readFloat();
    this.xRot = buffer.readFloat();
  }

  public void encode(RegistryFriendlyByteBuf packetBuffer) {
    packetBuffer.writeInt(this.entityID);
    packetBuffer.writeDouble(this.x);
    packetBuffer.writeDouble(this.y);
    packetBuffer.writeDouble(this.z);
    packetBuffer.writeFloat(this.yRot);
    packetBuffer.writeFloat(this.xRot);
  }

  @Override
  public void handle(IPayloadContext context) {
    HandleClient.handle(this);
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(EntityMovementChangePacket packet) {
      assert Minecraft.getInstance().level != null;
      Entity entity = Minecraft.getInstance().level.getEntity(packet.entityID);
      if (entity != null) {
        entity.setDeltaMovement(packet.x, packet.y, packet.z);
        entity.setYRot(packet.yRot);
        entity.setXRot(packet.xRot);
      }
    }
  }
  public static final Type<EntityMovementChangePacket> TYPE = new Type<>(TConstruct.getResource("entity_movement_change"));
  public static final StreamCodec<RegistryFriendlyByteBuf, EntityMovementChangePacket> CODEC =
    StreamCodec.ofMember(EntityMovementChangePacket::encode, EntityMovementChangePacket::new);

  @Override
  public Type<EntityMovementChangePacket> type() {
    return TYPE;
  }
}
