package ian.main.mcu;

import java.util.Date;

public class PID {
	public final static int AUTOMATIC = 1;
	public final static int MANUAL = 0;

	public final static int DIRECT = 0;
	public final static int REVERSE = 1;
	
	public final static int P_ON_M = 0;
	public final static int P_ON_E = 1;

	double dispKp; // * we'll hold on to the tuning parameters in user-entered
	double dispKi; // format for display purposes
	double dispKd; //

	double kp; // * (P)roportional Tuning Parameter
	double ki; // * (I)ntegral Tuning Parameter
	double kd; // * (D)erivative Tuning Parameter

	int controllerDirection;
	int pOn;

	double myInput; // * Pointers to the Input, Output, and Setpoint variables
	double myOutput; // This creates a hard link between the variables and the
	double mySetpoint; // PID, freeing the user from having to constantly tell
						// us
						// what these values are. with pointers we'll just know.

	long lastTime;
	double outputSum, lastInput;

	long sampleTime;
	double outMin, outMax;
	boolean inAuto, pOnE;

	private long millis() {
		return new Date().getTime();
	}

	public PID(double Kp, double Ki, double Kd) {
		// myOutput = Output;
		// myInput = Input;
		// mySetpoint = Setpoint;
		inAuto = false;

		setOutputLimits(-100, 100); // default output limit corresponds to
									// the arduino pwm limits

		sampleTime = 250; // default Controller Sample Time is 0.1 seconds

		setControllerDirection(DIRECT);
		setTunings(Kp, Ki, Kd, P_ON_E);

		lastTime = millis() - sampleTime;
	}

	public PID setInput(double input) {
		myInput = input;
		return this;
	}

	public PID setSetPoint(double setPoint) {
		mySetpoint = setPoint;
		return this;
	}

	public double getOutput() {
		return myOutput;
	}
	
	

	public PID compute() {
		if (!inAuto)
			return this;
		long now = millis();
		long timeChange = (now - lastTime);
		if (timeChange >= sampleTime) {
			/* Compute all the working error variables */
			double input = myInput;
			double error = mySetpoint - input;
			double dInput = (input - lastInput);
			outputSum += (ki * error);

			/* Add Proportional on Measurement, if P_ON_M is specified */
			if (!pOnE)
				outputSum -= kp * dInput;

			if (outputSum > outMax)
				outputSum = outMax;
			else if (outputSum < outMin)
				outputSum = outMin;

			/* Add Proportional on Error, if P_ON_E is specified */
			double output;
			if (pOnE)
				output = kp * error;
			else
				output = 0;

			/* Compute Rest of PID Output */
			output += outputSum - kd * dInput;

			if (output > outMax)
				output = outMax;
			else if (output < outMin)
				output = outMin;
			myOutput = output;

			/* Remember some variables for next time */
			lastInput = input;
			lastTime = now;
		}
		return this;
	}

	void setTunings(double Kp, double Ki, double Kd, int POn) {
		if (Kp < 0 || Ki < 0 || Kd < 0)
			return;

		pOn = POn;
		pOnE = POn == P_ON_E;

		dispKp = Kp;
		dispKi = Ki;
		dispKd = Kd;

		double SampleTimeInSec = ((double) sampleTime) / 1000;
		kp = Kp;
		ki = Ki * SampleTimeInSec;
		kd = Kd / SampleTimeInSec;

		if (controllerDirection == REVERSE) {
			kp = (0 - kp);
			ki = (0 - ki);
			kd = (0 - kd);
		}
	}

	public void setTunings(double Kp, double Ki, double Kd) {
		setTunings(Kp, Ki, Kd, pOn);
	}

	void setOutputLimits(double Min, double Max) {
		if (Min >= Max)
			return;
		outMin = Min;
		outMax = Max;

	}

	void setMode(int Mode) {
		boolean newAuto = (Mode == AUTOMATIC);
		if (newAuto && !inAuto) { /* we just went from manual to auto */
			initialize();
		}
		inAuto = newAuto;
	}

	void initialize() {
		outputSum = 0;
		lastInput = myInput;
		if (outputSum > outMax)
			outputSum = outMax;
		else if (outputSum < outMin)
			outputSum = outMin;
	}

	void setControllerDirection(int Direction) {
		if (inAuto && Direction != controllerDirection) {
			kp = (0 - kp);
			ki = (0 - ki);
			kd = (0 - kd);
		}
		controllerDirection = Direction;
	}

	public double getKp() {
		return dispKp;
	}

	public double getKi() {
		return dispKi;
	}

	public double getKd() {
		return dispKd;
	}

	public int getMode() {
		return inAuto ? AUTOMATIC : MANUAL;
	}
}
