package ian.main;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ian.main.mcu.MCU.MsgIndex;
import ian.main.mcu.MCU.MsgIndex.MsgStruct;

public class AllData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final int DATA_LEN = 61;
	public static final int OTHER_DATA_LEN = 12;
	public static final int CAPTURE_DATA_LEN = 7;
	public static final int MCU_DATA_LEN = 4;
	
	
	public short[] motor = new short[8];
    public short[] rc = new short[8];
    public short[] servo = new short[8];
    public short[] att = new short[3];
    public int altEstAlt;
    public short altVario;
    public int altBaro;
    public int altHold;
    public boolean isSonarOk;
    public boolean ok_to_arm;
    public boolean angle_mode;
    public boolean armed;
    public boolean baro_mode;
    public short[] debug = new short[4];
    
    public short sonarFront;
    public short sonarLeft;
    public short sonarRight;
    public short[] extraRc = new short[3];
    
    
    public byte captureStatus;
    public short captureDeltaX;
    public short captureDeltaY;
    public short captureAngle;
    
    public short takeOffHeading;
    public short wantHeading;

    public byte rollMode;
    public byte pitchMode;
    public byte yawMode;
    public byte armMode;
    public byte baroMode;
    
    public int step;
    public int cycleTime;
	public int[] rpiDebug = new int[8];
	public int setWantAlt = 0;
	
	public short tempature;
	
	public MsgStruct msgStruct = MsgIndex.POWER_OFF;
	
	
	public String extraMsg = "";
//	public ExtraPic captureExtraInfo = new ExtraPic();
	public short[] captureExtraInfo;
    
    public AllData setData(byte[] data) {
    	if (data.length != DATA_LEN) {
    		throw new RuntimeException("DATA_LEN = " + String.valueOf(DATA_LEN) + " , data.length = " + String.valueOf(data.length));
    	}
    	ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < motor.length; i++) {
            motor[i] = byteBuffer.getShort();
        }
        for (int i = 0; i < rc.length; i++) {
            rc[i] = byteBuffer.getShort();
        }
        for (int i = 0; i < att.length; i++) {
            att[i] = byteBuffer.getShort();
        }
        altEstAlt = byteBuffer.getInt();
        altVario = byteBuffer.getShort();
        altBaro = byteBuffer.getInt();
        altHold = byteBuffer.getInt();
        for (int i = 0; i < 4; i++) {
        	debug[i] = byteBuffer.getShort();
        }
        
        byte tmp = byteBuffer.get();
        ok_to_arm  = (tmp & (1 << 0)) != 0;
        angle_mode = (tmp & (1 << 1)) != 0;
        armed      = (tmp & (1 << 2)) != 0;
        baro_mode  = (tmp & (1 << 3)) != 0;
        isSonarOk  = (tmp & (1 << 4)) != 0;
        
        return this;
    }
    
//    public byte[] getData() {
//    	ByteBuffer byteBuffer = ByteBuffer.allocate(DATA_LEN).order(ByteOrder.LITTLE_ENDIAN);
//
//        for (int i = 0; i < motor.length; i++) {
//        	byteBuffer.putShort(motor[i]);
//        }
//    	for (int i = 0; i < rc.length; i++) {
//    		byteBuffer.putShort(rc[i]);
//    	}
//    	for (int i = 0; i < att.length; i++) {
//    		byteBuffer.putShort(att[i]);
//    	}
//    	byteBuffer.putInt(altEstAlt);
//    	byteBuffer.putShort(altVario);
//    	byteBuffer.putInt(altHold);
//    	for (int i = 0; i < 4; i++) {
//        	byteBuffer.putShort(debug[i]);
//        }
//    	
//    	byte tmp = 0;
//    	if (ok_to_arm ) tmp |= (1 << 0);
//    	if (angle_mode) tmp |= (1 << 1);
//    	if (armed     ) tmp |= (1 << 2);
//    	if (baro_mode ) tmp |= (1 << 3);
//    	if (isSonarOk ) tmp |= (1 << 4);
//    	byteBuffer.put(tmp);
//    	
//    	return byteBuffer.array();
//    }
    
    public AllData setOtherData(byte[] data) {
    	if (data.length != OTHER_DATA_LEN) {
    		throw new RuntimeException("OTHER_DATA_LEN = " + String.valueOf(OTHER_DATA_LEN) + " , data.length = " + String.valueOf(data.length));
    	}
    	ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    	sonarFront = byteBuffer.getShort();
    	sonarLeft = byteBuffer.getShort();
    	sonarRight = byteBuffer.getShort();
    	for (int i = 0; i < 3; i++) {
    		extraRc[i] = byteBuffer.getShort();
    	}
    	return this;
    }
    
//    public byte[] getOtherData() {
//    	ByteBuffer byteBuffer = ByteBuffer.allocate(OTHER_DATA_LEN).order(ByteOrder.LITTLE_ENDIAN);
//    	
//    	byteBuffer.putShort(sonarFront);
//    	byteBuffer.putShort(sonarLeft);
//    	byteBuffer.putShort(sonarRight);
//    	for (int i = 0; i < 3; i++) {
//    		byteBuffer.putShort(extraRc[i]);
//    	}
//    	
//    	
//    	return byteBuffer.array();
//    }
    
    public AllData setCaptureData(byte[] data) {
    	if (data.length != CAPTURE_DATA_LEN) {
    		throw new RuntimeException("CAPTURE_DATA_LEN = " + String.valueOf(CAPTURE_DATA_LEN) + " , data.length = " + String.valueOf(data.length));
    	}
    	ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    	captureStatus = byteBuffer.get();
    	captureDeltaX = byteBuffer.getShort();
    	captureDeltaY = byteBuffer.getShort();
    	captureAngle = byteBuffer.getShort();
    	return this;
    }
//    public byte[] getCaptureData() {
//    	ByteBuffer byteBuffer = ByteBuffer.allocate(CAPTURE_DATA_LEN).order(ByteOrder.LITTLE_ENDIAN);
//    	
//    	byteBuffer.put(captureStatus);
//    	byteBuffer.putShort(captureDeltaX);
//    	byteBuffer.putShort(captureDeltaY);
//    	byteBuffer.putShort(captureAngle);
//    	
//    	return byteBuffer.array();
//    }
//    
//    public MwcData setMcuData(byte[] data) {
//    	if (data.length != MCU_DATA_LEN) {
//    		throw new RuntimeException("MCU_DATA_LEN = " + String.valueOf(MCU_DATA_LEN) + " , data.length = " + String.valueOf(data.length));
//    	}
//    	ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
//    	rpMode = byteBuffer.get();
//    	yawMode = byteBuffer.get();
//    	armMode = byteBuffer.get();
//    	baroMode = byteBuffer.get();
//    	return this;
//    }
//    public byte[] getMcuData() {
//    	ByteBuffer byteBuffer = ByteBuffer.allocate(MCU_DATA_LEN).order(ByteOrder.LITTLE_ENDIAN);
//    	
//    	byteBuffer.put(rpMode);
//    	byteBuffer.put(yawMode);
//    	byteBuffer.put(armMode);
//    	byteBuffer.put(baroMode);
//    	
//    	return byteBuffer.array();
//    }
    
//    public MwcData setAll(byte[] data) {
//    	if (data.length != ALL_DATA_LEN) {
//    		throw new RuntimeException("ALL_DATA_LEN = " + String.valueOf(ALL_DATA_LEN) + " , data.length = " + String.valueOf(data.length));
//    	}
//    	int index = 0;
//    	byte[] eachdata;
//    	eachdata = new byte[DATA_LEN];
//    	System.arraycopy(data, index, eachdata, 0, eachdata.length);
//    	setData(eachdata);
//    	index += eachdata.length;
//    	
//    	eachdata = new byte[OTHER_DATA_LEN];
//    	System.arraycopy(data, index, eachdata, 0, eachdata.length);
//    	setOtherData(eachdata);
//    	index += eachdata.length;
//    	
//    	eachdata = new byte[CAPTURE_DATA_LEN];
//    	System.arraycopy(data, index, eachdata, 0, eachdata.length);
//    	setCaptureData(eachdata);
//    	index += eachdata.length;
//    	
//    	eachdata = new byte[MCU_DATA_LEN];
//    	System.arraycopy(data, index, eachdata, 0, eachdata.length);
//    	setMcuData(eachdata);
//    	index += eachdata.length;
//    	
//    	return this;
//    }
//    public byte[] getAll() {
//    	ByteBuffer byteBuffer = ByteBuffer.allocate(ALL_DATA_LEN).order(ByteOrder.LITTLE_ENDIAN);
//    	
//    	byteBuffer.put(getData());
//    	byteBuffer.put(getOtherData());
//    	byteBuffer.put(getCaptureData());
//    	byteBuffer.put(getMcuData());
//    	
//    	return byteBuffer.array();
//    }
}
