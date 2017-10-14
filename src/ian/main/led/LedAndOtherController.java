package ian.main.led;

import java.awt.Color;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import ian.main.AllData;

public class LedAndOtherController {
	public static final boolean isSkip = false;
	
	private static final int LED_COUNT = 1;
	private static final byte I2C_ADDR = 0x12;
	
	private I2CBus i2c;
	private I2CDevice i2cDevice;
	
	private Color[] ledColors = new Color[LED_COUNT];
	private boolean isChange = false;
	private boolean isThrow = false;
	
	{
		for (int i = 0; i < ledColors.length; i++) {
			ledColors[i] = new Color(0);
		}
	}
	
	
	public LedAndOtherController setLed(int index, Color color) {
		if (isSkip) return this;
		if (ledColors[index].getRGB() != color.getRGB()) {
			ledColors[index] = color;
			isChange = true;
		}
		return this;
	}
	public LedAndOtherController setAllLed(Color color) {
		for (int i = 0; i < LED_COUNT; i++) {
			setLed(i, color);
		}
		return this;
	}
	
	public LedAndOtherController setTurn(boolean isThrow) {
		if (this.isThrow != isThrow) {
			this.isThrow = isThrow;
			isChange = true;
		}
		return this;
	}
	
	
	public LedAndOtherController updateLed() throws IOException {
		if (isSkip) return this;
		if (isChange) {
			isChange = false;
			ByteBuffer buffer = ByteBuffer.allocate(LED_COUNT * 4 + 1).order(ByteOrder.BIG_ENDIAN);
			buffer.put(isThrow ? (byte)1 : (byte)0);
			for (Color ledColor : ledColors) {
				buffer.putInt(ledColor.getRGB());
			}
			i2cDevice.write(buffer.array());
			
		}
		return this;
	}
	public byte[] getSonar() throws IOException {
		byte[] data = new byte[AllData.OTHER_DATA_LEN];
		if (isSkip) return data;
		i2cDevice.read(data, 0, data.length);
		return data;
	}
	public LedAndOtherController init() throws UnsupportedBusNumberException, IOException {
		if (isSkip) return this;
		i2c = I2CFactory.getInstance(I2CBus.BUS_1);
		i2cDevice = i2c.getDevice(I2C_ADDR);
		return this;
	}

	public void close() throws IOException {
		if (isSkip) return;
		setAllLed(Color.BLACK);
		updateLed();
		i2c.close();
	}
}
