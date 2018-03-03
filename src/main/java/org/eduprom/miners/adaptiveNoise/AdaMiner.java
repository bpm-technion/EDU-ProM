package org.eduprom.miners.adaptiveNoise;

import com.google.common.collect.Lists;
import javafx.util.Pair;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.eduprom.benchmarks.IBenchmarkableMiner;
import org.eduprom.benchmarks.configuration.Logs;
import org.eduprom.benchmarks.configuration.Weights;
import org.eduprom.entities.CrossValidationPartition;
import org.eduprom.exceptions.LogFileNotFoundException;
import org.eduprom.exceptions.MiningException;
import org.eduprom.miners.AbstractMiner;
import org.eduprom.miners.AbstractPetrinetMiner;
import org.eduprom.miners.adaptiveNoise.IntermediateMiners.NoiseInductiveMiner;
import org.eduprom.miners.adaptiveNoise.benchmarks.AdaBenchmarkValidation;
import org.eduprom.miners.adaptiveNoise.configuration.AdaptiveNoiseConfiguration;
import org.eduprom.miners.adaptiveNoise.conformance.ConformanceInfo;
import org.eduprom.miners.adaptiveNoise.parameters.FallThroughIMadaptiveNoise;
import org.eduprom.miners.adaptiveNoise.parameters.MiningParametersNoise;
import org.eduprom.utils.LogHelper;
import org.eduprom.utils.PetrinetHelper;
import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.plugins.InductiveMiner.conversion.ReduceTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.MinerStateBase;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMf;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.interleaved.Interleaved;
import org.processmining.plugins.InductiveMiner.mining.interleaved.MaybeInterleaved;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitter;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogImpl;
import org.processmining.plugins.InductiveMiner.mining.logs.LifeCycles;
import org.processmining.plugins.InductiveMiner.mining.postprocessor.PostProcessor;
import org.processmining.plugins.fuzzymodel.adapter.Conformance;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractTask;
import org.processmining.processtree.impl.ProcessTreeImpl;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.String.format;


public class AdaMiner extends AbstractPetrinetMiner implements IBenchmarkableMiner {

    //region private/protected members
    protected MiningParametersNoise parameters;
    private AdaptiveNoiseConfiguration adaptiveNoiseConfiguration;
    private ProcessTree bestTree;
    private ConformanceInfo conformanceInfo;

    protected static final Logger logger = Logger.getLogger(AbstractMiner.class.getName());
    private static Map<Float, MinerState> parametersIMfMap;
    private static Map<String, ProcessTree> discoveredTrees = new HashMap<>();
    private static CLIPluginContext promContext = new CLIPluginContext(new org.processmining.contexts.cli.CLIContext(), "");
    protected static PetrinetHelper  petrinetHelper =
            new PetrinetHelper(promContext, new XEventNameClassifier());
    protected XLog validationLog;
    //endregion

    //region ctor
    public AdaMiner(String filename, AdaptiveNoiseConfiguration adaptiveNoiseConfiguration) throws LogFileNotFoundException {
        super(filename);
        parametersIMfMap = new HashMap<>();
        discoveredTrees = new HashMap<>();
        this.adaptiveNoiseConfiguration = adaptiveNoiseConfiguration;
        this.parameters = new MiningParametersNoise(adaptiveNoiseConfiguration, 0.0f);
        float[] thresholds = adaptiveNoiseConfiguration.getNoiseThresholds();
        for (float threshold: thresholds) {
            parametersIMfMap.put(threshold, new MinerState(new MiningParametersNoise(adaptiveNoiseConfiguration, threshold)
                    , this.canceller));
        }
    }
    //endregion

    //region discovery and get cut logic

    private ProcessTree discover(XLog xlog) throws MiningException {
        logger.info(String.format("Miners with %d noise thresholds %s are optional",
                adaptiveNoiseConfiguration.getNoiseThresholds().length, parametersIMfMap.keySet().stream()
                        .map(x-> String.valueOf(x.floatValue())).collect(Collectors.joining (","))));
        Weights weights = adaptiveNoiseConfiguration.getWeights();
        logger.info(format("Fitness weight: %f, Precision weight: %f, generalization weight: %f",
                weights.getFitnessWeight(), weights.getPrecisionWeight(), weights.getGeneralizationWeight()));

        IMLog log = new IMLogImpl(xlog, new XEventNameClassifier());
        if (parameters.isRepairLifeCycle()) {
            log = LifeCycles.preProcessLog(log);
        }

        //create process tree
        ProcessTree tree = new ProcessTreeImpl();
        MinerState minerState = new MinerState(parameters, this.canceller);
        Node root = mineNode(log, tree, minerState);

        if (this.canceller.isCancelled()) {
            minerState.shutdownThreadPools();
            return null;
        }

        root.setProcessTree(tree);
        tree.setRoot(root);
        discoveredTrees.putIfAbsent(tree.toString(), tree);


        logger.info(String.format("Found total %d trees", discoveredTrees.size()));


        Map<ProcessTree, ConformanceInfo> treeConformanceInfoEntry = discoveredTrees.entrySet().stream().collect(Collectors.toMap(x -> x.getValue(), x -> {
            try {
                ConformanceInfo info = getConformanceInfo(x.getValue(), xlog, minerState);
                logger.log(Level.FINE, String.format("discovered tree: conformance: %s  details: %s", info, x.getKey()));
                return info;
            } catch (MiningException e) {
                throw new RuntimeException();
            }
        }));

        Map.Entry<ProcessTree, ConformanceInfo> bestModel = treeConformanceInfoEntry.entrySet().stream()
                .max(Comparator.comparing(x->x.getValue().getPsi())).get();
        this.bestTree = bestModel.getKey();
        this.conformanceInfo = bestModel.getValue();
        logger.info(String.format("Best AdA model: conformance %s, tree: %s", bestModel.getValue(), bestModel.getKey().toString()));


        if (this.canceller.isCancelled()) {
            minerState.shutdownThreadPools();
            return null;
        }

        debug("discovered tree " + bestModel.getKey().getRoot(), minerState);

        //reduce the tree
        if (this.parameters.getReduceParameters() != null) {
            try {
                this.bestTree = ReduceTree.reduceTree(this.bestTree, this.parameters.getReduceParameters());
                debug("after reduction " + tree.getRoot(), minerState);
            } catch (UnknownTreeNodeException | EfficientTreeReduce.ReductionFailedException e) {
                e.printStackTrace();
            }
        }

        minerState.shutdownThreadPools();

        if (this.canceller.isCancelled()) {
            return null;
        }

        return this.bestTree;
    }


    private static Pair<Float, MinerState> obtainMinerState(XLog cLog) throws MiningException {
        Map<Float, Pair<MinerState, List<ConformanceInfo>>> values = new HashMap<>();
        parametersIMfMap.entrySet().forEach(x-> values.put(x.getKey(), new Pair<>(x.getValue(), new ArrayList<>())));

        final int partitionSize = getPartitionSize(cLog);

        boolean append = discoveredTrees.isEmpty();
        Map<String, Pair<ProcessTree, ConformanceInfo>> found = new HashMap<>();
        Weights weights = ((MiningParametersNoise) parametersIMfMap.entrySet().stream().findAny().get().getValue().parameters)
                .getAdaptiveNoiseConfiguration().getWeights();
        ConformanceInfo lowerBoundConformance = new ConformanceInfo(weights);
        lowerBoundConformance.setFitness(0.0);
        lowerBoundConformance.setPrecision(0.0);

        List<Map.Entry<Float, MinerState>> items =  parametersIMfMap.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .collect(Collectors.toList());

        for(Map.Entry<Float, MinerState> mfEntry:  items){
            try {
                NoiseInductiveMiner miner = new NoiseInductiveMiner(String.format(Logs.CONTEST_2017, 1),
                        mfEntry.getKey(), ((MiningParametersNoise) mfEntry.getValue().parameters).getAdaptiveNoiseConfiguration().isPreExecuteFilter());
                ProcessTree t = miner.mineProcessTree(cLog).getProcessTree();
                discoveredTrees.putIfAbsent(t.toString(), t);

                Collections.shuffle(cLog);
                List<CrossValidationPartition> origin = Lists.partition(cLog, partitionSize)
                        .stream().map(x -> new CrossValidationPartition(x, cLog.getAttributes())).collect(Collectors.toList());

                for (CrossValidationPartition validationPartitions : origin) {
                    XLog validationLog = CrossValidationPartition.bind(validationPartitions).getLog();
                    List<CrossValidationPartition> withoutValidation = CrossValidationPartition.exclude(origin, validationPartitions);
                    XLog trainingLog = partitionSize > 1 ? CrossValidationPartition.bind(withoutValidation).getLog() : validationLog;

                    ProcessTree subLogTree = miner.mineProcessTree(trainingLog).getProcessTree();
                    if (found.containsKey(subLogTree.toString())){
                        continue;
                    }

                    ConformanceInfo conformanceInfo = getConformanceInfo(subLogTree, trainingLog, validationLog, mfEntry.getValue());
                    if (getLowerBound(lowerBoundConformance) > getUpperBound(conformanceInfo)) {
                        continue;
                    }
                    else if (getLowerBound(conformanceInfo) > getLowerBound(lowerBoundConformance)) {
                        lowerBoundConformance  = conformanceInfo;
                    }

                    found.putIfAbsent(subLogTree.toString(), new Pair<>(subLogTree, conformanceInfo));


                    //logger.info(String.format("finished evaluating conformance for noise threshold: %f", mfEntry.getKey()));
                    logger.log(Level.FINE, String.format("%f threshold, conformance info: %s, tree: %s", mfEntry.getKey(),
                            conformanceInfo, subLogTree.toString()));

                    values.get(mfEntry.getKey()).getValue().add(conformanceInfo);

                    if (!append && values.get(mfEntry.getKey()).getValue().size() > 0){
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (append) {
            final double lowerBound = getLowerBound(lowerBoundConformance);
            found.entrySet().stream()
                    .filter(x -> getUpperBound(x.getValue().getValue()) > lowerBound)
                    .forEach(x -> discoveredTrees.putIfAbsent(x.getKey(), x.getValue().getKey()));
            found.entrySet().stream()
                    .filter(x -> getUpperBound(x.getValue().getValue()) < lowerBound)
                    .forEach(x-> discoveredTrees.remove(x.getKey()));
        }

        return values.entrySet().stream()
                .max(Comparator.comparing(x ->
                        x.getValue().getValue().stream().mapToDouble(y->y.getPsi()).average().orElse(0)))
                .map(x-> new Pair<Float, MinerState>(x.getKey(), x.getValue().getKey())).get();
    }

    //endregion

    //region private helpers

    private ConformanceInfo getConformanceInfo(ProcessTree tree,  XLog trainingLog, MinerState minerState) throws MiningException {
        return getConformanceInfo(tree,  trainingLog, validationLog, minerState);
    }

    private static ConformanceInfo getConformanceInfo(ProcessTree tree,  XLog trainingLog,XLog validationLog, MinerState minerState) throws MiningException {
        return AdaBenchmarkValidation.getPsi(petrinetHelper, tree, trainingLog, validationLog,
                ((MiningParametersNoise)minerState.parameters).getWeights());
    }

    private static double getUpperBound(ConformanceInfo conformanceInfo){
        return conformanceInfo.getFitnessWeight() * conformanceInfo.getFitness() +
                conformanceInfo.getPrecisionWeight() *conformanceInfo.getPrecision() +
                conformanceInfo.getGeneralizationWeight() * 1.0;
    }

    private static double getLowerBound(ConformanceInfo conformanceInfo){
        return conformanceInfo.getFitnessWeight() * conformanceInfo.getFitness() +
                conformanceInfo.getPrecisionWeight() *conformanceInfo.getPrecision() +
                conformanceInfo.getGeneralizationWeight() * 0.0;
    }

    private static int getPartitionSize(XLog cLog) {
        //if log size is too small (possible since we are partitioning the log), take partition size to be 1.
        return (int)Math.round(cLog.size() / 10.0) == 0 ? 1 : (int)Math.round(cLog.size() / 10.0);
    }

    //endregion

    //region IM static methods

    public static Node mineNode(IMLog log, ProcessTree tree, MinerState minerState) throws MiningException {
        //construct basic information about log
        IMLogInfo logInfo = minerState.parameters.getLog2LogInfo().createLogInfo(log);

        //output information about the log
        debug("\nmineProcessTree epsilon=" + logInfo.getDfg().getNumberOfEmptyTraces() + ", " + logInfo.getActivities(),
                minerState);
        //debug(log, minerState);

        //find base cases
        Node baseCase = findBaseCases(log, logInfo, tree, minerState);
        if (baseCase != null) {

            baseCase = postProcess(baseCase, log, logInfo, minerState);

            debug(" discovered node " + baseCase, minerState);
            return baseCase;
        }

        if (minerState.isCancelled()) {
            return null;
        }
        XLog cLog = log.toXLog();

        logger.log(Level.FINE,"started evaluating miners");

        Map.Entry<Float, Pair<MinerState, ConformanceInfo>> best = null;
        Cut bestCut = null;
        IMLog cIMLog = null;
        IMLogInfo cLogInfo = null;


        Pair<Float, MinerState> entry = obtainMinerState(cLog);

        MinerState cMinerState = entry.getValue();
        cIMLog = log;
        cLogInfo = logInfo;
        if (((MiningParametersNoise)entry.getValue().parameters).getAdaptiveNoiseConfiguration().isPreExecuteFilter()){
            cLog = NoiseInductiveMiner.filterLog(cLog, entry.getKey(), promContext).getFilteredLog();
            cMinerState = parametersIMfMap.get(0.0f);
            cIMLog = new IMLogImpl(cLog, new XEventNameClassifier());
            cLogInfo = minerState.parameters.getLog2LogInfo().createLogInfo(cIMLog);
        }

        Cut cut = findCut(cIMLog, cLogInfo, cMinerState);
        logger.log(Level.FINE, String.format("Chosen cut of %f noise threshold", entry.getKey()));
        return handleCut(cMinerState, cut, cLogInfo, cIMLog, tree);
    }

    private static Node handleCut(MinerState minerState, Cut cut, IMLogInfo logInfo, IMLog log, ProcessTree tree) throws MiningException {
        if (minerState.isCancelled()) {
            return null;
        }

        if (cut != null && cut.isValid()) {
            //cut is valid

            debug(" chosen cut: " + cut, minerState);

            //split logs
            LogSplitter.LogSplitResult splitResult = splitLog(log, logInfo, cut, minerState);

            if (minerState.isCancelled()) {
                return null;
            }

            //make node
            Block newNode;
            try {
                newNode = newNode(cut.getOperator());
            } catch (UnknownTreeNodeException e) {
                e.printStackTrace();
                return null;
            }
            addNode(tree, newNode);

            //recurse
            if (cut.getOperator() != Cut.Operator.loop) {
                for (IMLog sublog : splitResult.sublogs) {
                    Node child = mineNode(sublog, tree, minerState);

                    if (minerState.isCancelled()) {
                        return null;
                    }

                    addChild(newNode, child, minerState);
                }
            } else {
                //loop needs special treatment:
                //ProcessTree requires a ternary loop
                Iterator<IMLog> it = splitResult.sublogs.iterator();

                //mine body
                IMLog firstSublog = it.next();
                {
                    Node firstChild = mineNode(firstSublog, tree, minerState);

                    if (minerState.isCancelled()) {
                        return null;
                    }

                    addChild(newNode, firstChild, minerState);
                }

                //mine redo parts by, if necessary, putting them under an xor
                Block redoXor;
                if (splitResult.sublogs.size() > 2) {
                    redoXor = new AbstractBlock.Xor("");
                    addNode(tree, redoXor);
                    addChild(newNode, redoXor, minerState);
                } else {
                    redoXor = newNode;
                }
                while (it.hasNext()) {
                    IMLog sublog = it.next();
                    Node child = mineNode(sublog, tree, minerState);

                    if (minerState.isCancelled()) {
                        return null;
                    }

                    addChild(redoXor, child, minerState);
                }

                //add tau as third child
                {
                    Node tau = new AbstractTask.Automatic("tau");
                    addNode(tree, tau);
                    addChild(newNode, tau, minerState);
                }
            }

            Node result = postProcess(newNode, log, logInfo, minerState);

            debug(" discovered node " + result, minerState);
            return result;

        } else {
            //cut is not valid; fall through
            Node result = findFallThrough(log, logInfo, tree, minerState);

            result = postProcess(result, log, logInfo, minerState);

            debug(" discovered node " + result, minerState);
            return result;
        }
    }

    private static Node postProcess(Node newNode, IMLog log, IMLogInfo logInfo, MinerState minerState) {
        for (PostProcessor processor : minerState.parameters.getPostProcessors()) {
            newNode = processor.postProcess(newNode, log, logInfo, minerState);
        }

        return newNode;
    }

    private static Block newNode(Cut.Operator operator) throws UnknownTreeNodeException {
        switch (operator) {
            case loop :
                return new AbstractBlock.XorLoop("");
            case concurrent :
                return new AbstractBlock.And("");
            case sequence :
                return new AbstractBlock.Seq("");
            case xor :
                return new AbstractBlock.Xor("");
            case maybeInterleaved :
                return new MaybeInterleaved("");
            case interleaved :
                return new Interleaved("");
            case or :
                return new AbstractBlock.Or("");
        }
        throw new UnknownTreeNodeException();
    }

    /**
     *
     * @param tree
     * @param node
     *            The log used as input for the mining algorithm. Provide null
     *            if this node was not directly derived from a log (e.g. it is a
     *            child in a flower-loop).
     */
    public static void addNode(ProcessTree tree, Node node) {
        node.setProcessTree(tree);
        tree.addNode(node);
    }

    public static Node findBaseCases(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {
        Node n = null;
        Iterator<BaseCaseFinder> it = minerState.parameters.getBaseCaseFinders().iterator();
        while (n == null && it.hasNext()) {

            if (minerState.isCancelled()) {
                return null;
            }

            n = it.next().findBaseCases(log, logInfo, tree, minerState);
        }
        return n;
    }

    public static Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
        Cut c = null;
        Iterator<CutFinder> it = minerState.parameters.getCutFinders().iterator();
        while (it.hasNext() && (c == null || !c.isValid())) {

            if (minerState.isCancelled()) {
                return null;
            }

            c = it.next().findCut(log, logInfo, minerState);
        }
        return c;
    }

    public static Node findFallThrough(IMLog log, IMLogInfo logInfo, ProcessTree tree, MinerState minerState) {

        Node n = null;
        Iterator<FallThrough> it = minerState.parameters.getFallThroughs().iterator();
        while (n == null && it.hasNext()) {

            if (minerState.isCancelled()) {
                return null;
            }

            n = it.next().fallThrough(log, logInfo, tree, minerState);
        }
        logger.log(Level.FINE, String.format("Fall Through: %s", n));
        return n;
    }

    public static LogSplitter.LogSplitResult splitLog(IMLog log, IMLogInfo logInfo, Cut cut, MinerState minerState) {
        LogSplitter.LogSplitResult result = minerState.parameters.getLogSplitter().split(log, logInfo, cut, minerState);

        if (minerState.isCancelled()) {
            return null;
        }

        //merge the discarded events of this log splitting into the global discarded events list
        minerState.discardedEvents.addAll(result.discardedEvents);

        return result;
    }

    public static void debug(Object x, MinerState minerState) {
        if (minerState.parameters.isDebug()) {
            System.out.println(x.toString());
        }
    }

    public static void addChild(Block parent, Node child, MinerStateBase minerState) {
        if (!minerState.isCancelled() && parent != null && child != null) {
            parent.addChild(child);
        }
    }

    @Override
    protected ProcessTree2Petrinet.PetrinetWithMarkings minePetrinet() throws MiningException {
        return PetrinetHelper.ConvertToPetrinet(discover(this.log));
    }

    //endregion

    //region public methods

    @Override
    public ConformanceInfo getConformanceInfo() {
        return this.conformanceInfo;
    }

    @Override
    public void setConformanceInfo(ConformanceInfo conformanceInfo) {
        this.conformanceInfo = conformanceInfo;
    }

    @Override
    public ProcessTree2Petrinet.PetrinetWithMarkings getModel() {
        return this.getDiscoveredPetriNet();
    }

    @Override
    public PetrinetHelper getHelper() {
        return this.petrinetHelper;
    }

    @Override
    public ProcessTree getProcessTree() {
        return this.bestTree;
    }

    public void setValidationLog(XLog validationLog) {
        this.validationLog = validationLog;
    }

    //endregion
}
