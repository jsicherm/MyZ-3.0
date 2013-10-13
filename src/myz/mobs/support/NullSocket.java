/**
 * 
 */
package myz.mobs.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author kumpelblase2
 * 
 */
public class NullSocket extends Socket {
	private final byte[] buffer = new byte[50];

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(buffer);
	}

	@Override
	public OutputStream getOutputStream() {
		return new ByteArrayOutputStream(10);
	}
}
