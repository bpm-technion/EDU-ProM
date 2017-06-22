package org.eduprom;

import org.deckfour.xes.model.XLog;
import org.eduprom.Models.Alpha.AlphaPlus;
import org.eduprom.Models.Alpha.AlphaPlusEnhanced;
import org.eduprom.Models.Alpha.AlphaPlusPlus;
import org.eduprom.Models.Alpha.AlphaSharp;
import org.eduprom.Models.EnumerateAllPaths;
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
			XLog finalLog = _logHelper.HandleIncompleteTraces(_log);
//			if (filename.contains("log1.xes")) //  incomplete traces
//			{
//				XLog finalLog = _logHelper.HandleIncompleteTraces(_log);
////
//				InductiveMiner inductiveModel = new InductiveMiner(finalLog, filename,0);
//				inductiveModel.Train();
//				inductiveModel.Export();
////				double = 0.5 * fitness + 0.25 * precision + 0.25 * generalization
//
////				InductiveMiner inductiveModel5 = new InductiveMiner(filename,(float)0.1);
////				inductiveModel5.Train();
////				inductiveModel5.Export();
//
				AlphaPlusPlus alphaModel = new AlphaPlusPlus(finalLog,filename);
				alphaModel.Train();
				alphaModel.Export();
				alphaModel.Evaluate();

////				AlphaPlusEnhanced alphaModel = new AlphaPlusEnhanced(finalLog,filename);
////				alphaModel.Train();
////				alphaModel.Export();
////				alphaModel.Evaluate();
//			}
//			else if (filename.contains("log2.xes")) // incomplete traces
//			{
//				XLog finalLog = _logHelper.HandleIncompleteTraces(_log);
//				InductiveMiner inductiveModel = new InductiveMiner(filename,0);
//				inductiveModel.Train();
//				inductiveModel.Export();
//
////				AlphaPlusPlus alphaModel = new AlphaPlusPlus(finalLog,filename);
////				alphaModel.Train();
////				alphaModel.Export();
////				alphaModel.Evaluate();
//
//			}
//			else if (filename.contains("log3.xes"))
//			{
//				_logHelper.HandleIncompleteTraces(_log);
//			}
//			else if (filename.contains("log4.xes"))
//			{
//
//			}
//			else if (filename.contains("log5.xes")) // incomplete traces
//			{
//
//			}
//			else if (filename.contains("log6.xes"))
//			{
//
//			}
//			else if (filename.contains("log7.xes"))
//			{
//
//			}
//			else if (filename.contains("log8.xes"))
//			{
//
//			}
//			else if (filename.contains("log9.xes")) // incomplete traces
//			{
//
//			}
//			else if (filename.contains("log10.xes")) // incomplete traces
//			{
//
//			}


        } catch (Exception ex) {
        	logger.log(Level.SEVERE, "exception when trying to train/evaluate the model", ex);
        }
        
        logger.info("ended application");
    }
}
