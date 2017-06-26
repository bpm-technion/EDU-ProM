package org.eduprom.Miners.Alpha;

import org.cpntools.accesscpn.engine.highlevel.instance.Marking;
import org.deckfour.xes.model.XTrace;
import org.eduprom.Entities.Trace;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet;
import org.ujmp.gui.plot.Traces;

import java.util.*;


public class AlphaPlusEnhanced extends AlphaPlus {

    public AlphaPlusEnhanced(String filename) throws Exception {
        super(filename);
        _log= _logHelper.Read(_filename);
    }
    public HashMap<Trace, Double> percentOfTraces = new HashMap<Trace, Double>();
    //Map<XTrace, Double> percentOfTraces = new HashMap<XTrace, Double>();

    @Override
    protected ProcessTree2Petrinet.PetrinetWithMarkings TrainPetrinet() throws Exception {
        logger.info("Started mining a petri nets using alpha plus enhanced miner");
        double DataLogSize = _log.size();
        //System.out.println("Data size is"+ DataLogSize); //debug
        double FilterNoiseOut = (0.1*DataLogSize);
        //System.out.println("FilterNoiseOut is"+ FilterNoiseOut); //debug
        // here we can set the percentage of the noise out

        Iterator<XTrace> Traces_Pointer = _log.iterator();
        while(Traces_Pointer.hasNext()) {
            Trace t = new Trace(Traces_Pointer.next());
            _traceHelper.Add(t);
        }

        double MinValue = DataLogSize;
        Trace MinKey = null;
        //Map.Entry MinTrace = null;
        double sumOfMinValues =0.0;
        System.out.println("sumOfMinValues  is"+ sumOfMinValues); //debug , 0
        System.out.println("smaller than FilterNoiseOut is"+ FilterNoiseOut); //debug, 10000
        while( sumOfMinValues < FilterNoiseOut) {
            // remove these values that appear the less number of times from all hashmaps/lists
            if(MinKey != null) {
                //System.out.println(" percentOfTraces.get(MinKey) " + percentOfTraces.get(MinKey)); //debug
                for (XTrace itemXtrace : _traceHelper.xTracesList.get(MinKey)) {
                    _log.remove(itemXtrace);
                   // _traceHelper.xTracesList.remove(MinKey);
                }
                _log.remove(MinKey);
               // _traceHelper.Traces.remove(MinTrace);
                _traceHelper.Traces.remove(MinKey);
                percentOfTraces.remove(MinKey);
                //System.out.println(" data size is after remove " + (_log.size()) );
            }
            //find min value from current set of traces
            MinValue = DataLogSize;
            for (Map.Entry<Trace, Integer> Trace : _traceHelper.Traces.entrySet()) {
                double value = Trace.getValue();
               // System.out.println(" value is " + value); //debug
                if(value < MinValue) {
                    MinValue = value;
                    MinKey = Trace.getKey();
                    //MinTrace = Trace;
                }
            }
            sumOfMinValues= sumOfMinValues + MinValue;
            //System.out.println(" sumOfMinValues is " + sumOfMinValues);
        }
        //System.out.println(" final percentage is " + (sumOfMinValues/DataLogSize) );
        System.out.println(" new data size is " + (_log.size()) );
        Pair<Petrinet, org.processmining.models.semantics.petrinet.Marking> runResult = Mine(_log);
        ProcessTree2Petrinet.PetrinetWithMarkings pn = new ProcessTree2Petrinet.PetrinetWithMarkings();
        pn.petrinet = runResult.getFirst();
        pn.initialMarking = runResult.getSecond();

        return pn;
    }

    public AlphaVersion GetVersion(){
        return AlphaVersion.PLUS;
    }
}
