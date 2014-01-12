package com.teamcenter.TcLoadSimulate.Modules;

import com.teamcenter.TcLoadSimulate.Core.Setting;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2007_01.DataManagement.WhereUsedOutput;
import com.teamcenter.services.strong.core._2007_01.DataManagement.WhereUsedResponse;
import com.teamcenter.soa.client.Connection;

/**
 * A module that executes a where used search.
 * 
 * @author Mattias Jonsson
 * @version 1.0.0 Initial
 * 
 */
public final class WhereUsed extends QueryModule {
	String[] targetObjects;

	public WhereUsed() {
		super();
	}

	public WhereUsed(WhereUsed copy) {
		super(copy);
	}

	public WhereUsed(String id, String type, Setting[] settingsList) {
		super(id, type, settingsList);
	}

	/**
	 * Gets the saved query from the super class.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.teamcenter.TcLoadSimulate.Modules.QueryModule#initialize()
	 */
	protected final void initialize() throws Exception {
		super.initialize();
	}

	/**
	 * Executes the saved query in the super class and then performs a where
	 * used search.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.teamcenter.TcLoadSimulate.Modules.QueryModule#execute()
	 */
	protected final void execute() throws Exception {
		super.execute();

		DataManagementService dm = DataManagementService.getService(connection);

		int levels = getParsedSettingAsInt("parent_levels");
		boolean precise = getSettingAsBool("precise_search");

		timer.start();
		WhereUsedResponse response1 = dm.whereUsed(queryObjects, levels,
				precise, null);

		int referencers = 0;
		if (response1.output != null)
			for (WhereUsedOutput output : response1.output)
				referencers += output.info.length;

		timeDelta = timer.stop();

		miscInfo = String.format(
				"Objects: %d, Parents: %d, Levels: %d", queryObjects.length,
				referencers, levels);

	}

	/**
	 * Inherited method which purpose is to run the module.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.teamcenter.TcLoadSimulate.Core.Module#run(com.teamcenter.soa.client.Connection,
	 *      com.teamcenter.TcLoadSimulate.Core.Events.ProgressEvent)
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
