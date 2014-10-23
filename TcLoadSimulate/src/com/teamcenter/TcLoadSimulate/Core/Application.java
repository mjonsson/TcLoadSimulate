package com.teamcenter.TcLoadSimulate.Core;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.*;

import com.teamcenter.TcLoadSimulate.TcLoadSimulate;

/**
 * The main application class responsible for setting up global constants and
 * supplying static helper methods for getting settings within the application.
 * 
 */
@XmlRootElement(name = "application")
@XmlAccessorType(XmlAccessType.NONE)
public class Application {
	/**
	 * A list of all global settings defined in the xml-configuration.
	 */
	@XmlElementWrapper(name = "globals")
	@XmlElement(name = "setting")
	private Setting[] globalsList;
	/**
	 * Defined modules derived from the xml-configuration.
	 */
	@XmlElementWrapper(name = "modules")
	@XmlElement(name = "module")
	private Module[] moduleList;
	/**
	 * Defined workers derived from the xml-configuration.
	 */
	@XmlElementWrapper(name = "workers")
	@XmlElement(name = "worker")
	private Worker[] workerList;

	/**
	 * A static list of global settings. It is created from the local
	 * globalsList member.
	 */
	public static Setting[] staticGlobalsList = null;
	/**
	 * A static list of module definitions. It is created from the local
	 * moduleList member.
	 */
	private static Module[] staticModuleList;

	public Application() {
	}

	/**
	 * Convenience method that returns the global setting for a specific input
	 * parameter.
	 * 
	 * @param name
	 *            Name of global setting to retrieve.
	 * @return The value of the setting as a string.
	 */
	public static final String getGlobal(String name) {
		if (staticGlobalsList != null)
			for (Setting s : staticGlobalsList)
				if (name.equals(s.name))
					return s.value;

		return null;
	}

	/**
	 * Convenience method that returns a module object by id.
	 * 
	 * @param id
	 *            The id of module to return.
	 * @return The module object.
	 */
	public static final Module getModule(String id) {
		for (Module mod : staticModuleList)
			if (id.equals(mod.id))
				return mod;

		return null;
	}

	/**
	 * Helper method for starting a list of workers
	 * 
	 * @param workers
	 *            List of workers
	 */
	public static final Thread startWorkers(final List<Worker> workers) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					for (Worker w : workers) {
						if (w.mode == Mode.STOPPED) {
							w.start();
							Thread.sleep(w.getSettingAsLong("start_delay") * 1000);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.setName("Start");
		t.setDaemon(true);
		t.start();
		
		return t;
	}

	/**
	 * Helper method for stopping a list of workers
	 * 
	 * @param workers
	 *            List of workers
	 */
	public static final void stopWorkers(final List<Worker> workers) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					for (Worker w : workers) {
						if (w.mode == Mode.STARTED) {
							w.stop();
							Thread.sleep(w.getSettingAsLong("stop_delay") * 1000);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.setName("Stop");
		t.setDaemon(true);
		t.start();
	}

	/**
	 * The application initialization method. Sets up applicationwide, static
	 * settings- and modules lists. Triggers the startup sequence class object.
	 * 
	 * @throws Exception
	 */
	public final void init() throws Exception {
		try {
			if (globalsList != null) {
				staticGlobalsList = globalsList.clone();
				globalsList = null;
			}
			if (moduleList != null) {
				staticModuleList = moduleList.clone();
				moduleList = null;
			}

			if (TcLoadSimulate.gui) {
				if (workerList != null) {
					for (final Worker w : workerList) {
						w.initialize();
					}
				}
				UserInterface.loop();
			} else {
				if (workerList != null) {
					for (Worker w : workerList) {
						w.initialize();
					}
					Thread t = startWorkers(Arrays.asList(workerList));
					t.join();
					for (Worker w : workerList) {
						w.myThread.join();
					}
				}
			}
		} catch (Exception e) {
			if (TcLoadSimulate.gui) {
				UserInterface.DisplayError("Initialization error", e);
				UserInterface.loop();
			}
			else
			{
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
}