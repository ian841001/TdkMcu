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
import ian.main.capture.struct.ExtraPic;
import ian.main.capture.struct.Line;
import ian.main.mcu.MCU;

public class CaptureCalculator {
	
	private static void print(String info) {
		MainStart.print("CaptureCalculator", info);
	}
	
	public static void cal(Mat f) {
		Mat edges = new Mat();
		Mat f2 = new Mat();
		Mat hierarchy = new Mat();
		ArrayList<MatOfPoint> contours = new ArrayList<>();
		Imgproc.cvtColor(f, f2, Imgproc.COLOR_BGR2GRAY);
		Imgproc.GaussianBlur(f2, f2, new Size(3, 3), 0, 0);
		Imgproc.Canny(f2, edges, 100, 150, 3, true);
		Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
//		showImage(f, "f" + String.valueOf(index), 100 + index * 50, 100 + index * 50);
//		showImage(edges, "e" + String.valueOf(index), 100 + index * 50, 100 + index * 50);
//		ps.println(contours.size());
//		try {
//			cal(contours, index);
//		 {
//			calLine(contours, index);
		try {
			calRoute(contours);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public static void calRoute(ArrayList<MatOfPoint> contours) {
		MCU.printTime();
		int frameX = 500;
		int frameY = 500;
		ArrayList<Point> points = new ArrayList<>();
		
//		Point R = new Point();
//		Point T = new Point();
//		Point L = new Point();
//		Point B = new Point();
		for(int i=0; i<500; i++) {
			points.clear();
			int Rf = 0;
			int Tf = 0;
			int Lf = 0;
			int Bf = 0;
			for(MatOfPoint contour : contours) {
				for (Point p : contour.toArray()) {
					if(p.x == i && p.y <= frameY-i && p.y >= i && Lf == 0) {
//						L = p;
						Lf = 1;
						points.add(p);
					} else if (p.y == i && p.x <= frameX-i && p.x >= i && Tf == 0) {
//						T = p;
						Tf = 1;
						points.add(p);
					} else if (p.x == frameX-i && p.y <= frameY-i && p.y >= i && Rf == 0){
//						R = p;
						Rf = 1;
						points.add(p);
					} else if (p.y == frameY-i && p.x <= 500-i && p.x >= i && Bf == 0) {
//						B = p;
						Bf = 1;
						points.add(p);
					}
					
				}
			}
			if (Lf+Tf+Rf+Bf>=2) break;
			
		}
		
		ExtraPic ep = new ExtraPic();
		for (Point p : points) {
			ep.addBlueCircle((short)p.x, (short)p.y, (short)10);
		}
		MainStart.captureExtraInfo = ep;
		MCU.printTime();
		System.out.println();
//		ps.printf("%s %s %s %s", R, T, L, B);
		
//		BufferedImage bufferedImage = new BufferedImage(500, 500, BufferedImage.TYPE_3BYTE_BGR);
//		Graphics2D g = bufferedImage.createGraphics();
//		g.setColor(Color.WHITE);
//		for (Point p : points) {
//			drawDot(g, p, 10);
//		}
		
//		showImage(bufferedImage, "result" + String.valueOf(index), 100 + index * 50, 100 + index * 50);
	}
	
	
	public static void calLine(Mat f) {
		int status;
		int deltaX;
		int deltaY;
		double angle;
		if (f.empty()) {
			print("f empty");
			return;
		}
		
		ExtraPic ep = new ExtraPic();
		
		try {
			Mat f2 = new Mat();
			Mat edges = new Mat();
			Mat hierarchy = new Mat();
			
			
			// showImage(f, "f" + String.valueOf(index), 100 + index * 50, 100 + index * 50);
			Imgproc.cvtColor(f, f2, Imgproc.COLOR_BGR2GRAY);
			Imgproc.GaussianBlur(f2, f2, new Size(3, 3), 0, 0);
			Imgproc.Canny(f2, edges, 100, 150, 3, true);
//			storeImage("edges", edges);
			// showImage(f, "f", 100, 100);
			// showImage(f2, "f2", 100, 100);
			// showImage(f3, "f3", 100, 100);
			

			ArrayList<MatOfPoint> contours = new ArrayList<>();
			Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
			
//			for (MatOfPoint contour : contours) {
//				for (Point point : contour.toArray()) {
//					System.out.printf("%.0f, %.0f\n", point.x, point.y);
//				}
//			}
			
			MCU.printTime();
			
//			Point[] max = contours.get(0).toArray();
//			for (int i = 1; i < contours.size(); i++) {
//				Point[] tmp = contours.get(i).toArray();
//				if (max.length < tmp.length) {
//					max = tmp;
//				}
//			}
			
			int maxLen = 0;
			ArrayList<Line> lines = new ArrayList<>();
			for (MatOfPoint contour : contours) {
				Point[] points = contour.toArray();
				int pointsLen = points.length;
				
				if (pointsLen < 10) continue;
				
				int preIndex;
				int nexIndex;
				double[] angle1 = new double[pointsLen];
				double[] angle2 = new double[pointsLen];
				double[] angle3 = new double[pointsLen];
				for (int i = 0; i < pointsLen; i++) {
					ep.addWhitePoint((short)points[i].x, (short)points[i].y);
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
					double detlaAngle = angle1[preIndex] - angle1[nexIndex];
					if (detlaAngle < -180) {
						detlaAngle += 360;
					} else if (detlaAngle > 180) {
						detlaAngle -= 360;
					}
					
					angle2[i] = detlaAngle;
				}
				
				for (int i = 0; i < pointsLen; i++) {
					preIndex = i - 1;
					nexIndex = i + 1;
					if (preIndex < 0) preIndex += pointsLen;
					if (nexIndex >= pointsLen) nexIndex -= pointsLen;
					double deltaAngle = angle2[preIndex] - angle2[nexIndex];
					if (deltaAngle < -180) {
						deltaAngle += 360;
					} else if (deltaAngle > 180) {
						deltaAngle -= 360;
					}
					
					angle3[i] = deltaAngle;
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
				
				double angle0 = lines.get(0).getAngle();
				lines1.add(lines.get(0));
				
				for (int i = 1; i < lines.size(); i++) {
					if (deltaLineAngle(lines.get(i).getAngle(), angle0) < 45) {
						lines1.add(lines.get(i));
					} else {
						lines2.add(lines.get(i));
					}
				}
				if (lines1.isEmpty() || lines2.isEmpty()) {
					throw new Exception();
				}
				
//				MainStart.debug4 = (int) (lines1.get(0).getAngle() * 100);
//				MainStart.debug5 = MainStart.info.captureAngle;
//				MainStart.debug6 = (int) (deltaLineAngle(lines1.get(0).getAngle(), (double)MainStart.info.captureAngle / 100) * 100);
				
//				MainStart.debug4 = 1;
//				MainStart.debug5 = 2;
//				MainStart.debug6 = 3;
				
				
				if (deltaLineAngle(lines1.get(0).getAngle(), (double)MainStart.info.captureAngle / 100) >= 45) {
					ArrayList<Line> tmp = lines1;
					lines1 = lines2;
					lines2 = tmp;
				}
			}
			for (Line line : lines1) {
				ep.addGreenLine((short)line.start.x, (short)line.start.y, (short)line.end.x, (short)line.end.y);
			}
			for (Line line : lines2) {
				ep.addRedLine((short)line.start.x, (short)line.start.y, (short)line.end.x, (short)line.end.y);
			}
			for (Line l : new Line[]{lines1.get(0)}) {//, lines2.get(0)
				short x1, y1, x2, y2;
				if (l.getA() == 0) {
					x1 = 0;
					y1 = (short) (1.0 / l.getB());
					x2 = 500;
					y2 = (short) (1.0 / l.getB());
				} else if (l.getB() == 0) {
					x1 = (short) (1.0 / l.getA());
					y1 = 0;
					x2 = (short) (1.0 / l.getA());
					y2 = 500;
				} else {
					x1 = 0;
					y1 = (short) (1.0 / l.getB());
					x2 = 500;
					y2 = (short) ((1 - l.getA() * 500) / l.getB());
				}
				ep.addYellowLine(x1, y1, x2, y2);
			}
			
			Point intersectionPoint = lines1.get(0).getIntersectionPoint(lines2.get(0));
			ep.addBlueCircle((short)intersectionPoint.x, (short)intersectionPoint.y, (short) 3);
			
			
			
			
			
			
			
			
			status = 1;
			deltaX = (int) Math.round(intersectionPoint.x) - 250;
			deltaY = (int) Math.round(intersectionPoint.y) - 250;
			angle = lines1.get(0).getAngle();
			MainStart.extraMsg = "ok";
		} catch(Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			MainStart.extraMsg = errors.toString();
//			e.printStackTrace();
			
			status = 0;
			deltaX = 0;
			deltaY = 0;
			angle = 0;
		}
		MCU.printTime();
		MainStart.captureExtraInfo = ep;
		
		MCU.printTime();
		
		
		
		
		storeInfo((byte)status, (short)deltaX, (short)deltaY, angle);
		MCU.printTime();
		System.out.println();
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
		angle *= 100;
		// angle range : [-18000,18000]
		
		short angle2 = (short)angle;
		int delta = angle2 - MainStart.info.captureAngle;
		// delta range : [-36000,36000]
		delta = fixCircle(delta, 100);
		// delta range : [-18000,18000]
		if (Math.abs(delta) > 9000) {
			if (angle2 <= 0) {
				angle2 += 18000;
			} else {
				angle2 -= 18000;
			}
		}
		MainStart.info.captureAngle = angle2;
		// System.out.printf("%6.0f %6d %6d\n", angle, angle2, delta);
	}
	
	private static double fixCircle(double angle, int rate) {
		while (angle <= -180 * rate) {
			angle += 360 * rate;
		}
		while (angle > 180 * rate) {
			angle -= 360 * rate;
		}
		return angle;
	}
	private static int fixCircle(int angle, int rate) {
		return (int)fixCircle((double)angle, rate);
	}
	
	// rturn [0,90]
	private static double deltaLineAngle(double angle1, double angle2) {
		double deltaAngle = angle1 - angle2;
		if (deltaAngle < 0) deltaAngle += 360;
		if (deltaAngle >= 180) deltaAngle -= 180;
		if (deltaAngle >= 90) deltaAngle = 180 - deltaAngle;
		return deltaAngle;
	}
	
	
	
//	private static void storeImage(String name, Mat img) {
//		Imgcodecs.imwrite(new File("/home/pi/java/out", name + ".png").getAbsolutePath(), img);
//		print("store " + name);
//	}
//	private static void storeImage(String name, BufferedImage img) {
//		try {
//			ImageIO.write(img, "png", new File("/home/pi/java/out", name + ".png"));
//			print("store " + name);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}
