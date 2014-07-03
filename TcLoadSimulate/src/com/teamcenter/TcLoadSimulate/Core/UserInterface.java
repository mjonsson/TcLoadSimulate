package com.teamcenter.TcLoadSimulate.Core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.UnmarshalException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.TcLoadSimulate.TcLoadSimulate;
import com.teamcenter.TcLoadSimulate.Core.Events.EventListener;
import com.teamcenter.TcLoadSimulate.Core.Events.Logger;

/**
 * GUI class that initiates display and widgets.
 * 
 */
public class UserInterface implements EventListener {
	/**
	 * The display object
	 */
	private static Display display;
	/**
	 * The shell object.
	 */
	private static Shell shell;
	/**
	 * The table in which all worker information is shown.
	 */
	private static Table table;
	/**
	 * The start button.
	 */
	private static Button startButton;
	/**
	 * The stop button.
	 */
	private static Button stopButton;

	private static Button rereadButton;

	private static Button loadButton;

	private static Button outputButton;

	private static Text outputFile;

	public final static Table getTable() {
		return table;
	}

	public final static void DisplayError(String msg) {
		MessageBox msgBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
		msgBox.setText("An error has occurred");
		msgBox.setMessage(msg);
		msgBox.open();
	}

	public final static void DisplayError(String msg, Exception e) {
		MessageBox msgBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
		msgBox.setText("An error has occurred");

		if (e instanceof UnmarshalException) {
			UnmarshalException ume = (UnmarshalException)e;
			
			msg += "\n\nMessage:\n\n" + ume.getLinkedException().getMessage();
		}
		if (e.getStackTrace().length > 0) {
			msg += "\n\nStack trace:\n\n";
			for (StackTraceElement ste : e.getStackTrace()) {
				msg += ste.toString();
			}
		}
		msgBox.setMessage(msg);
		msgBox.open();
	}

	/**
	 * Setup the application window and all its widgets.
	 * 
	 * @throws Exception
	 */
	public static void init() throws Exception {
		display = new Display();

		shell = new Shell(display);
		shell.setText("TcLoadSimulate");
		final Image imgTeamcenter = new Image(display, TcLoadSimulate.class
				.getClassLoader().getResourceAsStream(
						"com/teamcenter/TcLoadSimulate/Images/teamcenter.png"));
		shell.setImage(imgTeamcenter);
		shell.setLayout(new GridLayout(6, false));
		shell.setSize(990, 700);
		shell.setMinimumSize(990, 700);

		table = new Table(shell, SWT.MULTI | SWT.FULL_SELECTION | SWT.FLAT);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 6;
		table.setLayoutData(gd);

		TableColumn dummy = new TableColumn(table, SWT.NONE);
		dummy.setWidth(0);
		dummy.setMoveable(false);
		dummy.setResizable(false);
		TableColumn status = new TableColumn(table, SWT.NONE);
		status.setWidth(9);
		status.setMoveable(false);
		status.setResizable(false);
		TableColumn date = new TableColumn(table, SWT.NONE);
		date.setText("Date");
		date.setWidth(110);
		date.setMoveable(true);
		date.setResizable(false);
		TableColumn worker = new TableColumn(table, SWT.NONE);
		worker.setText("Worker");
		worker.setWidth(80);
		worker.setMoveable(true);
		worker.setResizable(true);
		TableColumn module = new TableColumn(table, SWT.NONE);
		module.setText("Module");
		module.setWidth(110);
		module.setMoveable(true);
		module.setResizable(true);
		TableColumn iteration = new TableColumn(table, SWT.RIGHT);
		iteration.setText("Iteration");
		iteration.setWidth(70);
		iteration.setMoveable(true);
		iteration.setResizable(false);
		TableColumn time = new TableColumn(table, SWT.RIGHT);
		time.setText("Elapsed");
		time.setWidth(70);
		time.setMoveable(true);
		time.setResizable(false);
		TableColumn progress = new TableColumn(table, SWT.RIGHT);
		progress.setText("Progress");
		progress.setWidth(58);
		progress.setMoveable(true);
		progress.setResizable(false);
		TableColumn misc = new TableColumn(table, SWT.NONE);
		misc.setText("Extra information");
		misc.setWidth(434);
		misc.setMoveable(false);
		misc.setResizable(true);

		final Composite c = new Composite(shell, SWT.NONE);
		GridLayout gl = new GridLayout(1, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		c.setLayout(gl);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		final Label outputLabel = new Label(c, SWT.NONE);
		outputLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		outputLabel.setText("Output file:");
		outputFile = new Text(c, SWT.SINGLE | SWT.BORDER);
		outputFile
				.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		if (TcLoadSimulate.configurationFile != null)
			outputFile.setText(TcLoadSimulate.configurationFile
					.getAbsolutePath());

		outputButton = new Button(shell, SWT.FLAT);
		gd = new GridData(80, 30);
		gd.verticalAlignment = SWT.BOTTOM;
		outputButton.setLayoutData(gd);
		outputButton.setText("OUTPUT...");
		outputButton.setToolTipText("Set output file.");
		outputButton.setFont(new Font(display, display.getSystemFont()
				.getFontData()[0].getName(), 8, SWT.BOLD));
		loadButton = new Button(shell, SWT.FLAT);
		loadButton.setLayoutData(gd);
		loadButton.setText("OPEN...");
		loadButton.setToolTipText("Open configuration file.");
		loadButton.setFont(new Font(display, display.getSystemFont()
				.getFontData()[0].getName(), 8, SWT.BOLD));
		rereadButton = new Button(shell, SWT.FLAT);
		rereadButton.setLayoutData(gd);
		rereadButton.setText("REREAD");
		rereadButton.setToolTipText("Reread configuration file.");
		rereadButton.setFont(new Font(display, display.getSystemFont()
				.getFontData()[0].getName(), 8, SWT.BOLD));
		rereadButton.setEnabled(false);

		startButton = new Button(shell, SWT.FLAT);
		startButton.setLayoutData(gd);
		startButton.setImage(new Image(display, TcLoadSimulate.class
				.getClassLoader().getResourceAsStream(
						"com/teamcenter/TcLoadSimulate/Images/start.png")));
		startButton.setText("START");
		startButton.setFont(new Font(display, display.getSystemFont()
				.getFontData()[0].getName(), 8, SWT.BOLD));
		startButton.setEnabled(false);
		stopButton = new Button(shell, SWT.FLAT);
		stopButton.setLayoutData(gd);
		stopButton.setImage(new Image(display, TcLoadSimulate.class
				.getClassLoader().getResourceAsStream(
						"com/teamcenter/TcLoadSimulate/Images/stop.png")));
		stopButton.setText("STOP");
		stopButton.setFont(new Font(display, display.getSystemFont()
				.getFontData()[0].getName(), 8, SWT.BOLD));
		stopButton.setEnabled(false);

		//
		// Listeners
		//
		loadButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				table.deselectAll();
				table.notifyListeners(SWT.Selection, new Event());
				final FileDialog dlg = new FileDialog(shell, SWT.OPEN);
				dlg.setText("Select configuration file");
				dlg.setFilterExtensions(new String[] { "*.xml" });
				if (TcLoadSimulate.configurationFile != null && TcLoadSimulate.configurationFile.exists())
					dlg.setFilterPath(TcLoadSimulate.configurationFile
							.getParent());
				else
					dlg.setFilterPath(TcLoadSimulate.appPath.getPath());
				String file = dlg.open();
				if (file != null) {
					TcLoadSimulate.configurationFile = new File(file);
					table.removeAll();
					TcLoadSimulate.start();
				}
				table.setFocus();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		outputFile.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				Text t = (Text) e.widget;
				if (t.getText().isEmpty()) return;
				table.deselectAll();
				table.notifyListeners(SWT.Selection, new Event());
				File file = new File(t.getText());
				Logger.reset();
				try {
					file.createNewFile();
					TcLoadSimulate.outputFile = file;
				} catch (IOException ex) {
					TcLoadSimulate.outputFile = null;
					DisplayError("File could not be created.");
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});

		outputFile.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == 13) {
					table.setFocus();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		outputButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				table.deselectAll();
				table.notifyListeners(SWT.Selection, new Event());
				final FileDialog dlg = new FileDialog(shell, SWT.SAVE);
				dlg.setText("Select output file");
				dlg.setFilterExtensions(new String[] { "*.csv" });
				String file = dlg.open();
				if (file != null) {
					TcLoadSimulate.outputFile = new File(file);
					outputFile.setText(TcLoadSimulate.outputFile
							.getAbsolutePath());
					outputFile.notifyListeners(SWT.FocusOut, new Event());
				}
				table.setFocus();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		rereadButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				table.deselectAll();
				table.notifyListeners(SWT.Selection, new Event());
				rereadButton.setEnabled(false);
				table.removeAll();
				table.setFocus();
				TcLoadSimulate.start();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		startButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				final List<Worker> wl = new ArrayList<Worker>();
				for (TableItem item : table.getSelection()) {
					wl.add((Worker) item.getData("worker"));
				}
				startButton.setEnabled(false);
				table.deselectAll();
				Application.startWorkers(wl);
				table.setFocus();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		stopButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				final List<Worker> wl = new ArrayList<Worker>();
				for (TableItem item : table.getSelection()) {
					wl.add((Worker) item.getData("worker"));
				}
				stopButton.setEnabled(false);
				table.deselectAll();
				Application.stopWorkers(wl);
				table.setFocus();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		table.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (table.getSelectionCount() == 0) {
					if (startButton.isEnabled())
						startButton.setEnabled(false);
					if (stopButton.isEnabled())
						stopButton.setEnabled(false);
				} else {
					boolean started = false;
					boolean stopped = false;
					for (TableItem item : table.getSelection()) {
						Worker w = (Worker) item.getData("worker");

						if (w.mode == Mode.STARTED)
							started = true;
						else
							stopped = true;
					}

					if (started && !stopButton.isEnabled())
						stopButton.setEnabled(true);
					else if (!started)
						stopButton.setEnabled(false);
					if (stopped && !startButton.isEnabled())
						startButton.setEnabled(true);
					else if (!stopped)
						startButton.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		table.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.stateMask == SWT.CTRL && e.keyCode == 'a') {
					if (table.getSelectionCount() == table.getItems().length)
						table.deselectAll();
					else
						table.selectAll();
					table.notifyListeners(SWT.Selection, new Event());
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		shell.open();
	}

	public static final void loop() {
		// Need to reset reread button if it has been pressed
		if (!rereadButton.isDisposed() && !rereadButton.isEnabled())
			rereadButton.setEnabled(true);
		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}

	public static final void updateUI() {
		for (TableItem item : table.getItems()) {
			Worker w = (Worker) item.getData("worker");
			if (w.mode == Mode.STARTED) {
				if (rereadButton.isEnabled())
					rereadButton.setEnabled(false);
				if (outputButton.isEnabled())
					outputButton.setEnabled(false);
				if (loadButton.isEnabled())
					loadButton.setEnabled(false);
				if (outputFile.isEnabled())
					outputFile.setEnabled(false);
				return;
			}
			if (!rereadButton.isEnabled())
				rereadButton.setEnabled(true);
			if (!outputButton.isEnabled())
				outputButton.setEnabled(true);
			if (!loadButton.isEnabled())
				loadButton.setEnabled(true);
			if (!outputFile.isEnabled())
				outputFile.setEnabled(true);
		}
	}

	private static final void updateWorkerStatus(
			final com.teamcenter.TcLoadSimulate.Core.Events.Event e) {
		final Worker w = (Worker) e.getSource();

		switch (w.status) {
		case RUNNING:
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					TableItem t = w.getTableItem();

					t.setBackground(1, new Color(display, 255, 255, 150));
					t.setText(new String[] { null, null, e.getDate(), w.id,
							w.getModuleType(), w.getIterations(), "---",
							w.getPercent(), "" });
				}
			});
			break;
		case SLEEPING:
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					TableItem t = w.getTableItem();
					t.setBackground(1, new Color(display, 150, 150, 255));
					t.setText(new String[] { null, null, e.getDate(), w.id,
							w.getModuleType(), w.getIterations(),
							w.getModuleTimeDelta(), w.getPercent(),
							w.getModuleMiscInfo() });
				}
			});
			break;
		case FINISHED:
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					TableItem t = w.getTableItem();
					t.setBackground(1, new Color(display, 150, 255, 150));
					t.setText(new String[] { null, null, e.getDate(), w.id,
							w.getModuleType(), w.getIterations(),
							w.getModuleTimeDelta(), w.getPercent(),
							w.getModuleMiscInfo() });
				}
			});
			break;
		case ERROR:
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					TableItem t = w.getTableItem();
					t.setBackground(1, new Color(display, 255, 150, 150));
					t.setText(new String[] { null, null, e.getDate(), w.id,
							w.getModuleType(), null, null, null,
							"Please see standard error console output for detailed error message" });
				}
			});
			break;
		case NONE:
			break;
		default:
			break;
		}
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				updateUI();
			}
		});
	}

	@Override
	public void handleWorkerEvent(
			com.teamcenter.TcLoadSimulate.Core.Events.Event e) {
		if (e.getSource() instanceof Worker) {
			updateWorkerStatus(e);
		}
	}
}
