package ian.main.mcu;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import javax.xml.ws.WebServiceException;

import com.pi4j.io.gpio.exception.UnsupportedBoardType;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.sun.xml.internal.ws.Closeable;

import ian.main.AllData;
import ian.main.MainStart;
import ian.main.capture.CaptureAdapter;
import ian.main.led.LedAndOtherController;
import ian.main.serial.MwcSerialAdapter;
import ian.main.serial.exception.DataNotReadyException;
import ian.main.serial.exception.NoConnectedException;
import ian.main.serial.exception.TimeOutException;
import ian.main.serial.exception.UnknownErrorException;
import ian.main.tempature.TempatureAdapter;

public class MCU implements Closeable {
	
	/* 高度誤差範圍 */
	static final int altError = 20;
	
	static MwcSerialAdapter mwc;
	static LedAndOtherController loc;
	static CaptureAdapter ca;
	// static TempLed led;
	
	static AllData info = MainStart.info;
	static MwcSetData setRc = MainStart.setRc;
	
	
	
	static int ledStep = 0;
	
	

	
	static class ControlMode {
		static final byte RELEASE = 0;
		static final byte STOP = 1;
		static final byte WORK = 2;
	}
	
	
	// ------------------throttle-------------------
	public static int throttleHoldValue;
	static int throttleValue = 0;
	// ------------------throttle-------------------
	
	
	// ------------------time-----------------------
	static long mwcErrorTime = 0;
	static long ledUpdateTime = 0;
	static long ledStepTime = 0;
	// ------------------time-----------------------
	
	
	// ------------------error flag-----------------
	static int mwcError = 0;
	static int ledError = 0;
	static int modeError = 0;
	static int pyError = 0;
	// ------------------error flag-----------------
	
	
	// ------------------mode flag------------------
	static int ledMode = 0;
	// ------------------mode flag------------------
	
	static boolean pwr = false;
	static boolean btn = false;
	
	
	
	private static void print(String info) {
		MainStart.print("MCU", info);
	}
	
	static int choose(int index, int defaultData, int... data) {
		return index < data.length ? data[index] : defaultData;
	}
	public static class MsgIndex {
		public static class MsgStruct implements Serializable {
			private static final long serialVersionUID = 1L;
			
			public final int level;
			public final String msgStr;
			public MsgStruct(int level, String msgStr) {
				this.level = level;
				this.msgStr = msgStr;
			}
		}
		public static final MsgStruct POWER_OFF      = new MsgStruct(0, "系統釋放");
		public static final MsgStruct STOP           = new MsgStruct(0, "等待");
		public static final MsgStruct RUN            = new MsgStruct(0, "動作中");
		
		public static final MsgStruct WAIT_MODE_1    = new MsgStruct(1, "等待 ok_to_arm...");
		public static final MsgStruct WAIT_MODE_2    = new MsgStruct(1, "等待 angle_mode...");
		public static final MsgStruct WAIT_MODE_3    = new MsgStruct(1, "等待 ok_to_arm 和 angle_mode...");
		
		public static final MsgStruct READY_FLY      = new MsgStruct(0, "準備起飛");
		public static final MsgStruct FORCE_FLY      = new MsgStruct(0, "起飛中");
		public static final MsgStruct WAIT_TO_BARO   = new MsgStruct(0, "等待至定高點");
		
		public static final MsgStruct FOLLOW_LINE    = new MsgStruct(0, "循線中");
		
		public static final MsgStruct LANDING        = new MsgStruct(0, "降落中");
		public static final MsgStruct LANDED         = new MsgStruct(0, "降落完成");
		
		
		public static final MsgStruct CAN_NOT_FLY    = new MsgStruct(2, "無法起飛");
		
		
	}
	
	
	static void stl() {
		switch (info.step) {
		case 0:
			info.step = 1;
			info.armMode = ControlMode.STOP;
			info.baroMode = ControlMode.STOP;
//			info.yawMode = ControlMode.STOP;
//			info.rpMode = ControlMode.STOP;
			MainStart.info.msgStruct = MsgIndex.STOP;
			
			
//			info.yawMode = ControlMode.WORK;
//			info.rpMode = ControlMode.WORK;
			break;
		case 1:
			if (btn) {
				info.step = 2;
				info.msgStruct = MsgIndex.RUN;
			}
//			info.yawMode = info.extraRc[2] > 1700 ? ControlMode.WORK : ControlMode.RELEASE;
//			info.rpMode = info.extraRc[2] > 1300 ? ControlMode.WORK : ControlMode.RELEASE;
			break;
		case 2:
			switch ((info.ok_to_arm ? 1 : 0) + (info.angle_mode ? 2 : 0)) {
			case 3: info.msgStruct = MsgIndex.RUN; info.step = 10; break;
			case 2: info.msgStruct = MsgIndex.WAIT_MODE_1; break;
			case 1: info.msgStruct = MsgIndex.WAIT_MODE_2; break;
			case 0: info.msgStruct = MsgIndex.WAIT_MODE_3; break;
			default: throw new RuntimeException("unknown error.");
			}
			break;
		case 10: // 解鎖油門
			info.armMode = ControlMode.WORK;
			throttleValue = 1098;
			if (info.armed) {
				info.msgStruct = MsgIndex.READY_FLY;
				createTimer();
				info.step = 11;				
			}
			break;
		case 11:
			if (timerOn(1500)) {
				info.step = 12;
				throttleValue = 1500;
				info.msgStruct = MsgIndex.FORCE_FLY;
			}
			break;
		case 12: // 起飛
			if (info.altEstAlt < 10) {
				if (throttleValue < 1850) {
					throttleValue += 10;
				} else {
					info.step = 500;
				}
			} else if (info.altEstAlt > 20) {
				throttleValue -= 30;
				throttleHoldValue = throttleValue;
				createTimer();
				info.step = 1012;
			}
			break;
		case 1012:
			if (timerOn(100)) {
				info.step = 13;
				info.msgStruct = MsgIndex.WAIT_TO_BARO;
			}
			break;
		case 13: // 設定高度200cm
			info.baroMode = ControlMode.WORK;
			if (info.baro_mode) {
				info.setWantAlt = 80;
				info.step = 14;
			}
			break;
		case 14: // 等待至200
			if (Math.abs(info.altEstAlt - info.setWantAlt) <= altError) {
				info.step = 15;
				info.msgStruct = MsgIndex.FOLLOW_LINE;
			}
			break;
		case 15:
			createTimer();
			info.step = 16;
			break;
		case 16:
			if (btn) {
				info.step = 100;
				info.yawMode = ControlMode.RELEASE;
				info.rpMode = ControlMode.RELEASE;
			}
			info.yawMode = info.extraRc[0] > 1700 ? ControlMode.WORK : ControlMode.RELEASE;
			info.rpMode = info.extraRc[2] > 1700 ? ControlMode.WORK : ControlMode.RELEASE;
			break;
		case 100: // 終點降落
			info.setWantAlt = 0;
			info.step = 101;
			info.msgStruct = MsgIndex.LANDING;
			break;
		case 101:
			if (info.altEstAlt < 5) {
				info.step = 102;
			}
			break;
		case 102: // 上鎖油門
			throttleValue = 1098;
			info.armMode = ControlMode.STOP;
			info.baroMode = ControlMode.STOP;
			info.msgStruct = MsgIndex.LANDED;
			break;
		case 500:
			throttleValue = 1098;
			info.armMode = ControlMode.STOP;
			info.baroMode = ControlMode.STOP;
			info.msgStruct = MsgIndex.CAN_NOT_FLY;
			break;
		default:
			break;
		}
	}
	void mode() {
		
		setRc.setAux1(choose(info.armMode , 0, 0, 1098, 1898));
		
		setRc.setAux3(choose(info.baroMode, 0, 0, 1500, 1898));
		
		if (info.baro_mode) {
			int offset = info.setWantAlt - info.altHold;
			
			if (Math.abs(offset) < 0) {
				offset = 0;
			}
			
			setRc.setThrottle(offset + throttleHoldValue);
		} else {
			setRc.setThrottle(throttleValue);
		}
		
		switch (info.yawMode) {
		case ControlMode.RELEASE:
			setRc.setYaw(0);
			break;
		case ControlMode.STOP:
			setRc.setYaw(1500);
			break;
		case ControlMode.WORK:
			int tmp = (int) (1500 + info.captureAngle * 0.1);
			if (tmp > 1900) {
				tmp = 1900;
			}
			if (tmp < 1100) {
				tmp = 1100;
			}
			setRc.setYaw(tmp);
			break;
		default:
			setRc.setYaw(0);
			break;
		}
		
		switch (info.rpMode) {
		case ControlMode.RELEASE:
			setRc.setRoll(0);
			setRc.setPitch(0);
			break;
		case ControlMode.STOP:
			setRc.setRoll(1500);
			setRc.setPitch(1500);
			break;
		case ControlMode.WORK:
			int tmp;
			
			tmp = (int) (1500 - getCm(info.captureDeltaX) * 1);
			if (tmp > 1900) {
				tmp = 1900;
			}
			if (tmp < 1100) {
				tmp = 1100;
			}
			setRc.setRoll(tmp);
			
			tmp = (int) (1500 + getCm(info.captureDeltaY) * 1);
			if (tmp > 1900) {
				tmp = 1900;
			}
			if (tmp < 1100) {
				tmp = 1100;
			}
			setRc.setPitch(tmp);
			break;
		default:
			setRc.setRoll(0);
			setRc.setPitch(0);
			break;
		}
		
		
		switch (ledMode) {
		case 1:
			loc.setAllLed(Color.BLACK);
			if (getTime() - ledStepTime > 200) {
				ledStepTime = getTime();
				loc.setLed(ledStep, Color.RED);
				if (++ledStep >= 2) {
					ledStep = 0;
				}
			}
			break;
		case 2:
			loc.setAllLed(Color.GREEN);
			break;
		case 3:
			loc.setLed(0, Color.GREEN).setLed(1, Color.RED);
			break;
		default:
			loc.setAllLed(Color.BLACK);
			break;
		}
		
	}
	
	static long time;
	static void createTimer() {
		time = getTime();
	}
	static boolean timerOn(long millis) {
		return getTime() - time >= millis;
	}
	
	static long getTime() {
		return new Date().getTime();
	}
	
	
	void extra() {
		
		
//		yawFixAngle = MainStart.extraInfo[0] - info.att[2];
//		
//		if (yawFixAngle < -180) {
//			yawFixAngle += 360;
//		}
//		if (yawFixAngle > 180) {
//			yawFixAngle -= 360;
//		}
		
//		MainStart.debug0 = MainStart.extraInfo[0];
//		MainStart.debug1 = info.att[2];
//		
//		MainStart.debug2 = yawFixAngle;
//		
//		MainStart.debug3 = yawMode;
	}
	
	
	public MCU setup() throws UnsupportedBoardType, IOException, InterruptedException, UnsupportedBusNumberException {
		ca = new CaptureAdapter().setup();
		mwc = new MwcSerialAdapter().open();
		loc = new LedAndOtherController().init();
		return this;
	}
	
	static long printTime1 = new Date().getTime();
	public static void printTime() {
		long printTime2 = new Date().getTime();
		System.out.printf("%5d , ", printTime2 - printTime1);
		printTime1 = printTime2;
	}
	
	public boolean loop() {
		
		try {
			ca.loop();
			pyError = 0;
		} catch (IOException e) {
			pyError++;
			e.printStackTrace();
		}
		
		try {
			info.tempature = TempatureAdapter.getTemp();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			info.setData(mwc.getRpi(setRc.getData()));
			mwcError = 0;
		} catch(NoConnectedException | TimeOutException | DataNotReadyException | UnknownErrorException | IOException e) {
			if (mwcError == 0) {
				mwcErrorTime = getTime();
			}
			mwcError++;
			e.printStackTrace();
		}
		
		setRc.reset();
		
		if (getTime() - ledUpdateTime > 250) {
			try {
				info.setOtherData(loc.getSonar());
				if (ledError != 0) {
					print("conn led IO");
				}
				ledError = 0;
			} catch(IOException e) {
				if (e.getMessage().equals("Remote I/O error")) {
					if (ledError == 0) {
						print("lost led IO");
					}
				} else {
					e.printStackTrace();
				}
				ledError++;
			}
			ledUpdateTime = getTime();
		}
		
		
		
		
		extra();
		
		
		pwr = info.extraRc[1] > 1300;
		btn = info.extraRc[1] > 1700;
		
		if (pwr) {
			try {
				stl();
				mode();
				modeError = 0;
			} catch(Exception e) {
				modeError++;
				e.printStackTrace();
			}
		} else {
			info.step = 0;
			info.msgStruct = MsgIndex.POWER_OFF;
			info.armMode = ControlMode.RELEASE;
			info.baroMode = ControlMode.RELEASE;
			info.yawMode = ControlMode.RELEASE;
			info.rpMode = ControlMode.RELEASE;
		}
		
//		MainStart.debug0 = setRc.getRoll();
//		MainStart.debug1 = setRc.getPitch();
//		MainStart.debug2 = setRc.getYaw();
		
		setRc.setRoll(0);
		setRc.setPitch(0);
		setRc.setYaw(0);
		
		
		
		
		
		
		
		if (modeError != 0 || pyError != 0 || (mwcError != 0 && (getTime() - mwcErrorTime > 800))) {
			System.out.printf("modeError = %d\npyError = %d\nmwcError = %d\n", modeError, pyError, mwcError);
			try {
				setRc.reset();
				mwc.setRc(setRc.getData());
			} catch (DataNotReadyException | NoConnectedException | TimeOutException | UnknownErrorException | IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		try {
			loc.updateLed();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public void close() throws WebServiceException {
		try {
			ca.close();
		} catch (IOException e) {
			print("[Close]: [ca]:");
			e.printStackTrace();
		}
		try {
			mwc.close();
		} catch (IllegalStateException | IOException e) {
			print("[Close]: [mwc]:");
			e.printStackTrace();
		}
		try {
			loc.close();
		} catch (IOException e) {
			print("[Close]: [loc]:");
			e.printStackTrace();
		}
		
		
	}
	public static double getCm(short pixel) {
		return pixel * (info.altEstAlt + 6) / 535.6;
	}
}
