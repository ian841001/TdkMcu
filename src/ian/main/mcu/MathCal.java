package ian.main.mcu;

import ian.main.MainStart;

public class MathCal {
	
	private static double[] rollModeVShouldBe;
	
	private static void print(String info) {
		MainStart.print("MathCal", info);
	}
	
	static void preShouldBe() {
		rollModeVShouldBe = new double[640];
		for (int i = 0; i < rollModeVShouldBe.length; i++) {
			rollModeVShouldBe[i] = calShouldBe(i - 320);
		}
	}
	static double getShouldBe(int deltaX) {
		try {
			return rollModeVShouldBe[deltaX + 320];
		} catch (IndexOutOfBoundsException e) {
			print("[WARING]: getShouldBe: deltaX = " + String.valueOf(deltaX));
			return calShouldBe(deltaX);
		}
	}
	static double calShouldBe(int deltaX) {
		double outt = Math.pow(deltaX, 3) / (-80000000);
		if (outt > 0.15) {
			outt = 0.15;
		}
		if (outt < -0.15) {
			outt = -0.15;
		}
		return outt;
	}
}
