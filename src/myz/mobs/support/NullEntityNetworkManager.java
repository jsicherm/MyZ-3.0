/**
 * 
 */
package myz.mobs.support;

import java.net.SocketAddress;

import javax.crypto.SecretKey;

import net.minecraft.server.v1_7_R1.EnumProtocol;
import net.minecraft.server.v1_7_R1.IChatBaseComponent;
import net.minecraft.server.v1_7_R1.NetworkManager;
import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.PacketListener;
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;

/**
 * @author Jordan
 * 
 */
public class NullEntityNetworkManager extends NetworkManager {

	public NullEntityNetworkManager(boolean flag) {
		super(flag);
	}

	@Override
	public void channelActive(ChannelHandlerContext channelhandlercontext) throws Exception {
	}

	@Override
	public void a(EnumProtocol enumprotocol) {
	}

	@Override
	public void channelInactive(ChannelHandlerContext channelhandlercontext) {
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext channelhandlercontext, Throwable throwable) {
	}

	@Override
	protected void a(ChannelHandlerContext channelhandlercontext, Packet packet) {
	}

	@Override
	public void a(PacketListener packetlistener) {
	}

	@Override
	public void handle(Packet packet, GenericFutureListener... agenericfuturelistener) {
	}

	@Override
	public void a() {
	}

	@Override
	public SocketAddress getSocketAddress() {
		return null;
	}

	@Override
	public void a(IChatBaseComponent ichatbasecomponent) {
	}

	@Override
	public boolean c() {
		return false;
	}

	@Override
	public void a(SecretKey secretkey) {
	}

	@Override
	public boolean d() {
		return false;
	}

	@Override
	public PacketListener getPacketListener() {
		return null;
	}

	@Override
	public IChatBaseComponent f() {
		return null;
	}

	@Override
	public void g() {
	}

	@Override
	protected void channelRead0(ChannelHandlerContext channelhandlercontext, Object object) {
	}

	static Channel a(NetworkManager networkmanager) {
		return null;
	}
}
