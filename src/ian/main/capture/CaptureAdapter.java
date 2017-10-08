package ian.main.capture;

import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import ian.main.MainStart;

public class CaptureAdapter {
	
	public static final boolean isSkip = false;
	public static final boolean isSimulation = false;
	
	
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
		Core.setErrorVerbosity(false);
		
		camera = isSimulation ? new CaptureSimulation() : new VideoCapture(0);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!camera.open(0)) {
			print("[Warring]: camera.open(0) return false");
		}
		if (!camera.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, 500)) {
			print("[Warring]: camera set width return false");
		}
		if (!camera.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, 500)) {
			print("[Warring]: camera set height return false");
		}
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
		
//		Imgcodecs.imwrite("/home/pi/java/out/" + MainStart.info.altEstAlt + "_" + String.valueOf(new Date().getTime()) + ".png", f);
//		print("cap.");
		CaptureCalculator.cal(f);
	}

	public void close() throws IOException {
		if (isSkip) return;
		print("Closing camera...");
		camera.release();
		print("Closed.");
	}
}
