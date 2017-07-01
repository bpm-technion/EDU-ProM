package org.eduprom.Miners.Alpha;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eduprom.Entities.Trace;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Sharon Hirsch on 23/06/2017.
 */
public class AlphaPlusPlusEnhanced  extends AlphaPlusPlus {

    public HashMap<Trace, Double> percentOfTraces = new HashMap<Trace, Double>();
    private double _threshold = 0;

    public AlphaPlusPlusEnhanced(String filename) throws Exception {
        super(filename);
    }

    public AlphaPlusPlusEnhanced( String filename, double threshold) throws Exception {
        super(filename);
        _threshold = threshold;
        _log       = _logHelper.Read(_filename);
    }

    public AlphaVersion GetVersion(){
        return AlphaVersion.PLUS_PLUS;
    }

    @Override
    protected ProcessTree2Petrinet.PetrinetWithMarkings TrainPetrinet() throws Exception {
        Iterator<XTrace> iterTraces = _log.iterator();
        // Q2 - PART 2: Create noise filtering
        double valueNoiseFilter     = _threshold;
        double originalLogSize      = _log.size();

        // Get the Traces
        while (iterTraces.hasNext()) {
            Trace t = new Trace(iterTraces.next());
            _traceHelper.Add(t);
        }

        // Create new HashMap with key = trace and value = the percent of the trace from the entire log
        // Find the trace with the minimum percent
        double minPercentTrace = 1;
        Trace minTrace = null;
        for ( Map.Entry<Trace, Integer> trace : _traceHelper.Traces.entrySet()) {
            double percentOfTrace = trace.getValue() / originalLogSize;
            percentOfTraces.putIfAbsent(trace.getKey(),percentOfTrace);
            if (percentOfTrace < minPercentTrace){
                minPercentTrace = percentOfTrace;
                minTrace = trace.getKey();
            }
        }

        while (valueNoiseFilter > 0) {
            valueNoiseFilter = valueNoiseFilter - minPercentTrace;
            // remove the trace from the log
            for ( XTrace itemXtrace : _traceHelper.ListOfXTraces.get(minTrace) ){
                _log.remove(itemXtrace);
            }
            _traceHelper.Traces.remove(minTrace);
            percentOfTraces.remove(minTrace);

            minPercentTrace = 1;
            for (Map.Entry<Trace, Double> trace : percentOfTraces.entrySet()) {
                double valueOfPercent = trace.getValue();
                if (valueOfPercent < minPercentTrace) {
                    minPercentTrace = valueOfPercent;
                    minTrace = trace.getKey();
                }
            }
        }

        Pair<Petrinet, Marking> runResult = Mine(_log);

        ProcessTree2Petrinet.PetrinetWithMarkings pn = new ProcessTree2Petrinet.PetrinetWithMarkings();
        pn.petrinet = runResult.getFirst();
        pn.initialMarking = runResult.getSecond();

        return pn;
    }
}
