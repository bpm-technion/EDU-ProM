package org.eduprom.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.deckfour.xes.model.XTrace;
import org.eduprom.Entities.Trace;
import org.eduprom.Main;
import org.processmining.framework.util.Pair;

public class TraceHelper  {
		
	public HashMap<Trace, Integer> Traces;
	// hash map - saves as key the two final activities in a trace and as
	// value the id's of the traces they was appeared in them at the end
	public HashMap<String, ArrayList<XTrace>> twoFinalActivities;
	// hash map - saves as key the final activity in a trace and as
	// value the id's of the traces he has appeared in them at the end
	public HashMap<String, ArrayList<XTrace>> finalActivities;
	// Add hash map for saving all the XTraces with equal trace
	public HashMap<Trace, ArrayList<XTrace>> ListOfXTraces = new HashMap<Trace, ArrayList<XTrace>>();
	final static Logger logger = Logger.getLogger(Main.class.getName());

	public TraceHelper(){
		Traces     		   = new HashMap<Trace, Integer>();
		finalActivities    = new HashMap<String, ArrayList<XTrace>>();
		twoFinalActivities = new HashMap<String, ArrayList<XTrace>>();
	}
	
	/**
	 * Saves the current trace and the number of cumulative occurrences ().
	 * @param t A trace to add. 
	 */
	synchronized public void Add(Trace t) throws Exception{
		System.out.print(t.Activities.length +" "+t.Activities[t.Activities.length-1]+ "\n");
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
//		//fill finalActivities hash map
//		if (finalActivities.containsKey(t.Activities[t.Activities.length-1])){
//			finalActivities.get(t.Activities[t.Activities.length-1]).add(t.getXTrace());
//		}
//		else {
//			finalActivities.putIfAbsent(t.Activities[t.Activities.length-1], new ArrayList<XTrace>());
//			finalActivities.get(t.Activities[t.Activities.length-1]).add(t.getXTrace());
//		}

		//fill twoFinalActivities hash map
		if (twoFinalActivities.containsKey(t.Activities[t.Activities.length-2] + t.Activities[t.Activities.length-1])){
			twoFinalActivities.get(t.Activities[t.Activities.length-2] + t.Activities[t.Activities.length-1]).add(t.getXTrace());
		}
		else {
			twoFinalActivities.putIfAbsent(t.Activities[t.Activities.length-2] + t.Activities[t.Activities.length-1], new ArrayList<XTrace>());
			twoFinalActivities.get(t.Activities[t.Activities.length-2] + t.Activities[t.Activities.length-1]).add(t.getXTrace());
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
