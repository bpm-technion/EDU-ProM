package org.eduprom;

import org.deckfour.xes.model.XLog;
import org.eduprom.Miners.InductiveMiner;
import org.eduprom.Miners.Alpha.AlphaPlusPlus;
import org.eduprom.Miners.Alpha.AlphaPlusPlusEnhanced;
import org.eduprom.Utils.LogHelper;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class Main {

	private static final LogManager logManager = LogManager.getLogManager();
	final static Logger logger = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) throws Exception {

//    	String filename = "EventLogs\\log1.xes";
//		String filename = "EventLogs\\log2.xes";
//		String filename = "EventLogs\\log3.xes";
//		String filename = "EventLogs\\log4.xes";
		String filename = "EventLogs\\log5.xes";
//		String filename = "EventLogs\\log6.xes";
//		String filename = "EventLogs\\log7.xes";
//		String filename = "EventLogs\\log8.xes";
//		String filename = "EventLogs\\log9.xes";
//		String filename = "EventLogs\\log10.xes";

		logManager.readConfiguration(new FileInputStream("./app.properties"));
		logger.info("started application");
		LogHelper _logHelper = new LogHelper();;
		XLog _log 			 = _logHelper.Read(filename);

		try {

			if (filename.contains("log1.xes")) //  incomplete traces
			{
				XLog finalLog = _logHelper.HandleIncompleteTraces(_log, filename);

				AlphaPlusPlus alphaModel = new AlphaPlusPlus(finalLog,filename);
				alphaModel.Train();
				alphaModel.Export();
				alphaModel.Evaluate();
				// Measure - 0.58423

				// TODO : check evaluation measures - 0.5 * fitness + 0.25 * precision + 0.25 * generalization
			}
			else if (filename.contains("log2.xes")) // incomplete traces
			{
				XLog finalLog = _logHelper.HandleIncompleteTraces(_log, filename);
				// Run Inductive Miner Model
				InductiveMiner inductiveModel = new InductiveMiner(finalLog, filename,0);
				inductiveModel.Train();
				inductiveModel.Export();
				inductiveModel.Evaluate();
				// Log 2: Measure - 0.796487

				// AlphaPlusPlus Model
				// Log 2: Measure - 0.0

				// TODO : check evaluation measures
			}
			else if (filename.contains("log5.xes")) // incomplete traces
			{
				XLog finalLog = _logHelper.HandleIncompleteTraces(_log, filename);
				// Run Inductive Miner Model
				InductiveMiner inductiveModel = new InductiveMiner(finalLog, filename,0);
				inductiveModel.Train();
				inductiveModel.Export();


			}
			else if (filename.contains("log9.xes")) // incomplete traces
			{
				XLog finalLog = _logHelper.HandleIncompleteTraces(_log, filename);
				// Run Inductive Miner Model
				InductiveMiner inductiveModel = new InductiveMiner(filename,0);
				inductiveModel.Train();
				inductiveModel.Export();
				inductiveModel.Evaluate();
				// Log 9: Measure - 0.7893305

				// AlphaPlusPlus Model
				// Log 9: Measure - 0.47700835

				// TODO : check evaluation measures
			}
			else if (filename.contains("log10.xes")) // incomplete traces
			{
				XLog finalLog = _logHelper.HandleIncompleteTraces(_log, filename);
				// Run Inductive Miner Model
				InductiveMiner inductiveModel = new InductiveMiner(filename,0);
				inductiveModel.Train();
				inductiveModel.Export();
				inductiveModel.Evaluate();
				// Log 10: Measure - 0.82085675

				// AlphaPlusPlus Model
				// Log 10: Measure - 0.5684346

				// TODO : check evaluation measures
			}
			else {
				// Run Inductive Miner Model with threshold = 0
				if (filename.contains("log6.xes") || filename.contains("log7.xes") || filename.contains("log8.xes") ||
						filename.contains("log3.xes")) {
					InductiveMiner inductiveModel = new InductiveMiner(filename, 0);
					inductiveModel.Train();
					inductiveModel.Export();
//				inductiveModel.Evaluate();
				}
				// Log 4: Measure - 0.79240975
				// Log 6: Measure - 0.8525085
				// Log 7: Measure - 0.82643825
				// Log 8: Measure - 0.9290375

				// Run Inductive Miner Model with threshold = 5%
				if (filename.contains("log4.xes")){
					InductiveMiner inductiveModelT5 = new InductiveMiner(filename,(float)0.05);
					inductiveModelT5.Train();
					inductiveModelT5.Export();
//				inductiveModelT5.Evaluate();
				}
				// Log 4: Measure - 0.79241125
				// Log 6: Measure - 0.8525085
				// Log 7: Measure - 0.82643825
				// Log 8: Measure - 0.9290365

				//Inductive Miner Model with threshold = 10%
				// Log 4: Measure - 0.79241075
				// Log 6: Measure - 0.8525085
				// Log 7: Measure - 0.82643825
				// Log 8: Measure - 0.929038

				// AlphaPlusPlus Model
				// Log 4: Measure - 0.604647
				// Log 6: Measure - 0.77651115
				// Log 7: Measure - 0.29259345
				// Log 8: Measure - 0.61220828

				// AlphaPlusPlusEnhanced
				// Log 4: Measure - 0.60506057
				// Log 6: Measure - 0.758872
				// Log 7: Measure - 0.292407775
				// Log 8: Measure - 0.61363398

				// TODO : check evaluation measures
				// TODO : if (T0 , T5, T10) is the best export the results
			}

		} catch (Exception ex) {
			logger.log(Level.SEVERE, "exception when trying to train/evaluate the model", ex);
		}

		logger.info("ended application");
	}
}
