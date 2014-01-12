package com.teamcenter.TcLoadSimulate.Modules;

import java.util.Arrays;

import com.teamcenter.TcLoadSimulate.Core.Setting;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2007_01.DataManagement.WhereReferencedOutput;
import com.teamcenter.services.strong.core._2007_01.DataManagement.WhereReferencedResponse;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;

/**
 * A module that executes a where referenced search.
 * 
 * @author Mattias Jonsson
 * @version 1.0.0 Initial
 * 
 */
public final class WhereReferenced extends QueryModule {
	String[] targetObjects;

	public WhereReferenced() {
		super();
	}

	public WhereReferenced(WhereReferenced copy) {
		super(copy);
	}

	public WhereReferenced(String id, String type, Setting[] settingsList) {
		super(id, type, settingsList);
	}

	protected final void initialize() throws Exception {
		super.initialize();
	}

	/**
	 * Executes the saved query in the superclass and then runs the where
	 * referenced search.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.teamcenter.TcLoadSimulate.Modules.QueryModule#execute()
	 */
	protected final void execute() throws Exception {
		super.execute();

		DataManagementService dm = DataManagementService.getService(connection);

		int levels = getParsedSettingAsInt("reference_levels");
		WorkspaceObject[] wsObjs = Arrays.copyOf(queryObjects,
				queryObjects.length, WorkspaceObject[].class);
		timer.start();
		WhereReferencedResponse response1 = dm.whereReferenced(wsObjs, levels);

		int referencers = 0;
		if (response1.output != null)
			for (WhereReferencedOutput output : response1.output)
				referencers += output.info.length;

		timeDelta = timer.stop();

		miscInfo = String.format(
				"Objects: %d, Referencers: %d, Levels: %d",
				queryObjects.length, referencers, levels);

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
