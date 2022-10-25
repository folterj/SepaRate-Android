package info.joostdefolter.separate;

import android.os.Handler;

public class UITimer {
	public Handler uiHandler = new Handler();
	private Runnable runMethod;
	private int intervalMs;
	private boolean enabled = false;
	private boolean repeating = false;

	public UITimer(Runnable runMethod, int intervalMs) {
		this.runMethod = runMethod;
		this.intervalMs = intervalMs;
	}

	public UITimer(Runnable runMethod, int intervalMs, boolean repeating) {
		this(runMethod, intervalMs);
		this.repeating = repeating;
	}

	public void start() {
		if (enabled)
			return;

		if (intervalMs < 1)
			return;

		enabled = true;
		uiHandler.postDelayed(timer_tick, intervalMs);
	}

	public void stop() {
		if (!enabled)
			return;

		enabled = false;
		uiHandler.removeCallbacks(runMethod);
		uiHandler.removeCallbacks(timer_tick);
	}

	public void reset() {
		stop();
		start();
	}

	public void setInterval(int intervalMs) {
		this.intervalMs = intervalMs;
	}

	public boolean isEnabled() {
		return enabled;
	}

	private Runnable timer_tick = new Runnable() {
		public void run() {
			if (!enabled)
				return;

			uiHandler.post(runMethod);

			if (!repeating) {
				enabled = false;
				return;
			}

			uiHandler.postDelayed(timer_tick, intervalMs);
		}
	};
}
