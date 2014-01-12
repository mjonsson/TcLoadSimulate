package com.teamcenter.TcLoadSimulate.Modules;

import com.teamcenter.TcLoadSimulate.Core.Setting;
import com.teamcenter.soa.client.Connection;

/**
 * A module that executes a saved query.
 * 
 */
public final class SavedQuery extends QueryModule {
	public SavedQuery() {
		super();
	}

	public SavedQuery(SavedQuery copy) {
		super(copy);
	}

	public SavedQuery(String id, String type, Setting[] settingsList) {
		super(id, type, settingsList);
	}

	/**
	 * Inherited method which purpose is to run the module.
	 * 
	 *  (non-Javadoc)
	 * @see com.teamcenter.TcLoadSimulate.Core.Module#run(com.teamcenter.soa.client.Connection, com.teamcenter.TcLoadSimulate.Core.Events.ProgressEvent)
	 */
	public final void run(Connection connection)
			throws Exception {
		super.start();

		this.connection = connection;
		initialize();
		execute();

		super.end();
	}
}
