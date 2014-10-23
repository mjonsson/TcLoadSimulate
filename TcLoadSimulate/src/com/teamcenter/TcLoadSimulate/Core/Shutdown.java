package com.teamcenter.TcLoadSimulate.Core;

import java.util.ArrayList;

/**
 * A thread instance of this class is created if user presses ctrl^c during
 * program execution in a console window or if the application is closed with
 * the close button from the GUI. The class is responsible for shutting down all
 * application threads in a graceful manner.
 * 
 */
public final class Shutdown implements Runnable {
	private static ArrayList<Thread> workerThreads = new ArrayList<Thread>();

	public final static void registerThread(Thread thread) {
		workerThreads.add(thread);
	}
	
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
			System.err.println("\nWaiting for workers to finish exit sequence...\n");

			for (Thread t : workerThreads) {
				if (t.isAlive()) {
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