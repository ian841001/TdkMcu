package ian.application;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;

public class McuSocket {
//	public static final String DEFAULT_IP = "127.0.0.1";
//	public static final String DEFAULT_IP = "192.168.2.2";
	public static final String DEFAULT_IP = "192.168.43.63";
//	public static final String DEFAULT_IP = "140.118.48.160";
	
	public static final int DEFAULT_PORT = 5987;
	
	public static final int RESERVET_MILLIS = 1000;
	
	private String ip;
	private int port;
	private Socket sc;
	private InputStream is;
	private OutputStream os;
	
	private Timer t;
	private boolean isReservet;
	
	private int mode;
	public int mode() {
		if (mode == 2 && sc != null && sc.isClosed()) {
			setMode(0);
		}
		return mode;
	};
	private void setMode(int mode) {
		if (this.mode != mode) {
			this.mode = mode;
			modeChangedListener.run();
		}
	}
	private Runnable modeChangedListener;
	public void setModeChangedListener(Runnable modeChangedListener) {
		this.modeChangedListener = modeChangedListener;
	}
	
	
	public void reservet() {
		isReservet = true;
		setMode(3);
		if (t == null) {
			t = new Timer();
		}
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					isReservet = false;
					conn();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, RESERVET_MILLIS);
	}
	private void cancelReservet() {
		isReservet = false;
		if (t != null) {
			t.cancel();
			t = null;
		}
	}
	public void conn(String ip, int port) throws IOException {
		this.ip = ip;
		this.port = port;
		conn();
	}
	
	public void conn() throws IOException {
		if (sc != null && !sc.isClosed()) {
			disc();
		}
		setMode(1);
		
		try {
			InetSocketAddress isa = new InetSocketAddress(ip, port);
			sc = new Socket();
			sc.connect(isa, 1000);
			is = sc.getInputStream();
			os = sc.getOutputStream();
			sc.setSoTimeout(1000);
		} catch (SocketTimeoutException e) {
			if (e.getMessage().equals("connect timed out")) {
				reservet();
			} else {
				throw e;
			}
		} catch (ConnectException e) {
			if (e.getMessage().equals("Connection refused")) {
				reservet();
			} else {
				throw e;
			}
		} finally {
			setMode(sc.isConnected() ? 2 : isReservet ? 3 : 0);
		}
	}
	
	public void disc() throws IOException {
		cancelReservet();
		if (is != null) is.close();
		if (os != null) os.close();
		if (sc != null) sc.close();
		setMode(0);
	}
	public void close() throws IOException {
		disc();
	}
	
	public Object cmdObj(byte cmd) throws IOException, ClassNotFoundException {
	    return new ObjectInputStream(new ByteArrayInputStream(cmd(cmd))).readObject();
	}
	
	public byte[] cmd(byte cmd) throws IOException {
		return cmd(cmd, null);
	}
	
	public byte[] cmd(byte cmd, byte[] data) throws IOException {
		
		os.write(cmd);
		if (data != null) {
			os.write(data.length);
			os.write(data);
		}
		os.flush();
		
		int len;
		int index = 0;
		byte[] buffer = new byte[4];
		byte[] outt;
		int tmp;
		while (true) {
			tmp = is.read(buffer, index, 1);
			if (tmp == 0) continue;
			else if (tmp == -1) throw new IOException("socket closed.");
			if (++index < buffer.length) continue;
			
			
			
			len = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
			
			index = 0;
			outt = new byte[len];
			while (index < len) {
				tmp = is.read(outt, index, outt.length - index);
				if (tmp == -1) throw new IOException();
				index += tmp;
			}
			
			break;
		}
		
		
		return outt;
	}
	
	
}
