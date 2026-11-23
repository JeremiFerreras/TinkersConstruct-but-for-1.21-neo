package slimeknights.tconstruct.tools.logic;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.events.ToolEquipmentChangeEvent;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability.ComputableDataKey;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.EnumMap;
import java.util.Map;

/**
 * Tracks player equipment on the client and dispatches modifier equipment-change hooks.
 * Server-side changes are handled directly by NeoForge's LivingEquipmentChangeEvent.
 */
public final class EquipmentChangeWatcher {
  private EquipmentChangeWatcher() {}

  private static final ComputableDataKey<PlayerLastEquipment> LAST_EQUIPMENT =
    TConstruct.createKey("equipment_watcher", PlayerLastEquipment::new);

  /** Registers the equipment listeners. */
  public static void register() {
    NeoForge.EVENT_BUS.addListener(EquipmentChangeWatcher::onEquipmentChange);
    if (FMLEnvironment.dist == Dist.CLIENT) {
      NeoForge.EVENT_BUS.addListener(EquipmentChangeWatcher::onPlayerTick);
    }
  }

  /** Server and client equipment changes. */
  private static void onEquipmentChange(LivingEquipmentChangeEvent event) {
    runModifierHooks(event.getEntity(), event.getSlot(), event.getFrom(), event.getTo());
  }

  /** Client-only fallback for changes that are not exposed through the equipment-change event. */
  private static void onPlayerTick(PlayerTickEvent.Post event) {
    Player player = event.getEntity();
    if (player.level().isClientSide) {
      TinkerDataCapability.getOptional(player)
        .ifPresent(data -> data.computeIfAbsent(LAST_EQUIPMENT).update(player));
    }
  }

  /** Shared modifier hook logic. */
  private static void runModifierHooks(LivingEntity entity, EquipmentSlot changedSlot, ItemStack original, ItemStack replacement) {
    EquipmentChangeContext context = new EquipmentChangeContext(entity, changedSlot, original, replacement);

    // First notify modifiers that an item was removed.
    IToolStackView tool = context.getOriginalTool();
    if (tool != null && ModifierUtil.validArmorSlot(tool, changedSlot)) {
      for (ModifierEntry entry : tool.getModifierList()) {
        entry.getHook(ModifierHooks.EQUIPMENT_CHANGE).onUnequip(tool, entry, context);
      }
    }

    // Then notify modifiers that an item was equipped.
    tool = context.getReplacementTool();
    if (tool != null && ModifierUtil.validArmorSlot(tool, changedSlot)) {
      for (ModifierEntry entry : tool.getModifierList()) {
        entry.getHook(ModifierHooks.EQUIPMENT_CHANGE).onEquip(tool, entry, context);
      }
    }

    // Finally notify equipped tools in every other slot.
    for (EquipmentSlot otherSlot : EquipmentSlot.values()) {
      if (otherSlot != changedSlot) {
        tool = context.getValidTool(otherSlot);
        if (tool != null) {
          for (ModifierEntry entry : tool.getModifierList()) {
            entry.getHook(ModifierHooks.EQUIPMENT_CHANGE).onEquipmentChange(tool, entry, context, otherSlot);
          }
        }
      }
    }

    NeoForge.EVENT_BUS.post(new ToolEquipmentChangeEvent(context));
  }

  /** Client-side equipment snapshot used to detect inventory changes. */
  private static final class PlayerLastEquipment {
    private final Map<EquipmentSlot,ItemStack> lastItems = new EnumMap<>(EquipmentSlot.class);

    private PlayerLastEquipment() {
      for (EquipmentSlot slot : EquipmentSlot.values()) {
        lastItems.put(slot, ItemStack.EMPTY);
      }
    }

    private void update(Player player) {
      for (EquipmentSlot slot : EquipmentSlot.values()) {
        ItemStack current = player.getItemBySlot(slot);
        ItemStack previous = lastItems.get(slot);
        if (!ItemStack.matches(previous, current)) {
          lastItems.put(slot, current.copy());
          runModifierHooks(player, slot, previous, current);
        }
      }
    }
  }
}
