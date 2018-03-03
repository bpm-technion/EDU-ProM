package org.eduprom.miners.adaptiveNoise.parameters;

import org.eduprom.benchmarks.configuration.Weights;
import org.eduprom.miners.adaptiveNoise.configuration.AdaptiveNoiseConfiguration;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIM;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMi;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIM;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMf.CutFinderIMf;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughIM;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterCombination;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterLoop;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterMaybeInterleaved;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterOr;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterParallel;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterSequenceFiltering;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterXorFiltering;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessorInterleaved;

import java.util.ArrayList;
import java.util.Arrays;

public class MiningParametersNoise extends MiningParameters {

    private final AdaptiveNoiseConfiguration adaptiveNoiseConfiguration;

    public MiningParametersNoise(AdaptiveNoiseConfiguration adaptiveNoiseConfiguration, float noiseThreshold) {
        this.adaptiveNoiseConfiguration = adaptiveNoiseConfiguration;

        setLog2LogInfo(new IMLog2IMLogInfoDefault());

        setBaseCaseFinders(new ArrayList<BaseCaseFinder>(Arrays.asList(
                new BaseCaseFinderIMi(),
                new BaseCaseFinderIM()
        )));

        setCutFinder(new ArrayList<CutFinder>(Arrays.asList(
                new CutFinderIM(),
                new CutFinderIMf()
        )));

        setLogSplitter(new LogSplitterCombination(
                new LogSplitterXorFiltering(),
                new LogSplitterSequenceFiltering(),
                new LogSplitterParallel(),
                new LogSplitterLoop(),
                new LogSplitterMaybeInterleaved(),
                new LogSplitterParallel(),
                new LogSplitterOr()));

        setFallThroughs(new ArrayList<>(Arrays.asList(
                new FallThroughIMadaptiveNoise()
        )));

        setPostProcessors(new ArrayList<PostProcessor>(Arrays.asList(
                new PostProcessorInterleaved()
        )));

        setNoiseThreshold(noiseThreshold);

        getReduceParameters().setReduceToOr(false);
    }

    public Weights getWeights() {
        return adaptiveNoiseConfiguration.getWeights();
    }

    public AdaptiveNoiseConfiguration getAdaptiveNoiseConfiguration() {
        return adaptiveNoiseConfiguration;
    }
}
