package org.eduprom.partitioning.trunk;

import javafx.util.Pair;
import org.deckfour.xes.model.XLog;
import org.eduprom.benchmarks.configuration.Logs;
import org.eduprom.entities.CrossValidationPartition;
import org.eduprom.exceptions.LogFileNotFoundException;
import org.eduprom.exceptions.MiningException;
import org.eduprom.miners.adaptiveNoise.AdaMinerValidation;
import org.eduprom.miners.adaptiveNoise.IntermediateMiners.NoiseInductiveMiner;
import org.eduprom.miners.adaptiveNoise.configuration.AdaptiveNoiseConfiguration;
import org.eduprom.miners.adaptiveNoise.conformance.ConformanceInfo;
import org.eduprom.miners.adaptiveNoise.parameters.MiningParametersNoise;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.processtree.ProcessTree;

import javax.resource.NotSupportedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdaMinerOldGen extends AdaMinerValidation {


    public AdaMinerOldGen(String filename, AdaptiveNoiseConfiguration adaptiveNoiseConfiguration) throws LogFileNotFoundException {
        super(filename, adaptiveNoiseConfiguration);
    }

    protected static ConformanceInfo getConformanceInfo(ProcessTree tree, XLog trainingLog, XLog validationLog, MinerState minerState) throws MiningException {
        return AdaBenchmark.getPsi(petrinetHelper, tree, trainingLog, ((MiningParametersNoise)minerState.parameters).getWeights());
    }

    @Override
    protected ConformanceInfo getConformanceInfo(ProcessTree tree,  XLog trainingLog, MinerState minerState) throws MiningException {
        return getConformanceInfo(tree,  trainingLog, validationLog, minerState);
    }
}
