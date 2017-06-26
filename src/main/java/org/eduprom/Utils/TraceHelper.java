package org.eduprom.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.deckfour.xes.model.XTrace;
import org.eduprom.Entities.Trace;

public class TraceHelper  {
		
	public HashMap<Trace, Integer> Traces;
	
	public TraceHelper(){
		Traces = new HashMap<Trace, Integer>();		
	}
	public HashMap<Trace, ArrayList<XTrace>> xTracesList = new HashMap<Trace, ArrayList<XTrace>>();

	/**
	 * Saves the current trace and the number of cumulative occurrences ().
	 * @param t A trace to add. 
	 */
	synchronized public void Add(Trace t){
		Integer value = Traces.putIfAbsent(t, 1);
		if(value != null){
			value++;
			Traces.put(t,value);
			//xTracesList.putIfAbsent(t,value);
		}
		else //if it is NULL then we didn't create it yet.
		{
			Traces.put()
			xTracesList.putIfAbsent(t, new ArrayList<XTrace>());
			xTracesList.get(t).add(t.getXTrace());
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
