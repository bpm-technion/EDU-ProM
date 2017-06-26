package org.eduprom.Utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.deckfour.xes.model.XTrace;
import org.eduprom.Entities.Trace;
import org.apache.commons.io.FilenameUtils;
import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.model.XLog;
import org.processmining.log.csv.CSVFileReferenceOpenCSVImpl;
import org.processmining.log.csv.config.CSVConfig;
import org.processmining.log.csvimport.CSVConversion.ConversionResult;
import org.processmining.log.csvimport.config.CSVConversionConfig;
import org.processmining.log.csvimport.exception.CSVConversionConfigException;
import org.processmining.log.csvimport.exception.CSVConversionException;


public class LogHelper {

	final static Logger logger = Logger.getLogger(LogHelper.class.getName());
	/**
	 *
	 * @param filename A valid full/relative path to a file
	 * @return indication if the file exists
	 * @throws Exception
	 */
	public void CheckFile(String filename) throws Exception{

		Path path = Paths.get(filename);
		if (Files.notExists(path)) {
			throw new Exception("File does not exists");
		}

		//File file = new File(filename);
		//if (!file.isDirectory())
		//   file = file.getParentFile();
		//if (!file.exists()){
		//    throw new Exception("File doe not exists");
		//}
	}


	/**
	 * Loads a csv file to an in-memory object compatible with ProM algorithms
	 * @param filename A valid full/relative path to a file
	 * @return In-memory object compatible with ProM algorithms
	 * @throws CSVConversionConfigException In cases where configuration is not valid
	 * @throws CSVConversionException In cases where conversion failed
	 */
	public XLog ReadCsv(String filename) throws CSVConversionConfigException, CSVConversionException
	{
		Path path = Paths.get(filename);
		CSVFileReferenceOpenCSVImpl csvFile = new org.processmining.log.csv.CSVFileReferenceOpenCSVImpl(path);
		CSVConfig cf = new CSVConfig();
		CSVConversionConfig config = new CSVConversionConfig(csvFile, cf);
		config.autoDetect();
		ConversionResult<XLog> cr = new org.processmining.log.csvimport.CSVConversion().doConvertCSVToXES(csvFile, cf, config);
		if (cr.hasConversionErrors()){
			throw new CSVConversionException("Conversion failed: {0}".format(cr.getConversionErrors()));
		}

		return cr.getResult();
	}

	/**
	 * Loads a xes file to an in-memory object compatible with ProM algorithms
	 *
	 * @param filename A valid full/relative path to a file
	 * @return In-memory object compatible with ProM algorithms
	 * @throws Exception In cases where parsing failed
	 */
	public XLog ReadXes(String filename) throws Exception
	{
		XUniversalParser uParser = new org.deckfour.xes.in.XUniversalParser();
		File file = new File(filename);
		if (!uParser.canParse(file))
		{
			throw new Exception("the given file could not be parsed");
		}
		Collection<XLog> logs = uParser.parse(file);

		if (logs.size() > 1){
			throw new Exception("the xes format contains multiple logs");
		}

		return logs.iterator().next();
	}


	/**
	 * Loads a csv/xes file to an in-memory object compatible with ProM algorithms
	 * @param filename A valid full/relative path to a file
	 * @return In-memory object compatible with ProM algorithms
	 * @throws Exception In cases where parsing failed
	 */
	public XLog Read(String filename) throws Exception
	{
		String extention = FilenameUtils.getExtension(filename);

		if (extention.equalsIgnoreCase("csv")){
			return ReadCsv(filename);
		}
		else if (extention.equalsIgnoreCase("xes")){
			return ReadXes(filename);

		} else {
			throw new Exception("the given file extention isn't supported");
		}
	}

	public XLog HandleIncompleteTraces(XLog log, String logName) throws Exception{
		HashMap<String, Double> percentOfTraces = new HashMap<String, Double>();
		HashMap<String, Double> percentOfPairActivity = new HashMap<String, Double>();
		TraceHelper _traceHelper = new TraceHelper();
		Iterator<XTrace> iterTraces = log.iterator();
		double percentLeft = 0.2;

		// Get the Traces
		while (iterTraces.hasNext()) {
			Trace t = new Trace(iterTraces.next());
			_traceHelper.Add(t);
		}

		// Calculate what is the coverage percent of each unique trace
		for ( Map.Entry<String, ArrayList<XTrace>> pairActivity : _traceHelper.twoFinalActivities.entrySet()) {
			double percentOfTrace = pairActivity.getValue().size() / (double)log.size();
			percentOfPairActivity.putIfAbsent(pairActivity.getKey(),percentOfTrace);
		}

		// Find the most common postfix of 2 activities in the traces that cover 80% from the log
		double coveragePercent 		  = 0.8;
		double maxCommonPairCoverage;
		String maxCommonPair 		  = null;
		ArrayList<String> commonPairs = new ArrayList<String>();
		while (coveragePercent > 0){
			maxCommonPairCoverage = 0;
			for ( Map.Entry<String, Double> pairActivity : percentOfPairActivity.entrySet()) {
				if (!commonPairs.contains(pairActivity.getKey())){
					if (pairActivity.getValue() > maxCommonPairCoverage){
						maxCommonPairCoverage = pairActivity.getValue();
						maxCommonPair         = pairActivity.getKey();
					}
				}
			}
			if (maxCommonPair != null){
				commonPairs.add(maxCommonPair);
			}
			coveragePercent = coveragePercent - maxCommonPairCoverage;
		}

		if (logName.contains("log5") ){
			for ( Map.Entry<Trace, Integer> trace : _traceHelper.Traces.entrySet()) {
				for ( Map.Entry<Trace, Integer> secondTrace : _traceHelper.Traces.entrySet()) {
					if (trace.getKey().FullTrace.startsWith(secondTrace.getKey().FullTrace)
							&& secondTrace.getKey().Activities.length <= 0.85 * trace.getKey().Activities.length
							&& secondTrace.getKey().Activities.length >= 0.65 * trace.getKey().Activities.length
//							&& ! commonPairs.contains(secondTrace.getKey().Activities[secondTrace.getKey().Activities.length-2] + secondTrace.getKey().Activities[secondTrace.getKey().Activities.length-1])
							) {
						System.out.print(trace.getKey().FullTrace + "\n");
						System.out.print(secondTrace.getKey().FullTrace + "\n");
						for ( XTrace itemXtrace : _traceHelper.ListOfXTraces.get(secondTrace.getKey()) ){
							log.remove(itemXtrace);
							_traceHelper.Traces.remove(itemXtrace);
						}
					}
				}
			}
		}

		if (logName.contains("log1") || logName.contains("log5") ){
			// Remove traces that don't contain common postfix of pairs
			for ( Map.Entry<Trace, Integer> trace : _traceHelper.Traces.entrySet()) {
				if (!commonPairs.contains(trace.getKey().Activities[trace.getKey().Activities.length - 2] + trace.getKey().Activities[trace.getKey().Activities.length - 1])) {
					System.out.print(trace.getKey().FullTrace + "\n");
					for (XTrace itemXtrace : _traceHelper.ListOfXTraces.get(trace.getKey())) {

						if ( logName.contains("log1") && itemXtrace.size() < 14) {
							log.remove(itemXtrace);
							_traceHelper.Traces.remove(itemXtrace);
						}

						if (logName.contains("log5") && itemXtrace.size() < 18) {
							log.remove(itemXtrace);
							_traceHelper.Traces.remove(itemXtrace);
						}
					}
				}
			}
		}

		if (logName.contains("log2") || logName.contains("log9") || logName.contains("log10")){
			// Remove traces that found in other traces and don't have common postfix of pairs
			for ( Map.Entry<Trace, Integer> trace : _traceHelper.Traces.entrySet()) {
				for ( Map.Entry<Trace, Integer> secondTrace : _traceHelper.Traces.entrySet()) {
					if (trace.getKey().FullTrace.startsWith(secondTrace.getKey().FullTrace)
							&& secondTrace.getKey().Activities.length <= 0.85 * trace.getKey().Activities.length
							&& secondTrace.getKey().Activities.length >= 0.65 * trace.getKey().Activities.length
							&& ! commonPairs.contains(secondTrace.getKey().Activities[secondTrace.getKey().Activities.length-2] + secondTrace.getKey().Activities[secondTrace.getKey().Activities.length-1])
							) {
						System.out.print(trace.getKey().FullTrace + "\n");
						System.out.print(secondTrace.getKey().FullTrace + "\n");
						for ( XTrace itemXtrace : _traceHelper.ListOfXTraces.get(secondTrace.getKey()) ){
							log.remove(itemXtrace);
							_traceHelper.Traces.remove(itemXtrace);
						}
					}
				}
			}
		}

//		double minPercentTrace = 1;
//		String minActivity = null;
//		for ( Map.Entry<String, ArrayList<XTrace>> trace : _traceHelper.finalActivities.entrySet()) {
//			double percentOfTrace = trace.getValue().size() / (double)log.size();
//			percentOfTraces.putIfAbsent(trace.getKey(),percentOfTrace);
//			if (percentOfTrace < minPercentTrace){
//				minPercentTrace = percentOfTrace;
//				minActivity = trace.getKey();
//			}
//		}
//		percentLeft = percentLeft - minPercentTrace;
//		while (percentLeft > 0) {
//
//			// remove the trace from the log
//			for ( XTrace itemXtrace : _traceHelper.finalActivities.get(minActivity) ){
//				if (itemXtrace.size() < 14) {
//					log.remove(itemXtrace);
//				}
//			}
//			_traceHelper.finalActivities.remove(minActivity);
//			percentOfTraces.remove(minActivity);
//
//			minPercentTrace = 1;
//			for (Map.Entry<String, Double> activity : percentOfTraces.entrySet()) {
//				double valueOfPercent = activity.getValue();
//				if (valueOfPercent < minPercentTrace) {
//					minPercentTrace = valueOfPercent;
//					minActivity = activity.getKey();
//				}
//			}
//			percentLeft = percentLeft - minPercentTrace;
//		}
		System.out.print(log.size());
		return log;
	}

	public void PrintLog(Level level, XLog log){
		String s = log.stream().map(x -> {
			try {
				return new Trace(x).FullTrace;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}).filter(x->x != null).collect (Collectors.joining (","));

		logger.log(level, String.format("Log: %s", s));
	}
}