package ian.main.test;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import ian.main.led.LedAndOtherController;

public class Test {
	static void print(String info) {
		System.out.println(info);
	}
	public static void test() throws UnsupportedBusNumberException, IOException {
		print("loadLibrary...");
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		print("Setup camera...");
		Core.setErrorVerbosity(false);
		VideoCapture camera = new VideoCapture(0);
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
			return;
		}
		print("Setup complete.");
		
		LedAndOtherController loc = new LedAndOtherController().init();
		
		
		Mat[] mats = new Mat[50];
		
		long time = new Date().getTime();
		long time2;
		for (int i = 0; i < mats.length; i++) {
			loc.setAllLed(i % 10 == 0 ? Color.WHITE : Color.BLACK).updateLed();
			mats[i] = new Mat();
			camera.read(mats[i]);
			time2 = new Date().getTime();
			System.out.println(time2 - time);
			time = time2;
		}
		
		
		
		
		print("Closing camera...");
		camera.release();
		print("Closed.");
		
		print("Saving pics.");
		File dirPath = new File("/home/pi/java/out");
		for (File file : dirPath.listFiles()) {
			file.delete();
		}
		for (int i = 0; i < mats.length; i++) {
			Imgcodecs.imwrite("/home/pi/java/out/" + String.valueOf(i) + ".png", mats[i]);
		}
		print("Saved pics.");
	}
}
