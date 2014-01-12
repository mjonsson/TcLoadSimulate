package com.teamcenter.TcLoadSimulate.Core;

/**
 * A basic timer class.
 * 
 */
public final class Timer {
	/**
	 * The start time in millseconds.
	 */
	private long _start;

	/**
	 * Start the timer.
	 */
	public final void start() {
		_start = System.nanoTime();
	}

	/**
	 * Stop the timer.
	 * 
	 * @return Delta time value
	 */
	public final String stop() {
		long delta = System.nanoTime() - _start;
		long min = (long) Math.floor(delta / 60000000000L);
		delta = delta % 60000000000L;
		long sec = (long) Math.floor(delta / 1000000000L);
		delta = delta % 1000000000L;
		long msec = (long) Math.floor(delta / 1000000L);
		return String.format("%02d:%02d.%03d", min, sec, msec);
	}
}