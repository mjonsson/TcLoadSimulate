package com.teamcenter.TcLoadSimulate.Core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.teamcenter.TcLoadSimulate.Core.Events.Event;
import com.teamcenter.TcLoadSimulate.Core.Events.EventListener;

/**
 * Top class for all defined objects in the xml-configuration that have an id
 * and a list of settings.
 * 
 */
public abstract class ApplicationObject {
	/**
	 * Reg ex. for finding the RandNr construct within the xml-configuration
	 * file.
	 */
	private static final String regexNr = "\\[\\s*RandNr\\s*\\(\\s*(\\d+\\s*,\\s*\\d+)\\s*\\)\\s*\\]";
	/**
	 * Reg ex. for finding the RandOpt construct within the xml-configuration
	 * file.
	 */
	private static final String regexOpt = "\\[\\s*RandOpt\\s*\\(\\s*(.+)\\s*\\)\\s*\\]";
	/**
	 * The compiled pattern for RandNr.
	 */
	private static final Pattern pNr = Pattern.compile(regexNr);
	/**
	 * The compiled pattern for RandOpt.
	 */
	private static final Pattern pOpt = Pattern.compile(regexOpt);
	/**
	 * Initializes a new Random object with a seed.
	 */
	protected final Random rnd = new Random(System.currentTimeMillis());
	/**
	 * Sets the format of the dateformat object.
	 */
	private final DateFormat dateFormat = new SimpleDateFormat(
			"yy/MM/dd HH:mm:ss");

	/**
	 * List of objects listening to events from this object
	 */
	protected List<EventListener> eventListeners = new ArrayList<EventListener>();

	/**
	 * Property representing global unique id for all objects
	 */
	@XmlAttribute(name = "id")
	public String id;
	/**
	 * A list of object level settings.
	 */
	@XmlElementWrapper(name = "settings")
	@XmlElement(name = "setting")
	protected Setting[] settingsList;

	public ApplicationObject() {
	}

	/**
	 * Copy constructor.
	 * 
	 * @param copy
	 *            Source object.
	 */
	public ApplicationObject(ApplicationObject copy) {
		this.id = copy.id;
		if (copy.settingsList != null)
			this.settingsList = copy.settingsList.clone();
	}

	/**
	 * Constructor based on object id and a settings list.
	 * 
	 * @param id
	 *            Global id. of the application object.
	 * @param settingsList
	 *            Object level settings list.
	 */
	public ApplicationObject(String id, Setting[] settingsList) {
		this.id = id;
		if (settingsList != null)
			this.settingsList = settingsList.clone();
	}

	public synchronized void addEventListener(EventListener listener) {
		eventListeners.add(listener);
	}

	public synchronized void removeEventListener(EventListener listener) {
		eventListeners.remove(listener);
	}

	protected synchronized void fireEvent() {
		Event event = new Event(this);
		for (EventListener l : eventListeners)
			l.handleWorkerEvent(event);
	}

	/**
	 * Convenience method that returns the local object setting for a specific
	 * input parameter. If no local setting exists, it fallbacks to the global
	 * settings.
	 * 
	 * @param name
	 *            Name of setting to retrieve.
	 * @return The value of the setting as a string.
	 * @throws Exception
	 */
	protected final String getSetting(String name) throws Exception {
		if (settingsList != null)
			for (Setting s : settingsList)
				if (name.equals(s.name))
					return s.value;
		if (Application.staticGlobalsList != null)
			for (Setting s : Application.staticGlobalsList)
				if (name.equals(s.name))
					return s.value;

		throw new Exception(String.format("Can not find setting \"%s\"", name));
	}

	/**
	 * Convenience method for getting a parsed setting.
	 * 
	 * @param name
	 *            Name of setting
	 * @return The parsed string
	 * @throws Exception
	 */
	protected final String getParsedSetting(String name) throws Exception {
		return parseValue(getSetting(name));
	}

	/**
	 * Convenience method for getting a setting as a boolean type.
	 * 
	 * @param value
	 *            Name of setting.
	 * @return A boolean representation.
	 * @throws Exception
	 */
	protected final boolean getSettingAsBool(String name) throws Exception {
		if (getSetting(name).toLowerCase().matches("true|1|y")) {
			return true;
		}

		return false;
	}

	/**
	 * Convenience method for getting a setting as a long type.
	 * 
	 * @param value
	 *            Name of setting.
	 * @return A long representation.
	 * @throws Exception
	 */
	protected final long getSettingAsLong(String name) throws Exception {
		return Long.parseLong(getSetting(name));
	}

	/**
	 * Convenience method for getting a setting as a integer type.
	 * 
	 * @param value
	 *            Name of setting.
	 * @return A integer representation.
	 * @throws Exception
	 */
	protected final int getSettingAsInt(String name) throws Exception {
		return Integer.parseInt(getSetting(name));
	}

	/**
	 * Convenience method for getting a parsed setting as a long type.
	 * 
	 * @param value
	 *            Name of setting.
	 * @return A long representation.
	 * @throws Exception
	 */
	protected final long getParsedSettingAsLong(String name) throws Exception {
		return Long.parseLong(getParsedSetting(name));
	}

	/**
	 * Convenience method for getting a parsed setting as a integer type.
	 * 
	 * @param value
	 *            Name of setting.
	 * @return A integer representation.
	 * @throws Exception
	 */
	protected final int getParsedSettingAsInt(String name) throws Exception {
		return Integer.parseInt(getParsedSetting(name));
	}

	/**
	 * Convenience method that parses a formatted string against a set of
	 * pre-defined regular expressions.
	 * 
	 * @param value
	 *            The string to be parsed.
	 * @return The parsed value.
	 * @throws Exception
	 */
	protected final String parseValue(String value) throws Exception {
		Matcher mNr = pNr.matcher(value);

		// Parse for "RandNr" statement
		while (mNr.find()) {
			int upperBounds = Integer.parseInt(mNr.group(1).split(",")[1]
					.trim());
			int lowerBounds = Integer.parseInt(mNr.group(1).split(",")[0]
					.trim());
			String random = Integer.toString(rnd.nextInt(upperBounds
					- lowerBounds)
					+ lowerBounds);
			value = mNr.replaceFirst(random);
		}

		Matcher mOpt = pOpt.matcher(value);

		// Parse for "RandOpt" statement
		while (mOpt.find()) {
			String[] options = mOpt.group(1).split(",");
			String random = options[rnd.nextInt(options.length)].trim();
			value = mOpt.replaceFirst(random);
		}

		return value;
	}

	/**
	 * Convenience method that returns a formatted date.
	 * 
	 * @return A string representing the date.
	 * @throws Exception
	 */
	public final String getDate() {
		return dateFormat.format(new Date());
	}

}