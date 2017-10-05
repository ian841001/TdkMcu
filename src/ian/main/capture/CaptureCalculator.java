package ian.main.capture;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import ian.main.MainStart;
import ian.main.capture.struct.Line;

public class CaptureCalculator {
	
	public static void cal(Mat f) {
		int status;
		int deltaX;
		int deltaY;
		double angle;
		
		
		try {
			Mat f2 = new Mat();
			Mat edges = new Mat();
			Mat hierarchy = new Mat();
			if (f.empty()) {
				throw new Exception("farme is empty");
			}
			Imgproc.cvtColor(f, f2, Imgproc.COLOR_BGR2GRAY);
			Imgproc.GaussianBlur(f2, f2, new Size(3, 3), 0, 0);
			Imgproc.Canny(f2, edges, 250, 150, 3, true);

			ArrayList<MatOfPoint> contours = new ArrayList<>();
			Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
			
			int maxLen = 0;
			ArrayList<Line> lines = new ArrayList<>();
			for (MatOfPoint contour : contours) {
				Point[] points = contour.toArray();
				int pointsLen = points.length;
				
				if (pointsLen < 6) continue;
				
				int preIndex;
				int nexIndex;
				double[] angle1 = new double[pointsLen];
				double[] angle2 = new double[pointsLen];
				for (int i = 0; i < pointsLen; i++) {
					preIndex = i - 2;
					nexIndex = i + 2;
					if (preIndex < 0) preIndex += pointsLen;
					if (nexIndex >= pointsLen) nexIndex -= pointsLen;
					angle1[i] = Line.getAngle(points[preIndex], points[nexIndex]);
				}
				
				for (int i = 0; i < pointsLen; i++) {
					preIndex = i - 1;
					nexIndex = i + 1;
					if (preIndex < 0) preIndex += pointsLen;
					if (nexIndex >= pointsLen) nexIndex -= pointsLen;
					double deltaAngle = angle1[preIndex] - angle1[nexIndex];
					if (deltaAngle < -180) {
						deltaAngle += 360;
					} else if (deltaAngle > 180) {
						deltaAngle -= 360;
					}
					
					angle2[i] = deltaAngle;
				}
				
				preIndex = 0;
				nexIndex = 0;
				
				for (int i = 0; i < pointsLen; i++) {
					if (preIndex >= pointsLen) preIndex -= pointsLen;
					if (nexIndex >= pointsLen) nexIndex -= pointsLen;
					if (Math.abs(angle2[nexIndex]) >= 20) {
						int deltaIndex = nexIndex - preIndex;
						if (deltaIndex < 0) deltaIndex += pointsLen;
						if (deltaIndex > 0) {
							lines.add(new Line(points[preIndex], points[nexIndex], deltaIndex));
							if (maxLen < deltaIndex) {
								maxLen = deltaIndex;
							}
						}
						preIndex = nexIndex + 1;
					}
					nexIndex++;
				}
			}
			
			for (int i = 0; i < lines.size();) {
				if (lines.get(i).len < maxLen / 2) {
					lines.remove(i);
				} else {
					i++;
				}
			}
			
			
			
			
			ArrayList<Line> lines1 = new ArrayList<>();
			ArrayList<Line> lines2 = new ArrayList<>();
			if (true) {
				int lineLenMax1 = 0;
				int lineLenMax2 = 0;
				
				double angle0 = lines.get(0).getAngle();
				lines1.add(lines.get(0));
				lineLenMax1 = lines.get(0).len;
				
				for (int i = 1; i < lines.size(); i++) {
					double deltaAngle = lines.get(i).getAngle() - angle0;
					if (deltaAngle < 0) deltaAngle += 360;
					if (deltaAngle >= 180) deltaAngle -= 180;
					if (deltaAngle >= 90) deltaAngle = 180 - deltaAngle;
					if (deltaAngle < 45) {
						lines1.add(lines.get(i));
						if (lineLenMax1 < lines.get(i).len) {
							lineLenMax1 = lines.get(i).len;
						}
					} else {
						lines2.add(lines.get(i));
						if (lineLenMax2 < lines.get(i).len) {
							lineLenMax2 = lines.get(i).len;
						}
					}
				}
				if (lineLenMax1 < lineLenMax2) {
					ArrayList<Line> tmp = lines1;
					lines1 = lines2;
					lines2 = tmp;
				}
			}
			
			Point intersectionPoint = lines1.get(0).getIntersectionPoint(lines2.get(0));
			
			status = 1;
			deltaX = (int) Math.round(intersectionPoint.x) - 250;
			deltaY = (int) Math.round(intersectionPoint.y) - 250;
			angle = lines1.get(0).getAngle();
			MainStart.extraMsg = "ok";
		} catch(Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			MainStart.extraMsg = errors.toString();
			e.printStackTrace();
			
			status = 0;
			deltaX = 0;
			deltaY = 0;
			angle = 0;
		}
		
		
		
		
		
		
		
		
		storeInfo((byte)status, (short)deltaX, (short)deltaY, angle);
	}
	
	public static void storeInfo(byte status, Short deltaX, Short deltaY, double angle) {
		MainStart.info.captureStatus = status;
		if (status == 0) return;
		MainStart.info.captureDeltaX = deltaX;
		MainStart.info.captureDeltaY = deltaY;
		
		
		
		// angle range : [-180.00,180.00]
		if (angle < 0) {
			angle += 180;
		}
		// angle range : [0.00,180.00]
		angle -= 90;
		// angle range : [-90.00,90.00]
		angle *= 100;
		// angle range : [-9000,9000]
		
		short angle2 = (short) angle;
		int delta = angle2 - MainStart.info.captureAngle;
		// delta range : [-36000,36000]
		if (delta < -18000) {
			delta += 36000;
		}
		if (delta > 18000) {
			delta -= 36000;
		}
		// delta range : [-18000,18000]
		if (Math.abs(delta) > 9000) {
			if (angle2 < 0) {
				angle2 += 18000;
			}
			if (angle2 > 0) {
				angle2 -= 18000;
			}
		}
		MainStart.info.captureAngle = angle2;
	}
}
