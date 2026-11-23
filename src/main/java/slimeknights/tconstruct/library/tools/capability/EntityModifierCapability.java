package slimeknights.tconstruct.library.tools.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import slimeknights.mantle.registration.deferred.AttachmentTypeDeferredRegister;
import slimeknights.mantle.registration.holder.DeferredAttachment;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Capability to allow an entity to store modifiers, used on projectiles fired from modifiable items */
public class EntityModifierCapability {
  /** Default instance to use with orElse */
  public static final EntityModifiers EMPTY = new EntityModifiers() {
    @Override
    public ModifierNBT getModifiers() {
      return ModifierNBT.EMPTY;
    }

    @Override
    public void setModifiers(ModifierNBT nbt) {}

    @Override
    public void addModifiers(ModifierNBT nbt) {}
  };

  private EntityModifierCapability() {}

  /* Static helpers */

  /** List of predicates to check if the entity supports this capability */
  private static final List<Predicate<Entity>> ENTITY_PREDICATES = new ArrayList<>();

  private static final AttachmentTypeDeferredRegister ATTACHMENTS = new AttachmentTypeDeferredRegister(TConstruct.MOD_ID);
  private static final DeferredAttachment<StoredModifiers> MODIFIERS = ATTACHMENTS.registerType("modifiers", () ->
    AttachmentType.serializable(StoredModifiers::new));

  public static EntityModifiers getCapability(Entity entity) {
    return supportCapability(entity) ? entity.getData(MODIFIERS) : EMPTY;
  }

  public static ModifierNBT getOrEmpty(Entity entity) {
    return entity.hasData(MODIFIERS) ? entity.getData(MODIFIERS).getModifiers() : ModifierNBT.EMPTY;
  }

  /** Checks if the given entity supports this capability */
  public static boolean supportCapability(Entity entity) {
    for (Predicate<Entity> entityPredicate : ENTITY_PREDICATES) {
      if (entityPredicate.test(entity)) {
        return true;
      }
    }
    return false;
  }

  /** Registers a predicate of entites that need this capability */
  public static void registerEntityPredicate(Predicate<Entity> predicate) {
    ENTITY_PREDICATES.add(predicate);
  }

  public static void register(IEventBus bus) {
    ATTACHMENTS.register(bus);
  }

  private static final class StoredModifiers implements EntityModifiers, INBTSerializable<ListTag> {
    private ModifierNBT modifiers = ModifierNBT.EMPTY;

    @Override
    public ModifierNBT getModifiers() {
      return modifiers;
    }

    @Override
    public void setModifiers(ModifierNBT modifiers) {
      this.modifiers = modifiers;
    }

    @Override
    public ListTag serializeNBT(HolderLookup.Provider registries) {
      return modifiers.serializeToNBT();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, ListTag data) {
      modifiers = ModifierNBT.readFromNBT(data);
    }
  }

  /** Interface for callers to use */
  public interface EntityModifiers {
    /** Gets the stored modifiers */
    ModifierNBT getModifiers();

    /** Sets the stored modifiers */
    void setModifiers(ModifierNBT nbt);

    /** Adds additional modifiers to the stored modifiers */
    default void addModifiers(ModifierNBT nbt) {
      ModifierNBT existing = getModifiers();
      if (existing.isEmpty()) {
        setModifiers(nbt);
      } else {
        setModifiers(ModifierNBT.builder().add(existing).add(nbt).build());
      }
    }
  }
}
