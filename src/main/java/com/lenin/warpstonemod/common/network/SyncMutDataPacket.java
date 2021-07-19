package com.lenin.warpstonemod.common.network;

import com.lenin.warpstonemod.common.mutations.MutateHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.UUID;

public class SyncMutDataPacket extends WarpPacket<SyncMutDataPacket> {

	public CompoundNBT data;
	//public UUID playerUUID;

	public SyncMutDataPacket () {}

	public SyncMutDataPacket (UUID _playerUUID, CompoundNBT _data) {
		data = _data;
		//playerUUID = _playerUUID;

		//System.out.println("UUID: " + playerUUID.toString() + "   Data: " + data.getInt("instability"));
	}

	@Nonnull
	@Override
	public Encoder<SyncMutDataPacket> encoder() {
		return (packet, buffer) -> {
			//ByteBufUtils.writeUUID(buffer, playerUUID);

			ByteBufUtils.writeNBT(buffer, packet.data);
		};
	}

	@Nonnull
	@Override
	public Decoder<SyncMutDataPacket> decoder() {
		return buffer -> {
			SyncMutDataPacket pkt = new SyncMutDataPacket();

			//pkt.playerUUID = ByteBufUtils.readUUID(buffer);
			pkt.data = ByteBufUtils.readNBT(buffer);

			return pkt;
		};
	}

	@Nonnull
	@Override
	public Handler<SyncMutDataPacket> handler() {
		return new Handler<SyncMutDataPacket>() {
			@Override
			@OnlyIn(Dist.CLIENT)
			public void handleClient(SyncMutDataPacket packet, NetworkEvent.Context context) {
				context.enqueueWork(() -> {
					PlayerEntity p = Minecraft.getInstance().player;
					if (p != null) {
						MutateHelper.updateClientMutations(packet);
					}
				});
			}

			@Override
			public void handle(SyncMutDataPacket packet, NetworkEvent.Context context, LogicalSide side) {}
		};
	}
}