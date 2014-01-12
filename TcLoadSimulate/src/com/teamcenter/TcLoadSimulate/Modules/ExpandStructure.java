package com.teamcenter.TcLoadSimulate.Modules;

import com.teamcenter.TcLoadSimulate.Core.Module;
import com.teamcenter.TcLoadSimulate.Core.Setting;
import com.teamcenter.soa.common.ObjectPropertyPolicy;
import com.teamcenter.soa.common.PolicyType;
import com.teamcenter.services.strong.cad.StructureManagementService;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsInfo;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.CreateBOMWindowsResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.GetRevisionRulesResponse;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.RevisionRuleConfigInfo;
import com.teamcenter.services.strong.cad._2007_01.StructureManagement.RevisionRuleInfo;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSAllLevelsInfo;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSAllLevelsOutput;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSAllLevelsPref;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSAllLevelsResponse2;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSOneLevelOutput;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.strong.cad._2008_06.StructureManagement.ExpandPSOneLevelResponse2;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.services.strong.core._2008_06.DataManagement.AttrInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.DatasetFilter;
import com.teamcenter.services.strong.core._2008_06.DataManagement.DatasetInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.GetItemAndRelatedObjectsInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.GetItemAndRelatedObjectsResponse;
import com.teamcenter.services.strong.core._2008_06.DataManagement.ItemInfo;
import com.teamcenter.services.strong.core._2008_06.DataManagement.RevInfo;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMView;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.RevisionRule;
import com.teamcenter.soa.client.model.ModelObject;

/**
 * A module that expands a structure all levels.
 * 
 */
public final class ExpandStructure extends Module {
	/**
	 * The Teamcenter item.
	 */
	private Item item = null;
	/**
	 * The Teamcenter bomview object related to the item.
	 */
	private BOMView bomView = null;
	/**
	 * The Teamcenter item revision.
	 */
	private ItemRevision itemRevision = null;
	/**
	 * The revision rule to use for configuring the structure.
	 */
	private RevisionRule revisionRule = null;
	private String strItemRevision;
	private String strRevRule;
	private String strViewType;

	public ExpandStructure() {
		super();
	}

	public ExpandStructure(ExpandStructure copy) {
		super(copy);
	}

	public ExpandStructure(String id, String type, Setting[] settingsList) {
		super(id, type, settingsList);
	}

	/**
	 * Method that performs all the required pre-requisities before expanding
	 * the structure.
	 * 
	 * @throws Exception
	 */
	private final void initialize() throws Exception {
		SessionService ss = SessionService.getService(connection);

		ObjectPropertyPolicy opp = new ObjectPropertyPolicy();
		opp.addType(new PolicyType("Item", new String[] { "item_id",
				"bom_view_tags" }));
		opp.addType(new PolicyType("ItemRevision", new String[] {
				"item_revision_id", "object_name", "structure_revisions" }));
		opp.addType(new PolicyType("RevisionRule",
				new String[] { "object_name" }));

		ss.setObjectPropertyPolicy(opp);

		StructureManagementService sm = StructureManagementService
				.getService(connection);

		GetRevisionRulesResponse response1 = sm.getRevisionRules();

		strRevRule = getParsedSetting("revision_rule");
		for (RevisionRuleInfo ruleInfo : response1.output) {
			if (ruleInfo.revRule.get_object_name().equals(strRevRule)) {
				revisionRule = ruleInfo.revRule;
				break;
			}
		}

		if (revisionRule == null)
			throw new Exception(String.format(
					"Cannot find revision rule \"%s\".", strRevRule));

		DataManagementService dm = DataManagementService.getService(connection);

		strItemRevision = getParsedSetting("item_revision");
		GetItemAndRelatedObjectsInfo input = new GetItemAndRelatedObjectsInfo();
		input.itemInfo = new ItemInfo();
		input.itemInfo.ids = new AttrInfo[1];
		input.itemInfo.ids[0] = new AttrInfo();
		input.itemInfo.ids[0].name = "item_id";
		input.itemInfo.ids[0].value = strItemRevision.split("/")[0];
		input.itemInfo.useIdFirst = true;
		input.revInfo = new RevInfo();
		input.revInfo.id = strItemRevision.split("/")[1];
		input.revInfo.useIdFirst = true;
		input.revInfo.processing = "Ids";
		input.datasetInfo = new DatasetInfo();
		input.datasetInfo.filter = new DatasetFilter();
		input.datasetInfo.filter.processing = "None";
		input.bvrTypeNames = new String[0];

		GetItemAndRelatedObjectsResponse response2 = dm
				.getItemAndRelatedObjects(new GetItemAndRelatedObjectsInfo[] { input });

		if (response2.output != null && response2.output.length == 1) {
			item = response2.output[0].item;
			itemRevision = response2.output[0].itemRevOutput[0].itemRevision;
		}

		if (item == null || itemRevision == null)
			throw new Exception(String.format(
					"Cannot find item revision \"%s\".", strItemRevision));

		dm.getProperties(item.get_bom_view_tags(),
				new String[] { "object_type" });
		strViewType = getParsedSetting("view_type");
		for (ModelObject mo : item.get_bom_view_tags()) {
			BOMView bv = (BOMView) mo;

			if (bv.get_object_type().equals(strViewType)) {
				bomView = bv;
				break;
			}
		}

		if (bomView == null) {
			throw new Exception(String.format(
					"Cannot find bom view type \"%s\".", strViewType));
		}
	}

	/**
	 * Method that expands a structure one-level or all-levels.
	 * 
	 * @throws Exception
	 */
	private final void execute() throws Exception {
		StructureManagementService sm = StructureManagementService
				.getService(connection);

		CreateBOMWindowsInfo[] input1 = { new CreateBOMWindowsInfo() };
		input1[0].item = item;
		input1[0].itemRev = itemRevision;
		input1[0].bomView = bomView;
		input1[0].revRuleConfigInfo = new RevisionRuleConfigInfo();
		input1[0].revRuleConfigInfo.revRule = revisionRule;

		strPropertyPolicy = getParsedSetting("property_policy");
		SessionService ss = SessionService.getService(connection);
		ss.setObjectPropertyPolicy(strPropertyPolicy);

		timer.start();
		CreateBOMWindowsResponse response1 = sm.createBOMWindows(input1);

		BOMLine bomLineToExpand = null;
		if (response1.output != null && response1.output.length == 1) {
			bomLineToExpand = response1.output[0].bomLine;
		}

		int nrOfParents = 0;
		long nrOfChildren = 0;
		if (getSettingAsBool("all_levels") == true) {
			ExpandPSAllLevelsInfo input2 = new ExpandPSAllLevelsInfo();
			input2.parentBomLines = new BOMLine[] { bomLineToExpand };
			input2.excludeFilter = "None2";
			ExpandPSAllLevelsPref prefs1 = new ExpandPSAllLevelsPref();
			prefs1.expItemRev = false;

			ExpandPSAllLevelsResponse2 response2 = sm.expandPSAllLevels(input2,
					prefs1);

			if (response2.output != null && response2.output.length > 0) {
				for (ExpandPSAllLevelsOutput psOutput : response2.output) {
					if (psOutput.children.length > 0) {
						nrOfChildren += psOutput.children.length;
						nrOfParents++;
					}
				}
			}
		} else {
			ExpandPSOneLevelInfo input2 = new ExpandPSOneLevelInfo();
			input2.parentBomLines = new BOMLine[] { bomLineToExpand };
			input2.excludeFilter = "None2";
			ExpandPSOneLevelPref prefs1 = new ExpandPSOneLevelPref();
			prefs1.expItemRev = false;

			ExpandPSOneLevelResponse2 response2 = sm.expandPSOneLevel(input2,
					prefs1);

			if (response2.output != null && response2.output.length > 0) {
				for (ExpandPSOneLevelOutput psOutput : response2.output) {
					if (psOutput.children.length > 0) {
						nrOfChildren += psOutput.children.length;
						nrOfParents++;
					}
				}
			}
		}

		timeDelta = timer.stop();
		miscInfo = String
						.format("Item Revision: %s, Bom View: %s, Revision Rule: %s, Property Policy: %s, Returned parents: %d, Returned children: %d",
								strItemRevision, strViewType, strRevRule,
								strPropertyPolicy, nrOfParents, nrOfChildren);
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