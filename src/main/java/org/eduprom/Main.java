package org.eduprom;

import org.eduprom.Miners.Alpha.AlphaPlus;
import org.eduprom.Miners.Alpha.AlphaPlusEnhanced;
import org.eduprom.Miners.IMiner;
import org.eduprom.Miners.InductiveMiner;

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
    	//logger.info("hiiiiiiiiiiiiiiii");
        try {
        	IMiner miner = new InductiveMiner(filename); // this one for part1
			//IMiner miner = new AlphaPlus(filename);
			//IMiner miner = new AlphaPlusEnhanced(filename);
			miner.Train();
        	miner.Export();
        	miner.Evaluate();
        } catch (Exception ex) {
        	logger.log(Level.SEVERE, "exception when trying to train/evaluate the miner", ex);
        }
        
        logger.info("ended application");
    }
}
