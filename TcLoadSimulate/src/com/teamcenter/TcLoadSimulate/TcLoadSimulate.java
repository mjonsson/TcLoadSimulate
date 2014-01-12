package com.teamcenter.TcLoadSimulate;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.teamcenter.TcLoadSimulate.Core.Application;
import com.teamcenter.TcLoadSimulate.Core.Shutdown;
import com.teamcenter.TcLoadSimulate.Core.UserInterface;
import com.teamcenter.TcLoadSimulate.Core.Events.Logger;

/**
 * Main application startup class.
 * 
 */
public final class TcLoadSimulate {
	public static boolean debug = false;
	public static boolean gui = true;
	public static UserInterface userInterface;
	public static Logger logger;
	private static Application app = null;
	public static File appPath = null;
	public static File configurationFile = null;
	public static File outputFile = null;
	
	/**
	 * Method for instantiating and starting the application based from the
	 * information from the xml-configuration file.
	 */
	public static final void start() {
		try {
			appPath = new File(TcLoadSimulate.class.getProtectionDomain()
					.getCodeSource().getLocation().getPath());
			JAXBContext context = JAXBContext.newInstance(Application.class);
			Unmarshaller um = context.createUnmarshaller();

			if (configurationFile == null)
				configurationFile = new File(appPath.getPath() + "\\TcLoadSimulate.xml");
			
			if (configurationFile.exists()) {
				app = (Application) um.unmarshal(new FileInputStream(configurationFile));
			}
			
			if (app != null)
				app.init();
		} catch (Exception e) {
			UserInterface.DisplayError("Initialization error", e);
			UserInterface.loop();
		}
	}

	/**
	 * Application entry method that opens the xml-configuration file and parses
	 * it into class objects.
	 * 
	 * @param args
	 *            Args passed from command line.
	 */
	public static final void main(String[] args) {

		try {
			if (System.getProperty("debug") != null)
				debug = true;
			if (System.getProperty("nogui") != null)
				gui = false;

			// Register shutdown hook to capture kill events
			Shutdown shutdownHook = new Shutdown();
			Runtime.getRuntime().addShutdownHook(shutdownHook.init());

			userInterface = new UserInterface();
			logger = new Logger();
			if (gui)
				UserInterface.init();
			
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
