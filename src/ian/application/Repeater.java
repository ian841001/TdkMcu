package ian.application;

import java.util.Timer;
import java.util.TimerTask;

import javax.xml.ws.WebServiceException;

import com.sun.xml.internal.ws.Closeable;

public class Repeater implements Closeable {
	
	public static final int PERIOD = 100;
	
	private Timer t;
	private TimerTask tt;
	
	private Runnable runnable;
	private boolean isClosed;
	
	public Repeater(Runnable runnable) {
		this.runnable = runnable;
	}
	
	public void start() {
		if (tt != null || t != null) {
			return;
		}
		tt = new TimerTask() {
			@Override
			public void run() {
				if (!isClosed) {
					runnable.run();
				}
			}
		};
		t = new Timer();
		t.schedule(tt, 0, PERIOD);
		isClosed = false;
	}
	
	public void stop() {
		isClosed = true;
		if (tt != null) {
			tt.cancel();
			tt = null;
		}
		if (t != null) {
			t.cancel();
			t = null;
		}
	}

	@Override
	public void close() throws WebServiceException {
		stop();
	}
}
