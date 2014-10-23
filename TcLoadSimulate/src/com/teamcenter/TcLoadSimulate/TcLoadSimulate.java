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
			JAXBContext context = JAXBContext.newInstance(Application.class);
			Unmarshaller um = context.createUnmarshaller();

			if (configurationFile != null && configurationFile.exists()) {
				app = (Application) um.unmarshal(new FileInputStream(configurationFile));
			}
			else if (!gui) {
				throw new Exception("Configuration file does not exist.");
			}
			else
			{
				app = new Application();
			}
			
			app.init();

		} catch (Exception e) {
			if (gui) {
				UserInterface.DisplayError("Initialization error", e);
				UserInterface.loop();
			}
			else
			{
				e.printStackTrace();
			}
		}
	}
	
	public static final String parseArgs(String[] args, String arg)
	{
		
		for (String a : args) {
			String aLc = a.toLowerCase();
			String argLc = arg.toLowerCase();
			if (aLc.equals(argLc))
				return argLc;
			else if (aLc.startsWith(argLc + "="))
				return aLc.split("=")[1];
		}

		return null;
	}

	/**
	 * Application entry method that opens the xml-configuration file and parses
	 * it into class objects.
	 * 
	 * @param args
	 *            Args passed from command line.
	 */
	public static final void main(String[] args) {
		String config;
		String output;
		
		try {
			appPath = new File(TcLoadSimulate.class.getProtectionDomain()
					.getCodeSource().getLocation().getPath());

			if (parseArgs(args, "-debug") != null)
				debug = true;
			if (parseArgs(args, "-nogui") != null)
				gui = false;
			if ((config = parseArgs(args, "-config")) != null)
				configurationFile = new File(config); 
			if ((output = parseArgs(args, "-output")) != null)
				outputFile = new File(output); 
			
			if (!gui && configurationFile == null)
				throw new Exception("No configuration defined.");
			
			// Register shutdown hook to capture kill events
			Shutdown shutdownHook = new Shutdown();
			Runtime.getRuntime().addShutdownHook(shutdownHook.init());

			logger = new Logger();
			if (gui)
			{
				userInterface = new UserInterface();
				UserInterface.init();
			}
			
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
