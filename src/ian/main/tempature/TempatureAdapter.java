package ian.main.tempature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TempatureAdapter {
	public static final boolean isSkip = false;
	public static final String PYTHON_CMD = "/opt/vc/bin/vcgencmd measure_temp";
	
	public static short getTemp() throws IOException {
		Process proc = Runtime.getRuntime().exec(PYTHON_CMD);
		BufferedReader read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		while (!read.ready());
		byte[] tempStr = read.readLine().getBytes();
		short temp = (short) ((tempStr[5] - 48) * 100 + (tempStr[6] - 48) * 10 + (tempStr[8] - 48));// Short.valueOf(read.readLine().replace("temp=", "").replace("'C", "").replace(".", ""));
		read.close();
		proc.destroy();
		return temp;
	}
	
	
}
