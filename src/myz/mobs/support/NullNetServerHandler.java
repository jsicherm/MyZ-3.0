/**
 * 
 */
package myz.mobs.support;

import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.EnumProtocol;
import net.minecraft.server.v1_7_R1.IChatBaseComponent;
import net.minecraft.server.v1_7_R1.MinecraftServer;
import net.minecraft.server.v1_7_R1.NetworkManager;
import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.PacketPlayInAbilities;
import net.minecraft.server.v1_7_R1.PacketPlayInArmAnimation;
import net.minecraft.server.v1_7_R1.PacketPlayInBlockDig;
import net.minecraft.server.v1_7_R1.PacketPlayInBlockPlace;
import net.minecraft.server.v1_7_R1.PacketPlayInChat;
import net.minecraft.server.v1_7_R1.PacketPlayInClientCommand;
import net.minecraft.server.v1_7_R1.PacketPlayInCloseWindow;
import net.minecraft.server.v1_7_R1.PacketPlayInCustomPayload;
import net.minecraft.server.v1_7_R1.PacketPlayInEnchantItem;
import net.minecraft.server.v1_7_R1.PacketPlayInEntityAction;
import net.minecraft.server.v1_7_R1.PacketPlayInFlying;
import net.minecraft.server.v1_7_R1.PacketPlayInHeldItemSlot;
import net.minecraft.server.v1_7_R1.PacketPlayInKeepAlive;
import net.minecraft.server.v1_7_R1.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_7_R1.PacketPlayInSettings;
import net.minecraft.server.v1_7_R1.PacketPlayInTabComplete;
import net.minecraft.server.v1_7_R1.PacketPlayInTransaction;
import net.minecraft.server.v1_7_R1.PacketPlayInUpdateSign;
import net.minecraft.server.v1_7_R1.PacketPlayInUseEntity;
import net.minecraft.server.v1_7_R1.PacketPlayInWindowClick;
import net.minecraft.server.v1_7_R1.PlayerConnection;

/**
 * @author kumpelblase2
 * 
 */
public class NullNetServerHandler extends PlayerConnection {

	public NullNetServerHandler(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
		super(minecraftserver, networkmanager, entityplayer);
	}

	@Override
	public void a() {
	}

	@Override
	public void a(double d0, double d1, double d2, float f, float f1) {

	}

	@Override
	public void a(EnumProtocol enumprotocol, EnumProtocol enumprotocol1) {
	}

	@Override
	public void a(IChatBaseComponent ichatbasecomponent) {
	}

	@Override
	public void a(PacketPlayInAbilities packet) {
	}

	@Override
	public void a(PacketPlayInArmAnimation packet) {
	}

	@Override
	public void a(PacketPlayInBlockDig packet) {
	}

	@Override
	public void a(PacketPlayInBlockPlace packet) {
	}

	@Override
	public void a(PacketPlayInChat packet) {
	}

	@Override
	public void a(PacketPlayInClientCommand packet) {
	}

	@Override
	public void a(PacketPlayInCloseWindow packet) {
	}

	@Override
	public void a(PacketPlayInCustomPayload packet) {
	}

	@Override
	public void a(PacketPlayInEnchantItem packet) {
	}

	@Override
	public void a(PacketPlayInEntityAction packet) {
	}

	@Override
	public void a(PacketPlayInFlying packet) {
	}

	@Override
	public void a(PacketPlayInHeldItemSlot packet) {
	}

	@Override
	public void a(PacketPlayInKeepAlive packet) {
	}

	@Override
	public void a(PacketPlayInSetCreativeSlot packet) {
	}

	@Override
	public void a(PacketPlayInSettings packet) {
	}

	@Override
	public void a(PacketPlayInTabComplete packet) {
	}

	@Override
	public void a(PacketPlayInTransaction packet) {
	}

	@Override
	public void a(PacketPlayInUpdateSign packet) {
	}

	@Override
	public void a(PacketPlayInUseEntity packet) {
	}

	@Override
	public void a(PacketPlayInWindowClick packet) {
	}

	@Override
	public void disconnect(String s) {
	}

	@Override
	public void sendPacket(Packet packet) {
	}
}
