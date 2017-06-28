package org.eduprom;

import org.deckfour.xes.model.XLog;
import org.eduprom.Models.Alpha.AlphaPlusPlus;
import org.eduprom.Models.Alpha.AlphaPlusPlusEnhanced;
import org.eduprom.Models.IModel;
import org.eduprom.Models.InductiveMiner;
import org.eduprom.Utils.LogHelper;
import org.eduprom.Utils.TraceHelper;

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
//		String filename = "EventLogs\\log5.xes";
//		String filename = "EventLogs\\log6.xes";
//		String filename = "EventLogs\\log7.xes";
//		String filename = "EventLogs\\log8.xes";
//		String filename = "EventLogs\\log9.xes";
		String filename = "EventLogs\\log10.xes";

		logManager.readConfiguration(new FileInputStream("./app.properties"));
		logger.info("started application");
		LogHelper _logHelper = new LogHelper();;
		XLog _log 			 = _logHelper.Read(filename);

		try {

			if (filename.contains("log1.xes")) //  incomplete traces
			{
				XLog finalLog = _logHelper.HandleIncompleteTraces(_log, filename);

				InductiveMiner inductiveModel = new InductiveMiner(finalLog, filename,0);
				inductiveModel.Train();
				inductiveModel.Export();
				inductiveModel.calculateNewEvaluate();

				AlphaPlusPlus alphaModel = new AlphaPlusPlus(finalLog,filename);
				alphaModel.Train();
				alphaModel.Export();
				alphaModel.Evaluate();

				// TODO : check evaluation measures - 0.5 * fitness + 0.25 * precision + 0.25 * generalization
			}
			else if (filename.contains("log2.xes")) // incomplete traces
			{
				XLog finalLog = _logHelper.HandleIncompleteTraces(_log, filename);
				// Run Inductive Miner Model
				InductiveMiner inductiveModel = new InductiveMiner(finalLog, filename,0);
				inductiveModel.Train();
				inductiveModel.Export();
				// Run AlphaPlusPlus Model
				AlphaPlusPlus alphaModel = new AlphaPlusPlus(finalLog,filename);
				alphaModel.Train();
				alphaModel.Export();
				alphaModel.Evaluate();

				// TODO : check evaluation measures
			}
			else if (filename.contains("log5.xes")) // incomplete traces
			{
				XLog finalLog = _logHelper.HandleIncompleteTraces(_log, filename);
				// Run Inductive Miner Model
				InductiveMiner inductiveModel = new InductiveMiner(filename,0);
				inductiveModel.Train();
				inductiveModel.Export();
				// Run AlphaPlusPlus Model
				AlphaPlusPlus alphaModel = new AlphaPlusPlus(finalLog,filename);
				alphaModel.Train();
				alphaModel.Export();
				alphaModel.Evaluate();
				// TODO : check evaluation measures
			}
			else if (filename.contains("log9.xes")) // incomplete traces
			{
				XLog finalLog = _logHelper.HandleIncompleteTraces(_log, filename);
				// Run Inductive Miner Model
				InductiveMiner inductiveModel = new InductiveMiner(filename,0);
				inductiveModel.Train();
				inductiveModel.Export();
				// Run AlphaPlusPlus Model
				AlphaPlusPlus alphaModel = new AlphaPlusPlus(finalLog,filename);
				alphaModel.Train();
				alphaModel.Export();
				alphaModel.Evaluate();
				// TODO : check evaluation measures
			}
			else if (filename.contains("log10.xes")) // incomplete traces
			{
				XLog finalLog = _logHelper.HandleIncompleteTraces(_log, filename);
				// Run Inductive Miner Model
				InductiveMiner inductiveModel = new InductiveMiner(filename,0);
				inductiveModel.Train();
				inductiveModel.Export();
				// Run AlphaPlusPlus Model
				AlphaPlusPlus alphaModel = new AlphaPlusPlus(finalLog,filename);
				alphaModel.Train();
				alphaModel.Export();
				alphaModel.Evaluate();
				// TODO : check evaluation measures
			}
			else {
				// Run Inductive Miner Model with threshold = 0
				InductiveMiner inductiveModel = new InductiveMiner(filename,0);
				inductiveModel.Train();
				inductiveModel.Export();
				// Run Inductive Miner Model with threshold = 5%
				InductiveMiner inductiveModelT5 = new InductiveMiner(filename,(float)0.05);
				inductiveModelT5.Train();
				inductiveModelT5.Export();
				// Run Inductive Miner Model with threshold = 10%
				InductiveMiner inductiveModelT10 = new InductiveMiner(filename,(float)0.1);
				inductiveModelT10.Train();
				inductiveModelT10.Export();
				// Run AlphaPlusPlus Model
				AlphaPlusPlus alphaModel = new AlphaPlusPlus(filename);
				alphaModel.Train();
				alphaModel.Export();
				alphaModel.Evaluate();
				// Run AlphaPlusPlusEnhanced
				AlphaPlusPlusEnhanced alphaPlusPlusEnhancedModel = new AlphaPlusPlusEnhanced(filename, 0.1);
				alphaPlusPlusEnhancedModel.Train();
				alphaPlusPlusEnhancedModel.Export();
				alphaPlusPlusEnhancedModel.Evaluate();
				// TODO : check evaluation measures
				// TODO : if (T0 , T5, T10) is the best export the results
			}

		} catch (Exception ex) {
			logger.log(Level.SEVERE, "exception when trying to train/evaluate the model", ex);
		}

		logger.info("ended application");
	}
}