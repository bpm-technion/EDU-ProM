package org.eduprom.tasks;

import org.eduprom.benchmarks.configuration.Logs;
import org.eduprom.benchmarks.configuration.NoiseThreshold;
import org.eduprom.benchmarks.configuration.Weights;
import org.eduprom.miners.IMiner;
import org.eduprom.miners.adaptiveNoise.AdaMiner;
import org.eduprom.miners.adaptiveNoise.configuration.AdaptiveNoiseConfiguration;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class AdaMinerTask {
	
	private static final LogManager logManager = LogManager.getLogManager();
	private static final Logger logger = Logger.getLogger(AdaMinerTask.class.getName());
	
    public static void main(String[] args) throws Exception {

        String filename = "EventLogs\\test_CAISE.csv";

    	logManager.readConfiguration(new FileInputStream("./app.properties"));
    	logger.info("started application");
    	    	    	
        try {
			AdaptiveNoiseConfiguration adaptiveNoiseConfiguration = AdaptiveNoiseConfiguration.getBuilder()
					.setNoiseThresholds(0.0f, 0.1f, 0.2f)
					.setWeights(Weights.getUniform())
					.build();
			AdaMiner miner = new AdaMiner(filename, adaptiveNoiseConfiguration);

        	miner.mine();
			// miner.getProcessTree() is available in this stage
        	miner.export();
        	// this exports the model image and pnml to output folder
			miner.evaluate();
			//this makes conformance info avilable via: miner.getConformanceInfo()
			logger.info(String.format("model conformance %s",
					miner.getConformanceInfo().toString()));

        } catch (Exception ex) {
        	logger.log(Level.SEVERE, "exception when trying to train/evaluate the miner", ex);
        }
        
        logger.info("ended application");
    }
}
