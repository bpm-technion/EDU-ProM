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

    	String filename = "EventLogs\\hw3_log.xes";

    	logManager.readConfiguration(new FileInputStream("./app.properties"));
    	logger.info("started application");

        try {

			// Q2 - PART 1
			InductiveMiner modelInductive = new InductiveMiner(filename);
			modelInductive.Train();
			modelInductive.Export();
			modelInductive.Evaluate();

        	// Q2 - PART 2
            // AlphaPlusEnhanced
			AlphaPlusEnhanced modelPlusEnhanced = new AlphaPlusEnhanced(filename);
			modelPlusEnhanced.Train();
			modelPlusEnhanced.Export();
			modelPlusEnhanced.Evaluate();

			// Q2 - PART 3
            // ModelComparison
            IModel modelIM = new InductiveMiner(filename);
			modelIM.Train();
			double inductiveMinerEvaluate = modelIM.calculateNewEvaluate();

			AlphaPlus modelAlphaPlus = new AlphaPlus(filename);
			modelAlphaPlus.Train();
			double alphaPlusEvaluate = modelAlphaPlus.calculateNewEvaluate();

			if (inductiveMinerEvaluate > alphaPlusEvaluate){
				modelIM.Export();
			} else{
				modelAlphaPlus.Export();
			}

        } catch (Exception ex) {
        	logger.log(Level.SEVERE, "exception when trying to train/evaluate the model", ex);
        }
        
        logger.info("ended application");
    }
}
