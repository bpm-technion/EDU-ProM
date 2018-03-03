package org.eduprom.tasks;

import org.eduprom.benchmarks.configuration.Logs;
import org.eduprom.benchmarks.configuration.NoiseThreshold;
import org.eduprom.benchmarks.configuration.Weights;
import org.eduprom.miners.adaptiveNoise.benchmarks.AdaBenchmarkValidation;
import org.eduprom.benchmarks.IBenchmark;
import org.eduprom.miners.adaptiveNoise.benchmarks.AdaptiveNoiseBenchmarkConfiguration;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class Benchmark {
	
	private static final LogManager logManager = LogManager.getLogManager();
	private static final Logger logger = Logger.getLogger(Benchmark.class.getName());
	public static final String dfciApril = "EventLogs\\\\DFCI_Train_April.csv";
	public static final String dfciMay = "EventLogs\\\\DFCI_Test_May.csv";
	
    public static void main(String[] args) throws Exception {

    	logManager.readConfiguration(new FileInputStream("./app.properties"));
    	logger.info("started application");

        try {
			AdaptiveNoiseBenchmarkConfiguration configuration = AdaptiveNoiseBenchmarkConfiguration.getBuilder()
					.addLogs(Logs.getBuilder().addFile(dfciApril).build())
					.setNoiseThresholds(NoiseThreshold.uniform(0.2f))
					.addWeights(Weights.getUniform())
					.build();
			new AdaBenchmarkValidation(configuration, 10).run();


        } catch (Exception ex) {
        	logger.log(Level.SEVERE, "exception when trying to train/evaluate the miner", ex);
        }
        
        logger.info("ended application");
    }
}
