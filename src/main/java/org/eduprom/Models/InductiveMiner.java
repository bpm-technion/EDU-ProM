package org.eduprom.Models;

import org.processmining.plugins.InductiveMiner.mining.*;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.processtree.ProcessTree;
import static org.processmining.ptconversions.pn.ProcessTree2Petrinet.PetrinetWithMarkings;

public class InductiveMiner extends AbstractPetrinetModel {
	
	public InductiveMiner(String filename) throws Exception {
		super(filename);
		_parameters = new MiningParametersIM();
	}
	public InductiveMiner(String filename, float threshold) throws Exception {
		super(filename);
		_parameters = new MiningParametersIM();
		_parameters.setNoiseThreshold(threshold);
	}
	private MiningParametersIM _parameters;

	@Override
	protected PetrinetWithMarkings TrainPetrinet() throws Exception {
		logger.info("Started mining a petri nets using inductive miner");

		// Q1: Set the noise threshold for the log
//		_parameters.setNoiseThreshold(0);
//		_parameters.setNoiseThreshold(10);
		Object[] res = IMPetriNet.minePetriNet(_log, _parameters, _canceller);

		PetrinetWithMarkings pn = new PetrinetWithMarkings();
		pn.petrinet = (PetrinetImpl)res[0];
		pn.initialMarking = (Marking)res[1];
		pn.finalMarking = (Marking)res[2];

		return pn;
	}

}