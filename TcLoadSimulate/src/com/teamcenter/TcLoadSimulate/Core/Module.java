package com.teamcenter.TcLoadSimulate.Core;

import javax.xml.bind.annotation.XmlAttribute;

import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.soa.client.Connection;

/**
 * Super class for all modules. All fields and methods common for modules are
 * placed within this class.
 * 
 */
public class Module extends ApplicationObject {
	protected Timer timer = new Timer();
	protected Connection connection = null;
	protected String strPropertyPolicy;
	protected String timeDelta;
	protected String miscInfo;
	private int retryCount = 0;
	private int retries = 0;
	private long retryInterval = 10;
	private long sleepInterval = 30;

	/**
	 * Property representing the module type
	 */
	@XmlAttribute(name = "type")
	protected String type;

	public Module() {
	}

	/**
	 * Copy constructor.
	 * 
	 * @param copy
	 *            Source object.
	 */
	public Module(Module copy) {
		super(copy);
		this.type = copy.type;
	}

	/**
	 * Constructor for creating a Module type object from an id and a
	 * settingslist.
	 * 
	 * @param id
	 *            Global id of object.
	 * @param settingsList
	 *            Local object settings.
	 */
	public Module(String id, String type, Setting[] settingsList) throws Exception {
		super(id, settingsList);
		this.type = type;
		this.retryCount = getSettingAsInt("retry_count");
		this.retryInterval = getSettingAsLong("retry_interval");
		this.sleepInterval = getParsedSettingAsLong("sleep");
	}

	public String getTimeDelta() {
		if (timeDelta == null) return "";
		return timeDelta;
	}

	public String getMiscInfo() {
		if (miscInfo == null) return "";
		return miscInfo;
	}
	
	public boolean continueOnError() {
		if (retryCount == 0) return true;
		else if (retries >= retryCount) return false;
		retries++;
		
		return true;
	}

	public void resetRetryCount() {
		retries = 0;
	}
	
	public void retrySleep() throws Exception {
		Thread.sleep(retryInterval * 1000);
	}

	/**
	 * Common method for running a module. This specific module is used by the
	 * login method.
	 * 
	 * @param progressEvent
	 *            An event object is passed into method for retrieving event
	 *            data after the module has been run.
	 * @return The Teamcenter connection object.
	 * @throws Exception
	 */
	public Connection run() throws Exception {
		return null;
	}

	/**
	 * Common method for running a module.
	 * 
	 * @param connection
	 *            The Teamcenter connection object.
	 * @param progressEvent
	 *            An event object is passed into method for retrieving event
	 *            data after the module has been run.
	 * @throws Exception
	 */
	public void run(Connection connection)
			throws Exception {
	}

	/**
	 * A common method invoked by all modules when they go idle for a period.
	 * 
	 * @throws Exception
	 */
	public final void sleep() throws Exception {
		Thread.sleep(sleepInterval * 1000);
	}

	/**
	 * Common method executed in the start of every module.
	 * 
	 * @param progressEvent
	 *            An event object is passed into method for retrieving event
	 *            data after the module has been run.
	 * @throws Exception
	 */
	protected final void start() throws Exception {
	}

	/**
	 * Common method executed in the end of every module.
	 * 
	 * @throws Exception
	 */
	protected final void end() throws Exception {
		// Reset property policy
		if (connection != null) {
			SessionService ss = SessionService.getService(connection);
			ss.setObjectPropertyPolicy("Empty");
		}
	}
}
