package ian.main.capture;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import ian.main.MainStart;

public class CaptureSimulation extends VideoCapture {
	
	private static void print(String info) {
		MainStart.print("CaptureSimulation", info);
	}
	
	public CaptureSimulation() {
		// super();
	}

	public static final String JPG_DIR_PATH = "/home/pi/java/jpg/ten";
	
	private ArrayList<Mat> allPic = new ArrayList<>();
	private int index = 0;

	@Override
	public boolean open(int index) {
		File dir = new File(JPG_DIR_PATH);
		File[] files = dir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().endsWith(".png");
			}
		});
		Arrays.sort(files, new Comparator<File>() {

			public int compare(File o1, File o2) {
				int int1 = Integer.valueOf(o1.getName().split("\\.")[0]);
				int int2 = Integer.valueOf(o2.getName().split("\\.")[0]);
				return int1 - int2;
			}
		});
		for (File file : files) {
			Mat mat = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
			if (mat.empty()) {
				print("skip " + file.getAbsolutePath());
			} else {
				allPic.add(mat);
				print("add " + file.getAbsolutePath());
			}
		}
		print("find " + String.valueOf(allPic.size()) + " pic(s).");
		return !allPic.isEmpty();
	}

	@Override
	public boolean isOpened() {
		return !allPic.isEmpty();
	}

	@Override
	public boolean read(Mat image) {
		allPic.get(index).copyTo(image);
//		print("read index = " + String.valueOf(index));
//		print(image.empty() ? "true" : "false");
//		MainStart.debug7 = index;
		if (++index >= allPic.size()) index = 0;
		return true;
	}

	@Override
	public boolean set(int propId, double value) {
		return true;
	}

	@Override
	public void release() {
		allPic.clear();
	}
	
}
