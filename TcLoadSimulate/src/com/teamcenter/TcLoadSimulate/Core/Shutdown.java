package com.teamcenter.TcLoadSimulate.Core;

/**
 * A thread instance of this class is created if user presses ctrl^c during
 * program execution in a console window or if the application is closed with
 * the close button from the GUI. The class is responsible for shutting down all
 * application threads in a graceful manner.
 * 
 */
public final class Shutdown implements Runnable {
	/**
	 * Initializes the thread before execution.
	 * 
	 * @return The initialized thread.
	 */
	public final Thread init() {
		Thread thread = new Thread(this, "Shutdown");

		return thread;
	}

	/**
	 * Executes the shutdown thread.
	 * 
	 * @return The initialized thread.
	 */
	public final Thread start() {
		Thread thread = init();
		thread.start();

		return thread;
	}

	/**
	 * Shutdown loop that stops all running worker threads gracefully.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public final void run() {
		try {
			Thread[] allThreads = new Thread[Thread.activeCount()];
			Thread.enumerate(allThreads);

			System.err.println("\nLooking for active workers...\n");

			for (Thread t : allThreads) {
				if (!t.getName()
						.matches(
								"UserInterface|Shutdown|MultiThreadedHttpConnectionManager cleanup|DestroyJavaVM|Thread-0")) {
					t.interrupt();
					Thread.sleep(Long.parseLong(Application
							.getGlobal("stop_delay")) * 1000);
					t.join();
				}
			}
		} catch (Exception e) {
		}
	}
}