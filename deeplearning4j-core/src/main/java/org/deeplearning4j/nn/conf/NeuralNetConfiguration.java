package org.deeplearning4j.nn.conf;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.deeplearning4j.models.featuredetectors.rbm.RBM;
import org.deeplearning4j.nn.conf.deserializers.*;
import org.deeplearning4j.nn.conf.serializers.*;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.nn.api.LayerFactory;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.api.StepFunction;
import org.deeplearning4j.optimize.stepfunctions.GradientStepFunction;
import org.nd4j.linalg.api.activation.ActivationFunction;
import org.nd4j.linalg.api.activation.Activations;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * A Serializable configuration
 * for neural nets that covers per layer parameters
 *
 * @author Adam Gibson
 */
public class NeuralNetConfiguration implements Serializable,Cloneable {

    private double sparsity = 0f;
    private boolean useAdaGrad = true;
    private double lr = 1e-1f;
    protected double corruptionLevel = 0.3f;
    protected int numIterations = 1000;
    /* momentum for learning */
    protected double momentum = 0.5f;
    /* L2 Regularization constant */
    protected double l2 = 0f;
    protected boolean useRegularization = false;

    //momentum after n iterations
    protected Map<Integer,Double> momentumAfter = new HashMap<>();
    //reset adagrad historical gradient after n iterations
    protected int resetAdaGradIterations = -1;
    protected double dropOut = 0;
    //use only when binary hidden neuralNets are active
    protected boolean applySparsity = false;
    //weight init scheme, this can either be a distribution or a applyTransformToDestination scheme
    protected WeightInit weightInit = WeightInit.VI;
    protected OptimizationAlgorithm optimizationAlgo = OptimizationAlgorithm.CONJUGATE_GRADIENT;
    protected LossFunctions.LossFunction lossFunction = LossFunctions.LossFunction.RECONSTRUCTION_CROSSENTROPY;
    protected int renderWeightsEveryNumEpochs = -1;
    //whether to concat hidden bias or add it
    protected  boolean concatBiases = false;
    //whether to constrain the gradient to unit norm or not
    protected boolean constrainGradientToUnitNorm = false;
    /* RNG for sampling. */
    protected long seed = 123;
    protected transient RandomGenerator rng;
    //weight initialization
    protected transient RealDistribution dist;
    protected transient Collection<IterationListener> listeners;
    protected transient StepFunction stepFunction = new GradientStepFunction();
    protected transient LayerFactory layerFactory;

    //gradient keys used for ensuring order when getting and setting the gradient
    protected List<String> gradientList = new ArrayList<>();
    //feed forward nets
    protected int nIn,nOut;

    protected ActivationFunction activationFunction;

    //RBMs
    private RBM.VisibleUnit visibleUnit = RBM.VisibleUnit.BINARY;
    private RBM.HiddenUnit hiddenUnit = RBM.HiddenUnit.BINARY;
    protected int k = 1;

    private int[] weightShape;

    //convolutional nets
    private int[] filterSize = {2,2};
    private int numFeatureMaps = 2;
    private int[] featureMapSize = {2,2};
    //aka pool size
    private int[] stride = {2,2};
    //kernel size for a convolutional net
    protected int kernel = 5;
    //batch size: primarily used for conv nets. Will be reinforced if set.
    protected int batchSize = 10;



    public NeuralNetConfiguration() {}





    public NeuralNetConfiguration(double sparsity,
                                  boolean useAdaGrad,
                                  double lr,
                                  int k,
                                  double corruptionLevel,
                                  int numIterations,
                                  double momentum,
                                  double l2,
                                  boolean useRegularization,
                                  Map<Integer, Double> momentumAfter,
                                  int resetAdaGradIterations,
                                  double dropOut,
                                  boolean applySparsity,
                                  WeightInit weightInit,
                                  OptimizationAlgorithm optimizationAlgo,
                                  LossFunctions.LossFunction lossFunction,
                                  int renderWeightsEveryNumEpochs,
                                  boolean concatBiases,
                                  boolean constrainGradientToUnitNorm,
                                  RandomGenerator rng,
                                  RealDistribution dist,
                                  long seed,
                                  int nIn,
                                  int nOut,
                                  ActivationFunction activationFunction,
                                  RBM.VisibleUnit visibleUnit,
                                  RBM.HiddenUnit hiddenUnit,
                                  int[] weightShape,
                                  int[] filterSize,
                                  int[] stride,
                                  int[] featureMapSize,
                                  int kernel,
                                  int batchSize,
                                  Collection<IterationListener> listeners,
                                  LayerFactory layerFactory) {

        this.batchSize = batchSize;
        this.layerFactory = layerFactory;
        this.listeners = listeners;
        this.sparsity = sparsity;
        this.useAdaGrad = useAdaGrad;
        this.lr = lr;
        this.kernel = kernel;
        this.k = k;
        this.corruptionLevel = corruptionLevel;
        this.numIterations = numIterations;
        this.momentum = momentum;
        this.l2 = l2;
        this.useRegularization = useRegularization;
        this.momentumAfter = momentumAfter;
        this.resetAdaGradIterations = resetAdaGradIterations;
        this.dropOut = dropOut;
        this.applySparsity = applySparsity;
        this.weightInit = weightInit;
        this.optimizationAlgo = optimizationAlgo;
        this.lossFunction = lossFunction;
        this.renderWeightsEveryNumEpochs = renderWeightsEveryNumEpochs;
        this.concatBiases = concatBiases;
        this.constrainGradientToUnitNorm = constrainGradientToUnitNorm;
        this.rng = rng;
        this.dist = dist;
        this.seed = seed;
        this.nIn = nIn;
        this.nOut = nOut;
        this.activationFunction = activationFunction;
        this.visibleUnit = visibleUnit;
        this.hiddenUnit = hiddenUnit;
        if(weightShape != null)
            this.weightShape = weightShape;
        else
            this.weightShape = new int[]{nIn,nOut};
        this.filterSize = filterSize;
        this.stride = stride;
        this.featureMapSize = featureMapSize;

    }

    public NeuralNetConfiguration(NeuralNetConfiguration neuralNetConfiguration) {
        this.layerFactory = neuralNetConfiguration.layerFactory;
        this.batchSize = neuralNetConfiguration.batchSize;
        this.sparsity = neuralNetConfiguration.sparsity;
        this.useAdaGrad = neuralNetConfiguration.useAdaGrad;
        this.lr = neuralNetConfiguration.lr;
        this.momentum = neuralNetConfiguration.momentum;
        this.l2 = neuralNetConfiguration.l2;
        this.numIterations = neuralNetConfiguration.numIterations;
        this.k = neuralNetConfiguration.k;
        this.corruptionLevel = neuralNetConfiguration.corruptionLevel;
        this.visibleUnit = neuralNetConfiguration.visibleUnit;
        this.hiddenUnit = neuralNetConfiguration.hiddenUnit;
        this.useRegularization = neuralNetConfiguration.useRegularization;
        this.momentumAfter = neuralNetConfiguration.momentumAfter;
        this.resetAdaGradIterations = neuralNetConfiguration.resetAdaGradIterations;
        this.dropOut = neuralNetConfiguration.dropOut;
        this.applySparsity = neuralNetConfiguration.applySparsity;
        this.weightInit = neuralNetConfiguration.weightInit;
        this.optimizationAlgo = neuralNetConfiguration.optimizationAlgo;
        this.lossFunction = neuralNetConfiguration.lossFunction;
        this.renderWeightsEveryNumEpochs = neuralNetConfiguration.renderWeightsEveryNumEpochs;
        this.concatBiases = neuralNetConfiguration.concatBiases;
        this.constrainGradientToUnitNorm = neuralNetConfiguration.constrainGradientToUnitNorm;
        this.rng = neuralNetConfiguration.rng;
        this.dist = neuralNetConfiguration.dist;
        this.seed = neuralNetConfiguration.seed;
        this.nIn = neuralNetConfiguration.nIn;
        this.nOut = neuralNetConfiguration.nOut;
        this.activationFunction = neuralNetConfiguration.activationFunction;
        this.visibleUnit = neuralNetConfiguration.visibleUnit;
        this.weightShape = neuralNetConfiguration.weightShape;
        this.stride = neuralNetConfiguration.stride;
        this.numFeatureMaps = neuralNetConfiguration.numFeatureMaps;
        this.filterSize = neuralNetConfiguration.filterSize;
        this.featureMapSize = neuralNetConfiguration.featureMapSize;
        if(dist == null)
            this.dist = new NormalDistribution(rng,0,.01,NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

        this.hiddenUnit = neuralNetConfiguration.hiddenUnit;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getKernel() {
        return kernel;
    }

    public void setKernel(int kernel) {
        this.kernel = kernel;
    }

    public LayerFactory getLayerFactory() {
        return layerFactory;
    }

    public void setLayerFactory(LayerFactory layerFactory) {
        this.layerFactory = layerFactory;
    }

    public void addVariable(String variable) {
        if(!gradientList.contains(variable))
            gradientList.add(variable);
    }

    public List<String> getGradientList() {
        return gradientList;
    }

    public void setGradientList(List<String> gradientList) {
        this.gradientList = gradientList;
    }

    public StepFunction getStepFunction() {
        return stepFunction;
    }

    public void setStepFunction(StepFunction stepFunction) {
        this.stepFunction = stepFunction;
    }



    public int[] getFeatureMapSize() {
        return featureMapSize;
    }

    public void setFeatureMapSize(int[] featureMapSize) {
        this.featureMapSize = featureMapSize;
    }

    public int[] getWeightShape() {
        return weightShape;
    }

    public void setWeightShape(int[] weightShape) {
        this.weightShape = weightShape;
    }

    public int getNumIterations() {
        return numIterations;
    }

    public void setNumIterations(int numIterations) {
        this.numIterations = numIterations;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public double getCorruptionLevel() {
        return corruptionLevel;
    }


    public RBM.HiddenUnit getHiddenUnit() {
        return hiddenUnit;
    }

    public void setHiddenUnit(RBM.HiddenUnit hiddenUnit) {
        this.hiddenUnit = hiddenUnit;
    }

    public RBM.VisibleUnit getVisibleUnit() {
        return visibleUnit;
    }

    public void setVisibleUnit(RBM.VisibleUnit visibleUnit) {
        this.visibleUnit = visibleUnit;
    }


    public LossFunctions.LossFunction getLossFunction() {
        return lossFunction;
    }

    public void setLossFunction(LossFunctions.LossFunction lossFunction) {
        this.lossFunction = lossFunction;
    }

    public ActivationFunction getActivationFunction() {
        return activationFunction;
    }

    public void setActivationFunction(ActivationFunction activationFunction) {
        this.activationFunction = activationFunction;
    }

    public int getnIn() {
        return nIn;
    }

    public void setnIn(int nIn) {
        this.nIn = nIn;
    }

    public int getnOut() {
        return nOut;
    }

    public void setnOut(int nOut) {
        this.nOut = nOut;
    }

    public double getSparsity() {
        return sparsity;
    }


    public boolean isUseAdaGrad() {
        return useAdaGrad;
    }

    public void setUseAdaGrad(boolean useAdaGrad) {
        this.useAdaGrad = useAdaGrad;
    }

    public double getLr() {
        return lr;
    }

    public void setLr(double lr) {
        this.lr = lr;
    }

    public double getMomentum() {
        return momentum;
    }


    public double getL2() {
        return l2;
    }

    public void setL2(double l2) {
        this.l2 = l2;
    }

    public boolean isUseRegularization() {
        return useRegularization;
    }

    public void setUseRegularization(boolean useRegularization) {
        this.useRegularization = useRegularization;
    }

    public Map<Integer, Double> getMomentumAfter() {
        return momentumAfter;
    }

    public void setMomentumAfter(Map<Integer, Double> momentumAfter) {
        this.momentumAfter = momentumAfter;
    }

    public int getResetAdaGradIterations() {
        return resetAdaGradIterations;
    }

    public void setResetAdaGradIterations(int resetAdaGradIterations) {
        this.resetAdaGradIterations = resetAdaGradIterations;
    }

    public double getDropOut() {
        return dropOut;
    }


    public boolean isApplySparsity() {
        return applySparsity;
    }

    public void setApplySparsity(boolean applySparsity) {
        this.applySparsity = applySparsity;
    }

    public WeightInit getWeightInit() {
        return weightInit;
    }

    public void setWeightInit(WeightInit weightInit) {
        this.weightInit = weightInit;
    }

    public OptimizationAlgorithm getOptimizationAlgo() {
        return optimizationAlgo;
    }

    public void setOptimizationAlgo(OptimizationAlgorithm optimizationAlgo) {
        this.optimizationAlgo = optimizationAlgo;
    }

    public int getRenderWeightIterations() {
        return renderWeightsEveryNumEpochs;
    }

    public void setRenderWeightIterations(int renderWeightsEveryNumEpochs) {
        this.renderWeightsEveryNumEpochs = renderWeightsEveryNumEpochs;
    }

    public boolean isConcatBiases() {
        return concatBiases;
    }

    public void setConcatBiases(boolean concatBiases) {
        this.concatBiases = concatBiases;
    }

    public boolean isConstrainGradientToUnitNorm() {
        return constrainGradientToUnitNorm;
    }

    public void setConstrainGradientToUnitNorm(boolean constrainGradientToUnitNorm) {
        this.constrainGradientToUnitNorm = constrainGradientToUnitNorm;
    }

    public RandomGenerator getRng() {
        return rng;
    }

    public void setRng(RandomGenerator rng) {
        this.rng = rng;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public RealDistribution getDist() {
        return dist;
    }

    public void setDist(RealDistribution dist) {
        this.dist = dist;
    }

    public int[] getFilterSize() {
        return filterSize;
    }

    public void setFilterSize(int[] filterSize) {
        this.filterSize = filterSize;
    }

    public int getNumFeatureMaps() {
        return numFeatureMaps;
    }

    public void setNumFeatureMaps(int numFeatureMaps) {
        this.numFeatureMaps = numFeatureMaps;
    }

    public int[] getStride() {
        return stride;
    }

    public void setStride(int[] stride) {
        this.stride = stride;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NeuralNetConfiguration)) return false;

        NeuralNetConfiguration that = (NeuralNetConfiguration) o;

        if (applySparsity != that.applySparsity) return false;
        if (batchSize != that.batchSize) return false;
        if (concatBiases != that.concatBiases) return false;
        if (constrainGradientToUnitNorm != that.constrainGradientToUnitNorm) return false;
        if (Double.compare(that.corruptionLevel, corruptionLevel) != 0) return false;
        if (Double.compare(that.dropOut, dropOut) != 0) return false;
        if (k != that.k) return false;
        if (kernel != that.kernel) return false;
        if (Double.compare(that.l2, l2) != 0) return false;
        if (Double.compare(that.lr, lr) != 0) return false;
        if (Double.compare(that.momentum, momentum) != 0) return false;
        if (nIn != that.nIn) return false;
        if (nOut != that.nOut) return false;
        if (numFeatureMaps != that.numFeatureMaps) return false;
        if (numIterations != that.numIterations) return false;
        if (renderWeightsEveryNumEpochs != that.renderWeightsEveryNumEpochs) return false;
        if (resetAdaGradIterations != that.resetAdaGradIterations) return false;
        if (seed != that.seed) return false;
        if (Double.compare(that.sparsity, sparsity) != 0) return false;
        if (useAdaGrad != that.useAdaGrad) return false;
        if (useRegularization != that.useRegularization) return false;
        if (activationFunction != null ? !activationFunction.equals(that.activationFunction) : that.activationFunction != null)
            return false;
        if (!Arrays.equals(featureMapSize, that.featureMapSize)) return false;
        if (!Arrays.equals(filterSize, that.filterSize)) return false;
        if (gradientList != null ? !gradientList.equals(that.gradientList) : that.gradientList != null) return false;
        if (hiddenUnit != that.hiddenUnit) return false;
        if (layerFactory != null ? !layerFactory.equals(that.layerFactory) : that.layerFactory != null) return false;
        if (lossFunction != that.lossFunction) return false;
        if (momentumAfter != null ? !momentumAfter.equals(that.momentumAfter) : that.momentumAfter != null)
            return false;
        if (optimizationAlgo != that.optimizationAlgo) return false;
        if (!Arrays.equals(stride, that.stride)) return false;
        if (visibleUnit != that.visibleUnit) return false;
        if (weightInit != that.weightInit) return false;
        if (!Arrays.equals(weightShape, that.weightShape)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(sparsity);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (useAdaGrad ? 1 : 0);
        temp = Double.doubleToLongBits(lr);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(corruptionLevel);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + numIterations;
        temp = Double.doubleToLongBits(momentum);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(l2);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (useRegularization ? 1 : 0);
        result = 31 * result + (momentumAfter != null ? momentumAfter.hashCode() : 0);
        result = 31 * result + resetAdaGradIterations;
        temp = Double.doubleToLongBits(dropOut);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (applySparsity ? 1 : 0);
        result = 31 * result + (weightInit != null ? weightInit.hashCode() : 0);
        result = 31 * result + (optimizationAlgo != null ? optimizationAlgo.hashCode() : 0);
        result = 31 * result + (lossFunction != null ? lossFunction.hashCode() : 0);
        result = 31 * result + renderWeightsEveryNumEpochs;
        result = 31 * result + (concatBiases ? 1 : 0);
        result = 31 * result + (constrainGradientToUnitNorm ? 1 : 0);
        result = 31 * result + (int) (seed ^ (seed >>> 32));
        result = 31 * result + (layerFactory != null ? layerFactory.hashCode() : 0);
        result = 31 * result + (gradientList != null ? gradientList.hashCode() : 0);
        result = 31 * result + nIn;
        result = 31 * result + nOut;
        result = 31 * result + (activationFunction != null ? activationFunction.hashCode() : 0);
        result = 31 * result + (visibleUnit != null ? visibleUnit.hashCode() : 0);
        result = 31 * result + (hiddenUnit != null ? hiddenUnit.hashCode() : 0);
        result = 31 * result + k;
        result = 31 * result + (weightShape != null ? Arrays.hashCode(weightShape) : 0);
        result = 31 * result + (filterSize != null ? Arrays.hashCode(filterSize) : 0);
        result = 31 * result + numFeatureMaps;
        result = 31 * result + (featureMapSize != null ? Arrays.hashCode(featureMapSize) : 0);
        result = 31 * result + (stride != null ? Arrays.hashCode(stride) : 0);
        result = 31 * result + kernel;
        result = 31 * result + batchSize;
        return result;
    }

    @Override
    public String toString() {
        return "NeuralNetConfiguration{" +
                "sparsity=" + sparsity +
                ", useAdaGrad=" + useAdaGrad +
                ", lr=" + lr +
                ", k=" + k +
                ", corruptionLevel=" + corruptionLevel +
                ", numIterations=" + numIterations +
                ", momentum=" + momentum +
                ", l2=" + l2 +
                ", useRegularization=" + useRegularization +
                ", momentumAfter=" + momentumAfter +
                ", resetAdaGradIterations=" + resetAdaGradIterations +
                ", dropOut=" + dropOut +
                ", applySparsity=" + applySparsity +
                ", weightInit=" + weightInit +
                ", optimizationAlgo=" + optimizationAlgo +
                ", lossFunction=" + lossFunction +
                ", renderWeightsEveryNumEpochs=" + renderWeightsEveryNumEpochs +
                ", concatBiases=" + concatBiases +
                ", constrainGradientToUnitNorm=" + constrainGradientToUnitNorm +
                ", rng=" + rng +
                ", dist=" + dist +
                ", seed=" + seed +
                ", nIn=" + nIn +
                ", nOut=" + nOut +
                ", activationFunction=" + activationFunction +
                ", visibleUnit=" + visibleUnit +
                ", hiddenUnit=" + hiddenUnit +
                ", weightShape=" + Arrays.toString(weightShape) +
                ", filterSize=" + Arrays.toString(filterSize) +
                ", numFeatureMaps=" + numFeatureMaps +
                ", featureMapSize=" + Arrays.toString(featureMapSize) +
                ", stride=" + Arrays.toString(stride) +
                '}';
    }


    /**
     * Set the configuration for classification
     * @param conf the configuration to set
     */
    public static void setClassifier(NeuralNetConfiguration conf) {
        setClassifier(conf,true);
    }

    /**
     * Set the conf for classification
     * @param conf the configuration to set
     * @param rows whether to use softmax rows or soft max columns
     */
    public static void setClassifier(NeuralNetConfiguration conf,boolean rows) {
        conf.setActivationFunction(rows ? Activations.softMaxRows() : Activations.softmax());
        conf.setLossFunction(LossFunctions.LossFunction.MCXENT);
        conf.setWeightInit(WeightInit.ZERO);
    }

    /**
     * Creates and returns a copy of this object.  The precise meaning
     * of "copy" may depend on the class of the object. The general
     * intent is that, for any object {@code x}, the expression:
     * <blockquote>
     * <pre>
     * x.clone() != x</pre></blockquote>
     * will be true, and that the expression:
     * <blockquote>
     * <pre>
     * x.clone().getClass() == x.getClass()</pre></blockquote>
     * will be {@code true}, but these are not absolute requirements.
     * While it is typically the case that:
     * <blockquote>
     * <pre>
     * x.clone().equals(x)</pre></blockquote>
     * will be {@code true}, this is not an absolute requirement.
     *
     * By convention, the returned object should be obtained by calling
     * {@code super.clone}.  If a class and all of its superclasses (except
     * {@code Object}) obey this convention, it will be the case that
     * {@code x.clone().getClass() == x.getClass()}.
     *
     * By convention, the object returned by this method should be independent
     * of this object (which is being cloned).  To achieve this independence,
     * it may be necessary to modify one or more fields of the object returned
     * by {@code super.clone} before returning it.  Typically, this means
     * copying any mutable objects that comprise the internal "deep structure"
     * of the object being cloned and replacing the references to these
     * objects with references to the copies.  If a class contains only
     * primitive fields or references to immutable objects, then it is usually
     * the case that no fields in the object returned by {@code super.clone}
     * need to be modified.
     *
     * The method {@code clone} for class {@code Object} performs a
     * specific cloning operation. First, if the class of this object does
     * not implement the interface {@code Cloneable}, then a
     * {@code CloneNotSupportedException} is thrown. Note that all arrays
     * are considered to implement the interface {@code Cloneable} and that
     * the return type of the {@code clone} method of an array type {@code T[]}
     * is {@code T[]} where T is any reference or primitive type.
     * Otherwise, this method creates a new instance of the class of this
     * object and initializes all its fields with exactly the contents of
     * the corresponding fields of this object, as if by assignment; the
     * contents of the fields are not themselves cloned. Thus, this method
     * performs a "shallow copy" of this object, not a "deep copy" operation.
     *
     * The class {@code Object} does not itself implement the interface
     * {@code Cloneable}, so calling the {@code clone} method on an object
     * whose class is {@code Object} will result in throwing an
     * exception at run time.
     *
     * @return a clone of this instance.
     * @throws CloneNotSupportedException if the object's class does not
     *                                    support the {@code Cloneable} interface. Subclasses
     *                                    that override the {@code clone} method can also
     *                                    throw this exception to indicate that an instance cannot
     *                                    be cloned.
     * @see Cloneable
     */
    @Override
    public NeuralNetConfiguration clone()  {
        return new NeuralNetConfiguration(this);
    }


    /**
     * Interface for a function to override
     * builder configurations at a particular layer
     *
     */
    public static interface  ConfOverride  {
        void override(int i,Builder builder);

    }

    /**
     * Fluent interface for building a list of configurations
     */
    public static class ListBuilder {
        private List<Builder> layerwise;
        private int[] hiddenLayerSizes;
        private boolean useDropConnect = false;
        private boolean pretrain = true;

        private Map<Integer,OutputPreProcessor> preProcessors = new HashMap<>();

        public ListBuilder(List<Builder> list) {
            this.layerwise = list;
        }


        public ListBuilder preProcessor(Integer layer,OutputPreProcessor preProcessor) {
            preProcessors.put(layer,preProcessor);
            return this;
        }

        public ListBuilder preProcessors(Map<Integer,OutputPreProcessor> preProcessors) {
            this.preProcessors = preProcessors;
            return this;
        }


        public ListBuilder pretrain(boolean pretrain) {
            this.pretrain = pretrain;
            return this;
        }

        public ListBuilder useDropConnect(boolean useDropConnect) {
            this.useDropConnect = useDropConnect;
            return this;
        }


        public ListBuilder override(ConfOverride override) {
            for(int i = 0; i < layerwise.size(); i++)
                override.override(i,layerwise.get(i));
            return this;
        }



        public ListBuilder hiddenLayerSizes(int...hiddenLayerSizes) {
            this.hiddenLayerSizes = hiddenLayerSizes;
            return this;
        }

        public MultiLayerConfiguration build() {
            if(layerwise.size() != hiddenLayerSizes.length + 1)
                throw new IllegalStateException("Number of hidden layers mut be equal to hidden layer sizes + 1");

            List<NeuralNetConfiguration> list = new ArrayList<>();
            for(int i = 0; i < layerwise.size(); i++)
                list.add(layerwise.get(i).build());
            return new MultiLayerConfiguration.Builder()
                    .useDropConnect(useDropConnect).pretrain(pretrain).preProcessors(preProcessors)
                    .hiddenLayerSizes(hiddenLayerSizes)
                    .confs(list).build();
        }

    }


    /**
     * Return this configuration as json
     * @return this configuration represented as json
     */
    public String toJson() {
        ObjectMapper mapper = mapper();

        try {
            String ret =  mapper.writeValueAsString(this);
            return ret
                    .replaceAll("\"activationFunction\",","")
                    .replaceAll("\"rng\",","")
                    .replaceAll("\"dist\",","")
                    .replaceAll("\"layerFactory\",","")
                    .replaceAll("\"stepFunction\",","");

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a neural net configuration from json
     * @param json the neural net configuration from json
     * @return
     */
    public static NeuralNetConfiguration fromJson(String json) {
        ObjectMapper mapper = mapper();
        try {
            //serialize seed rng properly
            NeuralNetConfiguration ret =  mapper.readValue(json, NeuralNetConfiguration.class);
            ret.rng.setSeed(ret.seed);
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSparsity(double sparsity) {
        this.sparsity = sparsity;
    }

    public void setCorruptionLevel(double corruptionLevel) {
        this.corruptionLevel = corruptionLevel;
    }

    public void setMomentum(double momentum) {
        this.momentum = momentum;
    }


    public void setDropOut(double dropOut) {
        this.dropOut = dropOut;
    }

    public Collection<IterationListener> getListeners() {
        if(listeners == null)
            listeners = new ArrayList<>();
        return listeners;
    }

    public void setListeners(Collection<IterationListener> listeners) {
        this.listeners = listeners;
    }

    /**
     * Object mapper for serialization of configurations
     * @return
     */
    public static ObjectMapper mapper() {
        ObjectMapper ret = new ObjectMapper();
        ret.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ret.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LayerFactory.class,new LayerFactorySerializer());
        module.addSerializer(StepFunction.class,new StepFunctionSerializer());
        module.addDeserializer(ActivationFunction.class, new ActivationFunctionDeSerializer());
        module.addSerializer(ActivationFunction.class, new ActivationFunctionSerializer());
        module.addDeserializer(RandomGenerator.class, new RandomGeneratorDeSerializer());
        module.addSerializer(RandomGenerator.class, new RandomGeneratorSerializer());
        module.addSerializer(RealDistribution.class, new DistributionSerializer());
        module.addDeserializer(StepFunction.class, new StepFunctionDeSerializer());
        module.addDeserializer(RealDistribution.class, new DistributionDeSerializer());
        module.addDeserializer(LayerFactory.class,new LayerFactoryDeSerializer());
        ret.registerModule(module);
        return ret;
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        rng = new MersenneTwister(seed);
        dist = new UniformRealDistribution();
    }

    public static class Builder {
        private int k = 1;
        private int kernel = 5;
        private double corruptionLevel = 3e-1f;
        private double sparsity = 0f;
        private boolean useAdaGrad = true;
        private double lr = 1e-1f;
        private double momentum = 0.5f;
        private double l2 = 0f;
        private boolean useRegularization = false;
        private Map<Integer, Double> momentumAfter;
        private int resetAdaGradIterations = -1;
        private double dropOut = 0;
        private boolean applySparsity = false;
        private WeightInit weightInit = WeightInit.VI;
        private OptimizationAlgorithm optimizationAlgo = OptimizationAlgorithm.CONJUGATE_GRADIENT;
        private int renderWeightsEveryNumEpochs = -1;
        private boolean concatBiases = false;
        private boolean constrainGradientToUnitNorm = false;
        private RandomGenerator rng = new MersenneTwister(123);
        private long seed = 123;
        private RealDistribution dist  = new NormalDistribution(rng,0,.01,NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        private boolean adagrad = true;
        private LossFunctions.LossFunction lossFunction = LossFunctions.LossFunction.RECONSTRUCTION_CROSSENTROPY;
        private int nIn;
        private int nOut;
        private ActivationFunction activationFunction = Activations.sigmoid();
        private RBM.VisibleUnit visibleUnit = RBM.VisibleUnit.BINARY;
        private RBM.HiddenUnit hiddenUnit = RBM.HiddenUnit.BINARY;
        private int numIterations = 1000;
        private int[] weightShape;
        private int[] filterSize = {2,2,2,2};
        private int[] featureMapSize = {2,2};
        private int numInFeatureMaps = 2;
        //subsampling layers
        private int[] stride = {2,2};
        private Collection<IterationListener> listeners;
        private StepFunction stepFunction = new GradientStepFunction();
        private LayerFactory layerFactory;
        private int batchSize = 0;

        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder kernel(int kernel) {
            this.kernel = kernel;
            return this;
        }

        public Builder layerFactory(LayerFactory layerFactory) {
            this.layerFactory = layerFactory;
            return this;
        }

        public Builder stepFunction(StepFunction stepFunction) {
            this.stepFunction = stepFunction;
            return this;
        }

        public ListBuilder list(int size) {
            if(size < 2)
                throw new IllegalArgumentException("Number of layers must be > 1");

            List<Builder> list = new ArrayList<>();
            for(int i = 0; i < size; i++)
                list.add(clone());
            return new ListBuilder(list);
        }

        public Builder clone() {
            return new Builder().activationFunction(activationFunction).layerFactory(layerFactory)
                    .adagradResetIterations(resetAdaGradIterations).applySparsity(applySparsity)
                    .concatBiases(concatBiases).constrainGradientToUnitNorm(constrainGradientToUnitNorm)
                    .dist(dist).dropOut(dropOut).featureMapSize(featureMapSize).filterSize(filterSize)
                    .hiddenUnit(hiddenUnit).iterations(numIterations).l2(l2).learningRate(lr).useAdaGrad(adagrad).stepFunction(stepFunction)
                    .lossFunction(lossFunction).momentumAfter(momentumAfter).momentum(momentum).listeners(listeners)
                    .nIn(nIn).nOut(nOut).optimizationAlgo(optimizationAlgo).batchSize(batchSize)
                    .regularization(useRegularization).render(renderWeightsEveryNumEpochs).resetAdaGradIterations(resetAdaGradIterations)
                    .rng(rng).seed(seed).sparsity(sparsity).stride(stride).useAdaGrad(useAdaGrad).visibleUnit(visibleUnit)
                    .weightInit(weightInit).weightShape(weightShape);
        }

        public Builder iterationListener(IterationListener listener) {
            if(listeners != null)
                listeners.add(listener);
            else {
                listeners = new ArrayList<>();
                listeners.add(listener);
            }

            return this;
        }

        public Builder  listeners(Collection<IterationListener> listeners) {
            this.listeners = listeners;
            return this;
        }


        public Builder featureMapSize(int[] featureMapSize) {
            this.featureMapSize = featureMapSize;
            return this;
        }


        public Builder stride(int[] stride) {
            this.stride = stride;
            return this;
        }

        public Builder filterSize(int...filterSize) {
            if(filterSize == null)
                return this;
            if(filterSize.length != 4)
                throw new IllegalArgumentException("Invalid filter size must be length 2");
            this.filterSize = filterSize;
            return this;
        }

        public Builder weightShape(int[] weightShape) {
            this.weightShape = weightShape;
            return this;
        }

        public Builder iterations(int numIterations) {
            this.numIterations = numIterations;
            return this;
        }

        public Builder dist(RealDistribution dist) {
            this.dist = dist;
            return this;
        }

        public Builder sparsity(double sparsity) {
            this.sparsity = sparsity;
            return this;
        }

        public Builder useAdaGrad(boolean useAdaGrad) {
            this.useAdaGrad = useAdaGrad;
            return this;
        }

        public Builder learningRate(double lr) {
            this.lr = lr;
            return this;
        }

        public Builder momentum(double momentum) {
            this.momentum = momentum;
            return this;
        }

        public Builder k(int k) {
            this.k = k;
            return this;
        }

        public Builder corruptionLevel(double corruptionLevel) {
            this.corruptionLevel = corruptionLevel;
            return this;
        }

        public Builder momentumAfter(Map<Integer, Double> momentumAfter) {
            this.momentumAfter = momentumAfter;
            return this;
        }

        public Builder adagradResetIterations(int resetAdaGradIterations) {
            this.resetAdaGradIterations = resetAdaGradIterations;
            return this;
        }

        public Builder dropOut(double dropOut) {
            this.dropOut = dropOut;
            return this;
        }

        public Builder applySparsity(boolean applySparsity) {
            this.applySparsity = applySparsity;
            return this;
        }

        public Builder weightInit(WeightInit weightInit) {
            this.weightInit = weightInit;
            return this;
        }

        public Builder render(int renderWeightsEveryNumEpochs) {
            this.renderWeightsEveryNumEpochs = renderWeightsEveryNumEpochs;
            return this;
        }

        public Builder concatBiases(boolean concatBiases) {
            this.concatBiases = concatBiases;
            return this;
        }

        public Builder rng(RandomGenerator rng) {
            this.rng = rng;
            return this;
        }

        public Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        public NeuralNetConfiguration build() {
            NeuralNetConfiguration ret = new NeuralNetConfiguration( sparsity,  useAdaGrad,  lr,  k,
                    corruptionLevel,  numIterations,  momentum,  l2,  useRegularization, momentumAfter,
                    resetAdaGradIterations,  dropOut,  applySparsity,  weightInit,  optimizationAlgo, lossFunction, renderWeightsEveryNumEpochs,
                    concatBiases,  constrainGradientToUnitNorm,  rng,
                    dist,  seed,  nIn,  nOut,  activationFunction, visibleUnit,hiddenUnit,weightShape,filterSize,stride,featureMapSize,kernel,batchSize,listeners,layerFactory);
            ret.useAdaGrad = this.adagrad;
            ret.stepFunction = stepFunction;
            return ret;
        }

        public Builder l2(double l2) {
            this.l2 = l2;
            return this;
        }

        public Builder regularization(boolean useRegularization) {
            this.useRegularization = useRegularization;
            return this;
        }

        public Builder resetAdaGradIterations(int resetAdaGradIterations) {
            this.resetAdaGradIterations = resetAdaGradIterations;
            return this;
        }

        public Builder optimizationAlgo(OptimizationAlgorithm optimizationAlgo) {
            this.optimizationAlgo = optimizationAlgo;
            return this;
        }

        public Builder lossFunction(LossFunctions.LossFunction lossFunction) {
            this.lossFunction = lossFunction;
            return this;
        }

        public Builder constrainGradientToUnitNorm(boolean constrainGradientToUnitNorm) {
            this.constrainGradientToUnitNorm = constrainGradientToUnitNorm;
            return this;
        }

        public Builder nIn(int nIn) {
            this.nIn = nIn;
            return this;
        }

        public Builder nOut(int nOut) {
            this.nOut = nOut;
            return this;
        }

        public Builder activationFunction(ActivationFunction activationFunction) {
            this.activationFunction = activationFunction;
            return this;
        }

        public Builder visibleUnit(RBM.VisibleUnit visibleUnit) {
            this.visibleUnit = visibleUnit;
            return this;
        }

        public Builder hiddenUnit(RBM.HiddenUnit hiddenUnit) {
            this.hiddenUnit = hiddenUnit;
            return this;
        }
    }
}
