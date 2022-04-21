package dtu.dcr.conformance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.in.XMxmlGZIPParser;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import dtu.dcr.engine.Activity;
import dtu.dcr.engine.Process;
import dtu.dcr.engine.Simulation;
import dtu.dcr.io.json.DcrJsonImporter;

public class ConformanceTester {

	public static void main(String[] args) throws IOException {

		if (args.length != 3) {
			System.err.println("Use: java -jar file.jar JSON_MODEL XES_LOG_FILE TRUE|FALSE");
			System.err.println(
					"The last parameter refers to whether the open world assumption should be used (true means it should be used; false it should not)");
			System.exit(1);
		}

		String modelFile = args[0];
		String logFile = args[1];
		boolean openWorld = Boolean.parseBoolean(args[2]);

		Process p = DcrJsonImporter.importProcess(Files.readString(Path.of(modelFile)));
		XLog log = parseLog(logFile);

		int totalTraces = 0;
		int acceptingTraces = 0;

		for (XTrace t : log) {
			Simulation simulation = new Simulation("", p);
			totalTraces++;
			boolean interrupted = false;
			for (XEvent e : t) {
				Activity activityToExecute = p.getActivityFromName(XConceptExtension.instance().extractName(e));
				if (openWorld) {
					if (activityToExecute != null) {
						simulation.execute(activityToExecute.getId());
					}
				} else {
					if (activityToExecute != null
							&& simulation.getEnabledActivities().contains(activityToExecute.getId())) {
						simulation.execute(activityToExecute.getId());
					} else {
						interrupted = true;
						break;
					}
				}
			}
			if (!interrupted && simulation.isAccepting()) {
				acceptingTraces++;
			}
		}

		System.out.println("Total traces: " + totalTraces);
		System.out.println("Accepting traces: " + acceptingTraces);
		System.out.println("Ratio: " + (acceptingTraces / totalTraces));
	}

	private static XLog parseLog(String fileName) {
		XParser[] parsers = new XParser[] { new XesXmlGZIPParser(), new XesXmlParser(), new XMxmlParser(),
				new XMxmlGZIPParser() };
		File file = new File(fileName);
		for (XParser p : parsers) {
			if (p.canParse(file)) {
				try {
					return p.parse(file).get(0);
				} catch (Exception e) {
				}
			}
		}
		return null;
	}
}
