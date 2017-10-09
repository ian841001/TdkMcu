package ian.main.capture;

import java.io.IOException;

import ian.main.MainStart;

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
		nativeLoop();
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
