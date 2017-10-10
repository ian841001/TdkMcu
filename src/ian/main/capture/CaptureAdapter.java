package ian.main.capture;

import java.io.IOException;

import ian.main.MainStart;
import ian.main.mcu.MCU;

public class CaptureAdapter {
	public static final boolean isSkip = false;

	private static void print(String info) {
		MainStart.print("CaptureAdapter", info);
	}
	
	static {
		print("loadLibrary...");
		System.loadLibrary("CaptureCpp");
	}
	
	private native boolean nativeSetup(int mode);
	private native short[] nativeLoop();
	private native short[] nativeGetData();
	private native boolean nativeClose(int mode);
	
	
	
	public CaptureAdapter setup() throws IOException {
		if (isSkip) return this;
		print("Setup start...");
		if (!nativeSetup(0)) {
			print("Setup fail...");
			throw new IOException("Setup failed...");
		}
		print("Setup complete.");
		return this;
	}
	public void loop() throws IOException {
		MCU.printTime();
		short[] result = nativeLoop();
		MCU.printTime();
		MainStart.info.captureExtraInfo = nativeGetData();
		MCU.printTime();
		byte status = (byte) result[0];
		
		MainStart.info.captureStatus = status;
		if (status != 0) {
			short deltaX = result[1];
			short deltaY = result[2];
			short angle = result[3];
			MainStart.info.captureDeltaX = deltaX;
			MainStart.info.captureDeltaY = deltaY;
			
			
			// angle range : [-18000,18000]
			
			
			int delta = angle - MainStart.info.captureAngle;
			// delta range : [-36000,36000]
			delta = CaptureCalculator.fixCircle(delta, 100);
			// delta range : [-18000,18000]
			if (Math.abs(delta) > 9000) {
				if (angle <= 0) {
					angle += 18000;
				} else {
					angle -= 18000;
				}
			}
			MainStart.info.captureAngle = angle;
		}
		MCU.printTime();
		MCU.printEnter();
	}
	public void close() throws IOException {
		if (isSkip) return;
		print("Closing...");
		if (!nativeClose(0)) {
			print("Close failed...");
			throw new IOException("Close failed...");
		}
		print("Closed.");
	}
}
