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
	
	static long rollModeLastTime;
	static double rollModePreX;
	
	
	static int yawModeOffset;
	
	
	static boolean isTimeing;
	static boolean isThrow;
	static int statusCount;
	
	
	
	
	static boolean pwr = false;
	static boolean btn = false;
	
	public static PID rollPID;
	
	private static void print(String info) {
		MainStart.print("MCU", info);
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
		public static final MsgStruct POWER_OFF             = new MsgStruct(0, "系統釋放");
		public static final MsgStruct STOP                  = new MsgStruct(0, "等待");
		public static final MsgStruct RUN                   = new MsgStruct(0, "動作中");
		
		public static final MsgStruct WAIT_MODE_1           = new MsgStruct(1, "等待 ok_to_arm...");
		public static final MsgStruct WAIT_MODE_2           = new MsgStruct(1, "等待 angle_mode...");
		public static final MsgStruct WAIT_MODE_3           = new MsgStruct(1, "等待 ok_to_arm 和 angle_mode...");
		
		public static final MsgStruct READY_FLY             = new MsgStruct(0, "準備起飛");
		public static final MsgStruct FORCE_FLY             = new MsgStruct(0, "起飛中");
		public static final MsgStruct SET_BARO              = new MsgStruct(0, "設定定高點");
		
		public static final MsgStruct FOLLOW_LINE           = new MsgStruct(0, "循線中");
		public static final MsgStruct TURN_LEFT             = new MsgStruct(0, "左轉中");
		public static final MsgStruct SHY                   = new MsgStruct(0, "避障中");
		
		
		
		public static final MsgStruct FOLLOW_LINE_ST        = new MsgStruct(0, "循線至起飛線");
		public static final MsgStruct FOLLOW_LINE_STE       = new MsgStruct(0, "穿越起飛線");
		public static final MsgStruct FOLLOW_LINE_TURN      = new MsgStruct(0, "循線至第一個轉彎點");
		public static final MsgStruct FOLLOW_LINE_TURNE     = new MsgStruct(0, "轉第一個彎");
		public static final MsgStruct FOLLOW_LINE_THROW     = new MsgStruct(0, "循線至拋射點");
		public static final MsgStruct FOLLOW_LINE_THROWE    = new MsgStruct(0, "穿越拋射點");
		public static final MsgStruct FOLLOW_LINE_TURN2     = new MsgStruct(0, "循線至第二個轉彎點");
		public static final MsgStruct FOLLOW_LINE_TURN2E    = new MsgStruct(0, "轉第二個彎");
		public static final MsgStruct FOLLOW_LINE_OBS       = new MsgStruct(0, "循線至障礙物");
		public static final MsgStruct FOLLOW_LINE_OBSE      = new MsgStruct(0, "穿越障礙物");
		public static final MsgStruct FOLLOW_LINE_STOP      = new MsgStruct(0, "循線至停止線");
		public static final MsgStruct FOLLOW_LINE_STOPE     = new MsgStruct(0, "穿越停止線");
		
		public static final MsgStruct PRELAND               = new MsgStruct(0, "準備降落");
		public static final MsgStruct LANDING               = new MsgStruct(0, "降落中");
		public static final MsgStruct LANDED                = new MsgStruct(0, "降落完成");
		
		
		public static final MsgStruct DEBUG_MODE            = new MsgStruct(0, "偵錯模式");
		
		public static final MsgStruct CAN_NOT_FLY           = new MsgStruct(2, "無法起飛");
		
		
	}
	
	
	private void stl() {
		switch (info.step) {
		case 0:
			info.step = 1;
			info.armMode = ControlMode.STOP;
			info.baroMode = ControlMode.STOP;
			info.yawMode = ControlMode.STOP;
			info.rollMode = ControlMode.STOP;
			info.pitchMode = ControlMode.STOP;
			MainStart.info.msgStruct = MsgIndex.STOP;
			info.takeOffHeading = info.att[2];
			info.wantHeading = 0;

//			info.rollMode = ControlMode.WORK;
//			info.yawMode = ControlMode.WORK;
//			info.pitchMode = ControlMode.WORK;
			
			break;
		case 1:
			if (btn) {
				info.step = 2;
				info.msgStruct = MsgIndex.RUN;
				info.wantHeading = 0;
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
			if (info.altEstAlt > 10) {
				info.yawMode = ControlMode.WORK;
				info.rollMode = ControlMode.WORK;
				info.pitchMode = ControlMode.WORK;
			}
			break;
		case 1012:
			if (timerOn(100)) {
				info.step = 13;
				info.msgStruct = MsgIndex.SET_BARO;
			}
			break;
		case 13: // 設定高度200cm
			info.baroMode = ControlMode.WORK;
			if (info.baro_mode) {
				isTimeing = false;
				isThrow = false;
				
				info.setWantAlt = 80;
				
//				info.step = 15;
				info.step = 20;
			}
			break;
		case 15:
			info.msgStruct = MsgIndex.DEBUG_MODE;
			if (btn) {
				info.step = 100;
			}
			break;
			
			
		case 20:
			createTimer();
			info.step = 21;
			break;
		case 21:
			info.msgStruct = MsgIndex.FOLLOW_LINE;
			if (info.captureStatus == 3) {
				statusCount++;
				if (statusCount > 5) {
//					info.step = 90;
				}
			} else {
				statusCount = 0;
			}
//			switch (info.captureStatus) {
//			case 2: // 左轉
//				info.step = 25;
//				break;
//			case 3: // 避障
//				statusCount++;
//				info.step = 90;
//				break;
//			case 4: // 降落
//				info.step = 90;
//				break;
//			default:
//				break;
//			}
			if (!isThrow) {
				if (timerOn(5000)) { // 拋射
					isThrow = true;
					loc.setTurn(true);
				}
			}
			break;
			
			
//		case 25: // 左轉
//			isTurned = true;
//			info.wantHeading += 90;
//			info.msgStruct = MsgIndex.TURN_LEFT;
//			info.pitchMode = ControlMode.STOP;
//			info.step = 26;
//			break;
//		case 26:
//			if (info.captureStatus == 1 && Math.abs(yawModeOffset) < 10) {
//				info.pitchMode = ControlMode.WORK;
//				info.step = 20;
//			}
//			break;
			
			
		case 30:
			if (info.captureStatus == 1) {
				info.step = 20;
			}
			break;
			
			
		case 90: // 降落
			info.msgStruct = MsgIndex.PRELAND;
			createTimer();
			info.step = 91;
			break;
		case 91:
			if (timerOn(1000)) {
				throttleValue = throttleHoldValue - 30;
				info.baroMode = ControlMode.STOP;
				info.pitchMode = ControlMode.STOP;
				info.step = 100;
			}
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
	private void mode() {
		
		switch (info.armMode) {
		case ControlMode.RELEASE:
			setRc.setAux1(0);
			break;
		case ControlMode.STOP:
			setRc.setAux1(1098);
			break;
		case ControlMode.WORK:
			setRc.setAux1(1898);
			break;
		default:
			setRc.setAux1(0);
			break;
		}
		
		switch (info.baroMode) {
		case ControlMode.RELEASE:
			setRc.setAux3(0);
			break;
		case ControlMode.STOP:
			setRc.setAux3(1098);
			break;
		case ControlMode.WORK:
			setRc.setAux3(1898);
			break;
		default:
			setRc.setAux3(0);
			break;
		}
		
		
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
			if (info.captureStatus != 0){
				info.wantHeading = (short) (info.captureAngle + info.att[2]);
			}
			
			
			yawModeOffset = info.wantHeading - info.att[2];
//			yawModeOffset = info.captureStatus != 0 ? info.captureAngle : 0;
			if (yawModeOffset <= -180) {
				yawModeOffset += 360;
			}
			if (yawModeOffset > 180) {
				yawModeOffset -= 360;
			}
			int offset = yawModeOffset * 10;
			
			if (offset > 400) {
				offset = 400;
			}
			if (offset < -400) {
				offset = -400;
			}
			setRc.setYaw(1500 + offset);
			break;
		default:
			setRc.setYaw(0);
			break;
		}
		
		
		
		rollPID.setMode(info.rollMode == ControlMode.WORK ? PID.AUTOMATIC : PID.MANUAL);
		switch (info.rollMode) {
		case ControlMode.RELEASE:
			setRc.setRoll(0);
			break;
		case ControlMode.STOP:
			setRc.setRoll(1486);
			break;
		case ControlMode.WORK:
			
			
			setRc.setRoll((short)(1486 + rollPID.setInput(getMm(info.captureDeltaX)).compute().getOutput()));
			
			break;
		default:
			setRc.setRoll(0);
			break;
		}
		
		
		
		
//		switch (info.rollMode) {
//		case ControlMode.RELEASE:
//			setRc.setRoll(0);
//			break;
//		case ControlMode.STOP:
//			setRc.setRoll(1486);
//			break;
//		case ControlMode.WORK:
//			long time = getTime();
//			double vShouldBe = MathCal.getShouldBe(info.captureDeltaX);
//			double vCurrent = (double)(getMm(info.captureDeltaX) - rollModePreX) / (time - rollModeLastTime);
//			rollModePreX = getMm(info.captureDeltaX);
//			rollModeLastTime = time;
//			int offset = (int) ((vShouldBe - vCurrent) * 150);
//			
//			info.rpiDebug[4] = (int) (vShouldBe * 1000000);
//			info.rpiDebug[5] = (int) (vCurrent * 1000000);
//			info.rpiDebug[6] = offset;
//			
//			
//
//			if (offset > 60) {
//				offset = 60;
//			}
//			if (offset < -60) {
//				offset = -60;
//			}
//			
//			setRc.setRoll(1481 + offset);
//			
//			break;
//		default:
//			setRc.setRoll(0);
//			break;
//		}
		
		switch (info.pitchMode) {
		case ControlMode.RELEASE:
			setRc.setPitch(0);
			break;
		case ControlMode.STOP:
			setRc.setPitch(1500);
			break;
		case ControlMode.WORK:
			setRc.setPitch(1505 + MainStart.extraInfo[0]);
			break;
		default:
			setRc.setPitch(0);
			break;
		}
		
	}

	private long timerTime;
	private void createTimer() {
		timerTime = getTime();
	}
	private boolean timerOn(long millis) {
		return getTime() - timerTime >= millis;
	}
	
	static long getTime() {
		return new Date().getTime();
	}
	
	
	public MCU setup() throws UnsupportedBoardType, IOException, InterruptedException, UnsupportedBusNumberException {
		ca = new CaptureAdapter().setup();
		mwc = new MwcSerialAdapter().open();
		loc = new LedAndOtherController().init();
		MathCal.preShouldBe();
		printTime1 = getTime();
		rollPID = new PID(0.08, 0.0, 0.03);
		return this;
	}
	
	static long printTime1;
	public static void printTime() {
		long printTime2 = getTime();
		System.out.printf("%5d , ", printTime2 - printTime1);
		printTime1 = printTime2;
	}
	public static void printEnter() {
		System.out.println();
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
					loc.updateLed();
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
			info.rollMode = ControlMode.RELEASE;
			info.pitchMode = ControlMode.RELEASE;
			
			loc.setTurn(false);
		}
		
		info.rpiDebug[0] = setRc.getRoll();
		info.rpiDebug[1] = setRc.getPitch();
		info.rpiDebug[2] = setRc.getYaw();
		
		info.rpiDebug[3] = info.extraRc[2];
		
		
//		rollPID.setMode(info.extraRc[2] > 1500 ? PID.AUTOMATIC : PID.MANUAL);
		
//		if (info.extraRc[2] < 1500) {
//			setRc.setRoll(0);
//		}
//		setRc.setYaw(0);
//		setRc.setPitch(0);
		
		try {
			switch(info.captureStatus) {
			case 0:
				loc.setLed(0, Color.RED);
				break;
			case 1:
				loc.setLed(0, Color.GREEN);
				break;
			case 3:
				loc.setLed(0, Color.BLUE);
				break;
			default:
				loc.setLed(0, Color.WHITE);
				break;
			}
			loc.updateLed();
		} catch (IOException e) {
//			e.printStackTrace();
		}
		
		
		
		
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
//		try {
//			loc.updateLed();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
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
			loc.setAllLed(Color.BLACK).updateLed().close();
		} catch (IOException e) {
			print("[Close]: [loc]:");
			e.printStackTrace();
		}
		
		
	}
	
	
	public static double getMm(short pixel) {
		return pixel * (info.altEstAlt + 5.5) / 54.16;
	}
	
	
}
