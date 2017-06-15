package org.eduprom;

import org.eduprom.Models.Alpha.AlphaPlus;
import org.eduprom.Models.Alpha.AlphaPlusEnhanced;
import org.eduprom.Models.IModel;
import org.eduprom.Models.InductiveMiner;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class Main {
	
	private static final LogManager logManager = LogManager.getLogManager();
	final static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {

    	//String filename = "EventLogs\\contest_dataset\\test_log_may_1.xes";
    	String filename = "EventLogs\\hw3_log.xes";
//    	String filename = "EventLogs\\sample2.xes";
        //String filename = "EventLogs\\gamma2.csv";
    	logManager.readConfiguration(new FileInputStream("./app.properties"));
    	logger.info("started application");
    	    	    	
        try {
            //AlphaPlusEnhanced
//			AlphaPlusEnhanced model = new AlphaPlusEnhanced(filename);
//          AlphaPlus model = new AlphaPlus(filename);

            //ModelComparison
            IModel modelIM = new InductiveMiner(filename);
			modelIM.Train();
			double inductiveMinerEvaluate = modelIM.calculateNewEvaluate();

//			AlphaPlus modelAlpha = new AlphaPlus(filename);
//			modelAlpha.Train();
//			double alphaEvaluate = modelAlpha.calculateNewEvaluate();

//			if(inductiveMinerEvaluate > alphaEvaluate){
//				modelIM.Export();
//			} else{
//				modelAlpha.Export();
//			}

//			model.Train();
//        	model.Export();
//			model.Evaluate();

        } catch (Exception ex) {
        	logger.log(Level.SEVERE, "exception when trying to train/evaluate the model", ex);
        }
        
        logger.info("ended application");
    }
}
