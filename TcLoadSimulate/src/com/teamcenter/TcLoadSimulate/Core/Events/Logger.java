package com.teamcenter.TcLoadSimulate.Core.Events;

import java.io.FileWriter;

import com.teamcenter.TcLoadSimulate.TcLoadSimulate;
import com.teamcenter.TcLoadSimulate.Core.Status;
import com.teamcenter.TcLoadSimulate.Core.Worker;

public class Logger implements EventListener {
	private static FileWriter fw = null;

	/**
	 * The operating system specific separator object.
	 */
	private static final String separator = System
			.getProperty("line.separator");

	public static final void reset() {
		try {
			if (fw != null) {
				fw.flush();
				fw.close();
				fw = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleWorkerEvent(Event e) {
		try {
			if (e.getSource() instanceof Worker) {
				if (TcLoadSimulate.outputFile != null) {
					Worker w = (Worker) e.getSource();
					if (w.status == Status.SLEEPING) {
						if (fw == null) {
							fw = new FileWriter(
									TcLoadSimulate.outputFile.getAbsoluteFile(),
									false);

							fw.write(String
									.format("\"DATE\";\"WORKER\";\"MODULE\";\"ITERATION\";\"TIME\";\"MISC.\"%s",
											separator));
							fw.flush();
						}
						fw.write(String.format(
								"\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";\"%s\"%s",
								e.getDate(), w.id, w.getModuleType(),
								w.getIterations(), w.getModuleTimeDelta(),
								w.getModuleMiscInfo(), separator));
						fw.flush();
					}
				}
			}
		} catch (Exception ex) {
			Console.err(ex);
		}
	}
}
