package org.eduprom.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.deckfour.xes.model.XTrace;
import org.eduprom.Entities.Trace;
import org.processmining.framework.util.Pair;

public class TraceHelper  {
		
	public HashMap<Trace, Integer> Traces;
	// Add hash map for saving all the XTraces with equal trace
	public HashMap<Trace, ArrayList<XTrace>> ListOfXTraces = new HashMap<Trace, ArrayList<XTrace>>();
	
	public TraceHelper(){
		Traces = new HashMap<Trace, Integer>();		
	}
	
	/**
	 * Saves the current trace and the number of cumulative occurrences ().
	 * @param t A trace to add. 
	 */
	synchronized public void Add(Trace t) throws Exception{
		if (Traces.containsKey(t)){
			Integer value = Traces.get(t);
			Traces.replace(t, value + 1);
			ListOfXTraces.get(t).add(t.getXTrace());
		}
		else {
			Integer value = Traces.putIfAbsent(t, 1);
			ListOfXTraces.putIfAbsent(t, new ArrayList<XTrace>());
			ListOfXTraces.get(t).add(t.getXTrace());
			if (value != null) {
				value++;
			}
		}
	}
	
	public void Clear(){
		Traces.clear();
	}
	
	
	public boolean Exists(Trace t){
		return Traces.containsKey(t);
	}
	
	public Iterator<Trace> iterator() {
		
        Iterator<Trace> iprof = Traces.keySet().stream()
        		.flatMap(x -> Collections.nCopies(1, x).stream())
        		.iterator();
        		
        return iprof;
    }

}
