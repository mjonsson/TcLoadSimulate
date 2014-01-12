package com.teamcenter.TcLoadSimulate.Core;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

import com.teamcenter.TcLoadSimulate.TcLoadSimulate;
import com.teamcenter.TcLoadSimulate.Core.Events.Console;
import com.teamcenter.TcLoadSimulate.Modules.Login;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.soa.client.Connection;

/**
 * Class that contains functionality for running all defined module occurrences
 * in a sequence.
 * 
 * @date Changed so that ProgressEvent is instantiated with the Worker object
 */
@XmlType
public final class Worker extends ApplicationObject implements Runnable {
	public Mode mode = Mode.STOPPED;
	public Status status = Status.NONE;
	public int tasks = 0;
	public int iterations = 0;
	public int totalIterations;
	public int totalTasks;
	private Module module;

	@XmlTransient
	public Thread myThread = null;

	/**
	 * Sequence of module occurrences that are to be iterated.
	 */
	@XmlElementWrapper(name = "sequence")
	@XmlElement(name = "mod_occ")
	public ModuleOcc[] sequenceList;

	/**
	 * Each worker have its own tableitem object for displaying information in
	 * the GUI table.
	 */
	private TableItem tableItem;

	/**
	 * The Teamcenter connection object.
	 */
	private Connection connection;

	public TableItem getTableItem() {
		return tableItem;
	}

	public String getModuleType() {
		return module.type;
	}

	public String getModuleTimeDelta() {
		return module.getTimeDelta();
	}

	public String getModuleMiscInfo() {
		return module.getMiscInfo();
	}

	public String getIterations() {
		if (totalIterations == 0)
			return String.format("%d", iterations);
		else
			return String.format("%d/%d", iterations, totalIterations);
	}

	public String getPercent() {
		if (totalIterations == 0)
			return "---";
		else
			return String.format("%d %%",
					Math.round((float) tasks / totalTasks * 100));
	}

	/**
	 * Intializes the thread before execution.
	 * 
	 * @return The thread object.
	 * @throws Exception
	 */
	public void start() {
		if (mode == Mode.STOPPED) {
			myThread = new Thread(this, id);
			myThread.setDaemon(false);
			myThread.start();
		}
	}

	public void stop() {
		if (mode == Mode.STARTED) {
			myThread.interrupt();
		}
	}

	/**
	 * Convenience class to merge two arrays, the primary has higher priority
	 * than secondary.
	 * 
	 * @param primary
	 *            Master array
	 * @param secondary
	 *            Secondary array
	 * @return A merged array
	 */
	private Setting[] mergeSettings(Setting[] primary, Setting[] secondary) {
		List<Setting> temp = null;

		if (primary == null && secondary == null)
			return null;
		else if (primary != null && secondary == null)
			return primary;
		else if (primary == null && secondary != null)
			return secondary;
		else {
			temp = new ArrayList<Setting>(Arrays.asList(primary));

			for (Setting sec : secondary) {
				boolean exist = false;
				for (Setting pri : temp) {
					if (pri.name.equals(sec.name)) {
						exist = true;
						break;
					}
				}
				if (!exist)
					temp.add(sec);
			}
		}

		return temp.toArray(new Setting[temp.size()]);
	}

	/**
	 * Creates worker specific instances of all modules with reflection. This is
	 * needed to make every worker fully thread-safe.
	 * 
	 * @throws Exception
	 */
	public final void initialize() throws Exception {
		for (ModuleOcc mo : sequenceList) {
			Module mod = Application.getModule(mo.refid);
			// Some reflection to get our modules into new instances of
			// correct class
			Class<?> classObj = Class
					.forName("com.teamcenter.TcLoadSimulate.Modules."
							+ mod.type);
			Constructor<?> construct = classObj.getConstructor(String.class,
					String.class, Setting[].class);
			mo.moduleObj = (Module) construct.newInstance(mod.id, mod.type,
					mergeSettings(mo.settingsList, mod.settingsList));
		}
		final TableItem tableItem = new TableItem(UserInterface.getTable(),
				SWT.NONE);
		tableItem.setData("worker", this);
		this.tableItem = tableItem;
		tableItem.setText(new String[] { null, null, "---", id, "---", "---",
				"---", "---" });
		addEventListener(TcLoadSimulate.userInterface);
		addEventListener(TcLoadSimulate.logger);
	}

	/**
	 * Run the module occurrence sequence for a defined number of iterations.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public final void run() {
		Console.out(id, "Starting thread");

		try {
			mode = Mode.STARTED;
			status = Status.NONE;
			fireEvent();

			tasks = 0;
			totalTasks = 0;
			totalIterations = getSettingAsInt("iterations");
			for (ModuleOcc mo : sequenceList) {
				if (mo.runonce.equals("false")) totalTasks += totalIterations;
				else totalTasks++;
			}

			for (iterations = 1; !Thread.interrupted()
					&& (totalIterations == 0 || iterations <= totalIterations); iterations++) {
				for (ModuleOcc mo : sequenceList) {
					if (mo.runonce.equals("false")
							|| (mo.runonce.equals("start") && iterations == 1)
							|| (mo.runonce.equals("end") && iterations == totalIterations)) {
						tasks++;

						// Get module for current task
						module = mo.moduleObj;

						// Enter running phase
						status = Status.RUNNING;
						fireEvent();

						if (module instanceof Login)
							connection = module.run();
						else {
							if (mo.probability == 100
									|| rnd.nextInt(100) <= mo.probability)
								module.run(connection);
							else
								continue;
						}

						// Enter sleep phase
						status = Status.SLEEPING;
						fireEvent();
						module.sleep();
					}
				}
			}
			iterations = totalIterations;
			mode = Mode.STOPPED;
			status = Status.FINISHED;
			fireEvent();
		} catch (InterruptedException e) {
			mode = Mode.STOPPED;
			status = Status.FINISHED;
			fireEvent();
		} catch (Exception e) {
			mode = Mode.STOPPED;
			status = Status.ERROR;
			fireEvent();
			Console.err(e);
		} finally {
			// Make sure the session is logged out
			try {
				if (connection != null) {
					SessionService ss = SessionService.getService(connection);
					ss.logout();
					connection = null;
				}
			} catch (Exception e) {
			}

			Console.out(id, "Stopping thread");
		}
	}
}
