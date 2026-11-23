package slimeknights.tconstruct.library.tools.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.network.SyncPersistentDataPacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

/** Persistent modifier data, synchronized to the owning player on login, respawn, and dimension change. */
public class PersistentDataCapability {
  private PersistentDataCapability() {}

  private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
    DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TConstruct.MOD_ID);
  private static final DeferredHolder<AttachmentType<?>, AttachmentType<ModDataNBT>> DATA =
    ATTACHMENTS.register("persistent_data", () -> AttachmentType.builder(ModDataNBT::new)
      .serialize(CompoundTag.CODEC.xmap(ModDataNBT::readFromNBT, ModDataNBT::getCopy))
      .copyOnDeath()
      .build());

  public static ModDataNBT getOrWarn(Entity entity) {
    if (entity instanceof LivingEntity || EntityModifierCapability.supportCapability(entity)) {
      return entity.getData(DATA);
    }
    TConstruct.LOG.warn("Missing Tinkers NBT on entity {}", entity.getType());
    return new ModDataNBT();
  }

  /** Read-only callers get empty data when the entity has never used this attachment. */
  public static ModDataNBT getOrEmpty(Entity entity) {
    return entity.hasData(DATA) ? entity.getData(DATA) : new ModDataNBT();
  }

  public static void register(IEventBus bus) {
    ATTACHMENTS.register(bus);
    NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerRespawnEvent.class, event -> sync(event.getEntity()));
    NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerChangedDimensionEvent.class, event -> sync(event.getEntity()));
    NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> sync(event.getEntity()));
  }

  private static void sync(Player player) {
    TinkerNetwork.getInstance().sendTo(new SyncPersistentDataPacket(getOrEmpty(player).getCopy()), player);
  }
}
