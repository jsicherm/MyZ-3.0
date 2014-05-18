/**
 * 
 */
package myz.nmscode.v1_7_R3.utilities;

import java.net.SocketAddress;

import javax.crypto.SecretKey;

import net.minecraft.server.v1_7_R3.EnumProtocol;
import net.minecraft.server.v1_7_R3.IChatBaseComponent;
import net.minecraft.server.v1_7_R3.NetworkManager;
import net.minecraft.server.v1_7_R3.Packet;
import net.minecraft.server.v1_7_R3.PacketListener;
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

	static Channel a(NetworkManager networkmanager) {
		return null;
	}

	@Override
	public void a() {
	}

	@Override
	public void a(EnumProtocol enumprotocol) {
	}

	@Override
	public void a(PacketListener packetlistener) {
	}

	@Override
	public void a(SecretKey secretkey) {
	}

	@Override
	public boolean c() {
		return false;
	}

	@Override
	public void channelActive(ChannelHandlerContext channelhandlercontext) throws Exception {
	}

	@Override
	public void channelInactive(ChannelHandlerContext channelhandlercontext) {
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext channelhandlercontext, Throwable throwable) {
	}

	@Override
	public IChatBaseComponent f() {
		return null;
	}

	@Override
	public void g() {
	}

	@Override
	public PacketListener getPacketListener() {
		return null;
	}

	@Override
	public SocketAddress getSocketAddress() {
		return null;
	}

	@Override
	public void handle(Packet packet, GenericFutureListener... agenericfuturelistener) {
	}

	@Override
	protected void a(ChannelHandlerContext channelhandlercontext, Packet packet) {
	}

	@Override
	protected void channelRead0(ChannelHandlerContext channelhandlercontext, Object object) {
	}
}
