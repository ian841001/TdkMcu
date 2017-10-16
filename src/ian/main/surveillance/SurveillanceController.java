package ian.main.surveillance;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.xml.ws.WebServiceException;

import com.sun.xml.internal.ws.Closeable;

import ian.main.MainStart;
import ian.main.mcu.MCU;

public class SurveillanceController implements Closeable {
	public static final int LISTEN_PORT = 5987;
	
    private ServerSocket server;
    private Socket socket;
    private Thread thread;
    
    private static void print(String info) {
		MainStart.print("SurveillanceController", info);
	}
    
	public SurveillanceController start() throws IOException {
		server = new ServerSocket(LISTEN_PORT);
		thread = new Thread(new Th());
		thread.start();
		return this;
	}
	
	@Override
	public void close() throws WebServiceException {
		print("Stop listen...");
		try {
			if (socket != null) socket.close();
			server.close();
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}
	
	
	private class Th implements Runnable {
		private InputStream is;
		private OutputStream os;
		
		@Override
		public void run() {
			byte[] cmd = new byte[1];
			
			try {
				while (true) {
					print("Listening at port " + String.valueOf(LISTEN_PORT) + " ...");
					socket = server.accept();
					print("Connected...");
					is = socket.getInputStream();
					os = socket.getOutputStream();
					
					while (!socket.isClosed()) {
						int tmpLen = is.read(cmd);
						if (tmpLen == -1) {
							break;
						} else if (tmpLen == 0) {
							continue;
						}
						processCmd(cmd[0]);
					}
					is.close();
					os.close();
					socket.close();
					print("Disconnected...");
				}
			} catch (SocketException e) {
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		
		private byte[] obj2ByteArray(Object obj) throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
		    ObjectOutputStream os = new ObjectOutputStream(out);
		    os.writeObject(obj);
		    return out.toByteArray();
		}
		
		private void processCmd(byte cmd) throws IOException {
			ByteBuffer byteBuffer;
			int len;
			byte[] allData;
			
			
			byte[] data;
			switch (cmd) {
			case Cmd.CMD_GET_INFO:
				data = obj2ByteArray(MainStart.info);
				break;
			case Cmd.CMD_SET_STATUS:
				data = new byte[]{0};
				len = is.read();
				allData = new byte[len];
				while (is.available() < len);
				is.read(allData);
				byteBuffer = ByteBuffer.wrap(allData).order(ByteOrder.LITTLE_ENDIAN);
				for (int i = 0; i < MainStart.extraInfo.length; i++) {
					MainStart.extraInfo[i] = byteBuffer.get();
				}
				// MainStart.ems = byteBuffer.get();
				break;
			case Cmd.CMD_GET_ROLL_PID:
				byteBuffer = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);
				byteBuffer.putDouble(MCU.rollPID.getKp()).putDouble(MCU.rollPID.getKi()).putDouble(MCU.rollPID.getKd());
				data = byteBuffer.array();
				break;
			case Cmd.CMD_SET_ROLL_PID:
				data = new byte[]{0};
				len = is.read();
				allData = new byte[len];
				while (is.available() < len);
				is.read(allData);
				byteBuffer = ByteBuffer.wrap(allData).order(ByteOrder.LITTLE_ENDIAN);
				MCU.rollPID.setTunings(byteBuffer.getDouble(), byteBuffer.getDouble(), byteBuffer.getDouble());
				break;
			default:
				data = new byte[]{0};
				break;
			}
			os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data.length).array());
			os.write(data);
			os.flush();
//			for (int i = 0; i < data.length; i++) {
//				System.out.printf("%d, ", data[i]);
//			}
//			System.out.println();
		}
		
	}




	
}
