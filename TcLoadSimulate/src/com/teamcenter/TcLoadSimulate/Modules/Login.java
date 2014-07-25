package com.teamcenter.TcLoadSimulate.Modules;

import java.util.UUID;

import org.apache.commons.httpclient.HttpState;

import com.teamcenter.TcLoadSimulate.Core.Module;
import com.teamcenter.TcLoadSimulate.Core.Setting;
import com.teamcenter.TcLoadSimulate.Core.Soa.ExceptionHandlerImpl;
import com.teamcenter.TcLoadSimulate.Core.Soa.CredentialManagerImpl;
import com.teamcenter.TcLoadSimulate.Core.Soa.PartialErrorListenerImpl;
import com.teamcenter.services.strong.core._2007_01.Session;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.soa.SoaConstants;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.Property;

/**
 * A module that creates a Teamcenter session and logs into Teamcenter.
 * 
 */
public final class Login extends Module {
	/**
	 * Keep track if module has been initialized or not.
	 */
	private boolean _initialized = false;
	/**
	 * The credential manager handles Teamcenter authentication.
	 */
	private CredentialManagerImpl credentialManager = null;
	/**
	 * Each connection to Teamcenter has a discrimator, which identifies the
	 * connection.
	 */
	private String discriminator = null;
	private String tcWebTier = null;
	private String tcServerid = null;
	private String tcSyslog = null;
	private String tcHostname = null;

	public Login() {
		super();
	}

	public Login(Login copy) {
		super(copy);
	}

	public Login(String id, String type, Setting[] settingsList) {
		super(id, type, settingsList);
	}

	/**
	 * Initialization method which main purpose is to setup the http connection
	 * object.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	private final void initialize() throws Exception {
		credentialManager = new CredentialManagerImpl();
		tcWebTier = getSetting("endpoint");

		String protocol = tcWebTier.toLowerCase()
				.startsWith("http") ? SoaConstants.HTTP : SoaConstants.IIOP;

		connection = new Connection(tcWebTier, new HttpState(),
				credentialManager, SoaConstants.REST, protocol, false);

		// Add an ExceptionHandler to the Connection, this will handle any
		// InternalServerException, communication errors, xml marshalling errors
		// .etc
		connection.setExceptionHandler(new ExceptionHandlerImpl());

		// While the above ExceptionHandler is required, all of the following
		// Listeners are optional. Client application can add as many or as few
		// Listeners
		// of each type that they want.

		// Add a Partial Error Listener, this will be notified when ever a
		// a service returns partial errors.
		connection.getModelManager().addPartialErrorListener(
				new PartialErrorListenerImpl());

		// Add a Change Listener, this will be notified when ever a
		// a service returns model objects that have been updated.
		// connection.getModelManager().addChangeListener(new
		// AppXUpdateObjectListener());

		// Add a Delete Listener, this will be notified when ever a
		// a service returns objects that have been deleted.
		// connection.getModelManager().addDeleteListener(new
		// AppXDeletedObjectListener());

		// Add a Request Listener, this will be notified before and after each
		// service request is sent to the server.
		// Connection.addRequestListener( new AppXRequestListener() );

		// If proxyhost has been set in configuration, add it
		if (!getSetting("proxyhost").equals(""))
			connection.setOption("HTTPProxyHost", getSetting("proxyhost"));

		// If bypass flag has been set in configuration, add it
		if (getSettingAsBool("bypass"))
			connection.setOption("bypassFlag", Property.toBooleanString(true));
	}

	/**
	 * Method that logs in to Teamcenter.
	 * 
	 * @throws Exception
	 */
	private final void execute() throws Exception {
		SessionService ss = SessionService.getService(connection);

		if (getSetting("discriminator").toLowerCase().equals("[randomnew]")) {
			discriminator = UUID.randomUUID().toString().replace("-", "");
		} else if (getSetting("discriminator").toLowerCase().equals("[random]")
				&& discriminator == null) {
			discriminator = UUID.randomUUID().toString().replace("-", "");
		} else if (discriminator == null)
			discriminator = getSetting("discriminator");

		timer.start();
		ss.login(getSetting("user"), getSetting("password"),
				getSetting("group"), getSetting("role"), null, discriminator);
		timeDelta = timer.stop();
		ss.setObjectPropertyPolicy("Empty");

		Session.GetTCSessionInfoResponse response = ss.getTCSessionInfo();

		tcServerid = (String) response.extraInfo.get("TcServerID");
		tcHostname = (String) response.extraInfo.get("hostName");
		tcSyslog = (String) response.extraInfo.get("syslogFile");

		miscInfo = String.format(
				"WebTier: %s, Server Id.: %s, Hostname: %s, Syslog: %s", tcWebTier, tcServerid,
				tcHostname, tcSyslog);
	}

	/**
	 * Inherited method which purpose is to run the module.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.teamcenter.TcLoadSimulate.Core.Module#run(com.teamcenter.TcLoadSimulate.Core.Events.ProgressEvent)
	 */
	public final Connection run() throws Exception {
		super.start();

		if (!_initialized)
			initialize();
		execute();

		super.end();

		return connection;
	}
}
