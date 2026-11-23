package slimeknights.tconstruct.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.library.materials.definition.UpdateMaterialsPacket;
import slimeknights.tconstruct.library.materials.stats.UpdateMaterialStatsPacket;
import slimeknights.tconstruct.library.materials.traits.UpdateMaterialTraitsPacket;
import slimeknights.tconstruct.library.modifiers.UpdateModifiersPacket;
import slimeknights.tconstruct.library.modifiers.fluid.UpdateFluidEffectsPacket;
import slimeknights.tconstruct.library.tools.definition.UpdateToolDefinitionDataPacket;
import slimeknights.tconstruct.library.tools.layout.UpdateTinkerSlotLayoutsPacket;
import slimeknights.tconstruct.shared.network.GeneratePartTexturesPacket;
import slimeknights.tconstruct.smeltery.network.ChannelFlowPacket;
import slimeknights.tconstruct.smeltery.network.FaucetActivationPacket;
import slimeknights.tconstruct.smeltery.network.FluidUpdatePacket;
import slimeknights.tconstruct.smeltery.network.SmelteryFluidClickedPacket;
import slimeknights.tconstruct.smeltery.network.SmelteryTankUpdatePacket;
import slimeknights.tconstruct.smeltery.network.StructureErrorPositionPacket;
import slimeknights.tconstruct.smeltery.network.StructureUpdatePacket;
import slimeknights.tconstruct.tables.network.StationTabPacket;
import slimeknights.tconstruct.tables.network.TinkerStationRenamePacket;
import slimeknights.tconstruct.tables.network.TinkerStationSelectionPacket;
import slimeknights.tconstruct.tables.network.UpdateCraftingRecipePacket;
import slimeknights.tconstruct.tables.network.UpdateStationScreenPacket;
import slimeknights.tconstruct.tables.network.UpdateTinkerStationRecipePacket;
import slimeknights.tconstruct.tools.network.EntityMovementChangePacket;
import slimeknights.tconstruct.tools.network.InteractWithAirPacket;
import slimeknights.tconstruct.tools.network.PushBlockRowPacket;
import slimeknights.tconstruct.tools.network.SyncProjectileModifiersPacket;
import slimeknights.tconstruct.tools.network.TinkerControlPacket;
import slimeknights.tconstruct.tools.network.ToolContainerFluidUpdatePacket;

import javax.annotation.Nullable;

/**
 * Base network class for all tinkers logic
 * <p>
 * In general, if you need to send packets you should use your own network class
 */
public final class TinkerNetwork {
  private static final TinkerNetwork INSTANCE = new TinkerNetwork();

  private TinkerNetwork() {}

  public static TinkerNetwork getInstance() {
    return INSTANCE;
  }

  public static void registerPackets(RegisterPayloadHandlersEvent event) {
    var registrar = event.registrar("6");
    // shared
    registrar.playToClient(InventorySlotSyncPacket.TYPE, InventorySlotSyncPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateNeighborsPacket.TYPE, UpdateNeighborsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(GeneratePartTexturesPacket.TYPE, GeneratePartTexturesPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(SyncPersistentDataPacket.TYPE, SyncPersistentDataPacket.CODEC, ISimplePacket.handle());

    // gadgets
    registrar.playToClient(EntityMovementChangePacket.TYPE, EntityMovementChangePacket.CODEC, ISimplePacket.handle());

    // tables
    registrar.playToServer(StationTabPacket.TYPE, StationTabPacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(TinkerStationRenamePacket.TYPE, TinkerStationRenamePacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateCraftingRecipePacket.TYPE, UpdateCraftingRecipePacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(TinkerStationSelectionPacket.TYPE, TinkerStationSelectionPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateTinkerSlotLayoutsPacket.TYPE, UpdateTinkerSlotLayoutsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateStationScreenPacket.TYPE, UpdateStationScreenPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateTinkerStationRecipePacket.TYPE, UpdateTinkerStationRecipePacket.CODEC, ISimplePacket.handle());

    // tools
    registrar.playToClient(UpdateMaterialsPacket.TYPE, UpdateMaterialsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateMaterialStatsPacket.TYPE, UpdateMaterialStatsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateMaterialTraitsPacket.TYPE, UpdateMaterialTraitsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateToolDefinitionDataPacket.TYPE, UpdateToolDefinitionDataPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(ToolContainerFluidUpdatePacket.TYPE, ToolContainerFluidUpdatePacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(SyncProjectileModifiersPacket.TYPE, SyncProjectileModifiersPacket.CODEC, ISimplePacket.handle());

    // modifiers
    registrar.playToServer(TinkerControlPacket.TYPE, TinkerControlPacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(InteractWithAirPacket.TYPE, InteractWithAirPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateModifiersPacket.TYPE, UpdateModifiersPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateFluidEffectsPacket.TYPE, UpdateFluidEffectsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(PushBlockRowPacket.TYPE, PushBlockRowPacket.CODEC, ISimplePacket.handle());

    // smeltery
    registrar.playToClient(FluidUpdatePacket.TYPE, FluidUpdatePacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(FaucetActivationPacket.TYPE, FaucetActivationPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(ChannelFlowPacket.TYPE, ChannelFlowPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(SmelteryTankUpdatePacket.TYPE, SmelteryTankUpdatePacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(StructureUpdatePacket.TYPE, StructureUpdatePacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(SmelteryFluidClickedPacket.TYPE, SmelteryFluidClickedPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(StructureErrorPositionPacket.TYPE, StructureErrorPositionPacket.CODEC, ISimplePacket.handle());
  }

  /**
   * Sends a vanilla packet to the given player
   * @param player  Player
   * @param packet  Packet
   */
  public void sendVanillaPacket(Entity player, Packet<?> packet) {
    if (player instanceof ServerPlayer serverPlayer) {
      serverPlayer.connection.send(packet);
    }
  }

  /**
   * Same as {@link #sendToClientsAround(Object, ServerLevel, BlockPos)}, but checks that the world is a serverworld
   * @param msg       Packet to send
   * @param world     World instance
   * @param position  Target position
   */
  public void sendToClientsAround(CustomPacketPayload msg, @Nullable LevelAccessor world, BlockPos position) {
    if (world instanceof ServerLevel server) {
      PacketDistributor.sendToPlayersTrackingChunk(server, new ChunkPos(position), msg);
    }
  }

  /**
   * Sends a packet to all entities tracking the given entity
   * @param msg     Packet
   * @param entity  Entity to check
   */
  public void sendToTrackingAndSelf(CustomPacketPayload msg, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, msg);
  }

  /**
   * Sends a packet to all entities tracking the given entity
   * @param msg     Packet
   * @param entity  Entity to check
   */
  public void sendToTracking(CustomPacketPayload msg, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntity(entity, msg);
  }

  /**
   * Sends a packet to the whole player list
   * @param targetedPlayer  Main player to target, if null uses whole list
   * @param playerList      Player list to use if main player is null
   * @param msg             Message to send
   */
  public void sendToPlayerList(@Nullable ServerPlayer targetedPlayer, PlayerList playerList, CustomPacketPayload msg) {
    if (targetedPlayer != null) {
      sendTo(msg, targetedPlayer);
    } else {
      for (ServerPlayer player : playerList.getPlayers()) {
        sendTo(msg, player);
      }
    }
  }
  public void sendTo(CustomPacketPayload msg, Entity player) {
    if (player instanceof ServerPlayer serverPlayer) {
      PacketDistributor.sendToPlayer(serverPlayer, msg);
    }
  }

  public void sendToServer(CustomPacketPayload msg) {
    PacketDistributor.sendToServer(msg);
  }
}
