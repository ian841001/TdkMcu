package ian.main;

import java.io.IOException;
import java.util.Date;

import javax.xml.ws.WebServiceException;

import com.pi4j.io.gpio.exception.UnsupportedBoardType;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import ian.main.capture.struct.ExtraPic;
import ian.main.mcu.MCU;
import ian.main.mcu.MCU.MsgIndex;
import ian.main.mcu.MCU.MsgIndex.MsgStruct;
import ian.main.mcu.MwcData;
import ian.main.mcu.MwcSetData;
import ian.main.surveillance.SurveillanceController;

public class MainStart {
	
	
	public static final String CMD_EXIT = "q";
	public static final int BUFFER_SIZE = CMD_EXIT.length();
	
	public static MwcData info = new MwcData();
	public static MwcSetData setRc = new MwcSetData();

	
	
	
	public static byte[] extraInfo = new byte[8];
	
	public static int cycleTime;
	public static int debug0, debug1, debug2, debug3, debug4, debug5, debug6, debug7;
	
	public static MsgStruct msgStruct = MsgIndex.POWER_OFF;
	
	
	public static String extraMsg = "";
	public static ExtraPic captureExtraInfo = new ExtraPic();
	
	private static void print(String info) {
		MainStart.print("Main", info);
	}
	
	
	public static void run(String[] args) {
		print("Setup.");
		byte[] buffer = new byte[BUFFER_SIZE];
		boolean isAlive = true;
		try (SurveillanceController sc = new SurveillanceController().start();
				MCU mcu = new MCU().setup()) {
			long time = new Date().getTime();
			print("Loop.");
			while (isAlive) {
				if (!mcu.loop()) break;
				
				while (System.in.available() > 0) {
					for (int i = 1; i < BUFFER_SIZE; i++) {
						buffer[i - 1] = buffer[i];
					}
					System.in.read(buffer, BUFFER_SIZE - 1, 1);
					
					if (new String(buffer).equals(CMD_EXIT)) {
						isAlive = false;
						break;
					}
				}
				long time2 = new Date().getTime();
				cycleTime = (int) (time2 - time);
				time = time2;
			}
			print("Close.");
		} catch (WebServiceException | IOException | UnsupportedBoardType | InterruptedException | UnsupportedBusNumberException e) {
			e.printStackTrace();
		}
		print("Exit.");
	}
	
	
	public static void main(String[] args) {
		run(args);
//		try {
//			Test.test();
//		} catch (UnsupportedBusNumberException | IOException e) {
//			e.printStackTrace();
//		}
		System.exit(0);
	}
	
	public static void print(String className, String info) {
		System.out.println("[" + className + "]: " + info);
	}
}






























