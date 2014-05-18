/**
 * 
 */
package myz.nmscode.v1_7_R3.utilities;

import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.EnumProtocol;
import net.minecraft.server.v1_7_R3.IChatBaseComponent;
import net.minecraft.server.v1_7_R3.MinecraftServer;
import net.minecraft.server.v1_7_R3.NetworkManager;
import net.minecraft.server.v1_7_R3.Packet;
import net.minecraft.server.v1_7_R3.PacketPlayInAbilities;
import net.minecraft.server.v1_7_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_7_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_7_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_7_R3.PacketPlayInChat;
import net.minecraft.server.v1_7_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_7_R3.PacketPlayInCloseWindow;
import net.minecraft.server.v1_7_R3.PacketPlayInCustomPayload;
import net.minecraft.server.v1_7_R3.PacketPlayInEnchantItem;
import net.minecraft.server.v1_7_R3.PacketPlayInEntityAction;
import net.minecraft.server.v1_7_R3.PacketPlayInFlying;
import net.minecraft.server.v1_7_R3.PacketPlayInHeldItemSlot;
import net.minecraft.server.v1_7_R3.PacketPlayInKeepAlive;
import net.minecraft.server.v1_7_R3.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_7_R3.PacketPlayInSettings;
import net.minecraft.server.v1_7_R3.PacketPlayInTabComplete;
import net.minecraft.server.v1_7_R3.PacketPlayInTransaction;
import net.minecraft.server.v1_7_R3.PacketPlayInUpdateSign;
import net.minecraft.server.v1_7_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_7_R3.PacketPlayInWindowClick;
import net.minecraft.server.v1_7_R3.PlayerConnection;

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
