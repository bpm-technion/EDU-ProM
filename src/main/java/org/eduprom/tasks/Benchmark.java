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
		//Arrays.stream()logger.getHandlers()

    	//2017 good: 3, (4?), (5?), 9

        try {
			AdaptiveNoiseBenchmarkConfiguration configuration = AdaptiveNoiseBenchmarkConfiguration.getBuilder()
					.addLogs(Logs.getBuilder().addNumbers(8).addFormat(Logs.CONTEST_2016).build())
					//.addLogs(Logs.getBuilder().addNumbers(5).addFormat(dfciMay).build())
					//.setLogSplitter(InductiveLogSplitting.class)
					//.addLogs(Logs.getBuilder().addNumbers(1, 10).addFormat(dfciApril).build())
					.setNoiseThresholds(NoiseThreshold.uniform(0.2f))
					.addWeights(Weights.getRangeGeneralization(0.2))
					//.addWeights(new Weights(0.0, 1.0, 0.0))
					//.addWeights(Weights.getRange(0.2))
					//.setPartitionNoiseFilter(0.2f)
					.build();
			//IBenchmark benchmark = new AdaptiveNoiseBenchmark(configuration, 10);
			new AdaBenchmarkValidation(configuration, 10).run();


        } catch (Exception ex) {
        	logger.log(Level.SEVERE, "exception when trying to train/evaluate the miner", ex);
        }
        
        logger.info("ended application");
    }
}
