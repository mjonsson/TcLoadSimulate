package com.teamcenter.TcLoadSimulate.Modules;

import com.teamcenter.TcLoadSimulate.Core.Module;
import com.teamcenter.TcLoadSimulate.Core.Setting;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.soa.client.Connection;

/**
 * A module that logs out of Teamcenter.
 * 
 */
public final class Logout extends Module {
	public Logout() {
		super();
	}

	public Logout(Logout copy) {
		super(copy);
	}

	public Logout(String id, String type, Setting[] settingsList) {
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

		SessionService ss = SessionService.getService(connection);

		timer.start();
		ss.logout();
		timeDelta = timer.stop();
		connection = null;

		super.end();
	}
}
