package ian.main.capture;

import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import ian.main.MainStart;

public class CaptureAdapter {
	
	public static final boolean isSkip = false;
	private VideoCapture camera;
	
	static {
		if (!isSkip) {
			print("loadLibrary...");
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		}
	}
	
	
	private static void print(String info) {
		MainStart.print("CaptureAdapter", info);
	}
	
	public CaptureAdapter setup() throws IOException {
		if (isSkip) return this;
		print("Setup camera...");
		camera = new VideoCapture();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		print(camera.open(0) ? "true" : "false");
		
//		print(camera.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, 500) ? "true" : "false");
//		print(camera.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, 500) ? "true" : "false");
		if (!camera.isOpened()) {
			print("Setup failed.");
			throw new IOException("Setup failed.");
		}
		print("Setup complete.");
        return this;
	}
	public void loop() throws IOException {
		if (isSkip) return;
		Mat f = new Mat();
		camera.read(f);
		CaptureCalculator.cal(f);
		
	}

	public void close() throws IOException {
		if (isSkip) return;
		print("Closing camera...");
		camera.release();
		print("Closed.");
	}
}
