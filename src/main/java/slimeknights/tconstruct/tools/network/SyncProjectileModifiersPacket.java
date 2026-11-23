package slimeknights.tconstruct.tools.network;

import net.minecraft.network.codec.StreamCodec;
import slimeknights.tconstruct.TConstruct;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.data.loadable.Streamable;
import slimeknights.mantle.network.packet.ISimplePacket;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import java.util.Objects;

public record SyncProjectileModifiersPacket(int entityId, ModifierNBT modifiers, CompoundTag persistentData) implements ISimplePacket {
  private static final Streamable<ModifierNBT> MODIFIER_LIST = ModifierEntry.LOADABLE.list(0).map(ModifierNBT::new, ModifierNBT::getModifiers);

  public SyncProjectileModifiersPacket(Entity entity) {
    this(entity.getId(), EntityModifierCapability.getOrEmpty(entity), PersistentDataCapability.getOrWarn(entity).getCopy());
  }

  public SyncProjectileModifiersPacket(RegistryFriendlyByteBuf buffer) {
    this(buffer.readVarInt(), MODIFIER_LIST.decode(buffer), Objects.requireNonNullElse(buffer.readNbt(), new CompoundTag()));
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeVarInt(entityId);
    MODIFIER_LIST.encode(buffer, modifiers);
    buffer.writeNbt(persistentData);
  }

  @Override
  public void handle(IPayloadContext context) {
    Level level = SafeClientAccess.getLevel();
    if (level != null) {
      Entity entity = level.getEntity(entityId);
      if (entity != null) {
        EntityModifierCapability.getCapability(entity).setModifiers(modifiers);
        PersistentDataCapability.getOrWarn(entity).copyFrom(persistentData);
      }
    }
  }
  public static final Type<SyncProjectileModifiersPacket> TYPE = new Type<>(TConstruct.getResource("sync_projectile_modifiers"));
  public static final StreamCodec<RegistryFriendlyByteBuf, SyncProjectileModifiersPacket> CODEC =
    StreamCodec.ofMember(SyncProjectileModifiersPacket::encode, SyncProjectileModifiersPacket::new);

  @Override
  public Type<SyncProjectileModifiersPacket> type() {
    return TYPE;
  }
}
