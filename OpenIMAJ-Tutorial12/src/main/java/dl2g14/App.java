package dl2g14;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.data.DataSource;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.experiment.dataset.sampling.GroupedUniformRandomisedSampler;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.feature.DiskCachingFeatureExtractor;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.data.LocalFeatureListDataSource;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101.Record;
import org.openimaj.image.feature.dense.gradient.dsift.ByteDSIFTKeypoint;
import org.openimaj.image.feature.dense.gradient.dsift.DenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.PyramidDenseSIFT;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.image.feature.local.aggregate.PyramidSpatialAggregator;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator.Mode;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;
import org.openimaj.ml.kernel.HomogeneousKernelMap;
import org.openimaj.ml.kernel.HomogeneousKernelMap.KernelType;
import org.openimaj.ml.kernel.HomogeneousKernelMap.WindowType;
import org.openimaj.util.pair.IntFloatPair;

import de.bwaldvogel.liblinear.SolverType;

public class App {
    public static void main( String[] args ) throws IOException   {
    	GroupedDataset<String, VFSListDataset<Record<FImage>>, Record<FImage>> allData = 
    			Caltech101.getData(ImageUtilities.FIMAGE_READER);
    	
    	// To save compilation time, we only use a subset of the data
    GroupedDataset<String, ListDataset<Record<FImage>>, Record<FImage>> data = 
    			GroupSampler.sample(allData, 5, false);
    	
    	// We split data into training and testing
    	GroupedRandomSplitter<String, Record<FImage>> splits = 
    			new GroupedRandomSplitter<String, Record<FImage>>(allData, 15, 0, 15);
    	
    	/*
    	 * Exercise 12.1.3 - The whole dataset
    	 * In this exercise, we run the code for the entire Caltech101 dataset.
    	 * To do this, we use our allData variable instead of our data variable, which is only
    	 * a subset of the data.
    	 * We also modify our denseSIFT by reducing the step size of the step-size from 5 to 3 
    	 * and adding extra scales to it. Moreover, the number of visual words was incremented to 600.
    	 * Lastly, we experiment with the PyramidSpatialAggregator instead
    	 * of the BlockSpatialAggregator.
    	 * 
    	 */
    	
    	// Dense SIFT extractor
    	DenseSIFT dsift = new DenseSIFT(3, 7);
    	int[] scales = {4,6,8,10};
    	PyramidDenseSIFT<FImage> pdsift = new PyramidDenseSIFT<FImage>(dsift, 6f, scales);
    	
    	HardAssigner<byte[], float[], IntFloatPair> assigner = trainQuantiser(GroupedUniformRandomisedSampler.sample(splits.getTrainingDataset(), 30), pdsift);
    	// We cache our assigner to the cache
    	IOUtils.writeToFile(assigner, new File("/Users/daniellee/Desktop/cache/assigner.txt"));
    	
    	// Exercise 12.1.1 - Apply a Homogoneous Kernel Map
    	/*
    	 * In this exercise we create a kernel map around our PHOWExtractor and analyse its performance.
    	 * When wrapping the PHOWExtractor with the kernel, we have an increase in performance from 73.3% to 82.7%.
    	 */
    	HomogeneousKernelMap kernel = new HomogeneousKernelMap(KernelType.Chi2, WindowType.Rectangular);
    	assigner = IOUtils.readFromFile(new File("/Users/daniellee/Desktop/cache/assigner.txt"));
    	FeatureExtractor<DoubleFV, Record<FImage>> extractor = kernel.createWrappedExtractor(new PHOWExtractor(pdsift, assigner));
    	
    	// Exercise 12.1.2 - Feature Caching
    	/*
    	 * We cache the feature in a local directory (/Users/daniellee/Desktop/cache).
    	 * We also cache the HardAssigner used to create these features by using the IOutils object.
    	 */
    	DiskCachingFeatureExtractor<DoubleFV, Record<FImage>> cacheExtractor = new DiskCachingFeatureExtractor<DoubleFV, Record<FImage>>(
    			new File("/Users/daniellee/Desktop/cache"), extractor);
    	
    	LiblinearAnnotator<Record<FImage>, String> ann = new LiblinearAnnotator<Record<FImage>, String>(
	            cacheExtractor, Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);
    	ann.train(splits.getTrainingDataset());
    	
    	// Compute classifier accuracy
		ClassificationEvaluator<CMResult<String>, String, Record<FImage>> eval = 
				new ClassificationEvaluator<CMResult<String>, String, Record<FImage>>(
						ann, splits.getTestDataset(), new CMAnalyser<Record<FImage>, String>(CMAnalyser.Strategy.SINGLE));
    				
    	Map<Record<FImage>, ClassificationResult<String>> guesses = eval.evaluate();
    	CMResult<String> result = eval.analyse(guesses);
    	System.out.println(result);
    }
    
    // Methods
    static HardAssigner<byte[], float[], IntFloatPair> trainQuantiser(
            Dataset<Record<FImage>> sample, PyramidDenseSIFT<FImage> pdsift) {
			List<LocalFeatureList<ByteDSIFTKeypoint>> allkeys = new ArrayList<LocalFeatureList<ByteDSIFTKeypoint>>();
			
			for (Record<FImage> rec : sample) {
			    FImage img = rec.getImage();
			
			    pdsift.analyseImage(img);
			    allkeys.add(pdsift.getByteKeypoints(0.005f));
			}
			
			if (allkeys.size() > 10000)
			    allkeys = allkeys.subList(0, 10000);
			
			ByteKMeans km = ByteKMeans.createKDTreeEnsemble(600);
			DataSource<byte[]> datasource = new LocalFeatureListDataSource<ByteDSIFTKeypoint, byte[]>(allkeys);
			ByteCentroidsResult result = km.cluster(datasource);
			
			return result.defaultHardAssigner();
    }
    
    static class PHOWExtractor implements FeatureExtractor<DoubleFV, Record<FImage>> {
        PyramidDenseSIFT<FImage> pdsift;
        HardAssigner<byte[], float[], IntFloatPair> assigner;
        
        
        public PHOWExtractor(PyramidDenseSIFT<FImage> pdsift, HardAssigner<byte[], float[], IntFloatPair> assigner)
        {
            this.pdsift = pdsift;
            this.assigner = assigner;
        }

        public DoubleFV extractFeature(Record<FImage> object) {
            FImage image = object.getImage();
            pdsift.analyseImage(image);

            BagOfVisualWords<byte[]> bovw = new BagOfVisualWords<byte[]>(assigner);
            
            // We try with the Pyramid Spatial Aggregator
            /*BlockSpatialAggregator<byte[], SparseIntFV> spatial = new BlockSpatialAggregator<byte[], SparseIntFV>(
                    bovw, 2, 2);*/
            
            PyramidSpatialAggregator<byte[], SparseIntFV> spatial = new PyramidSpatialAggregator<byte[], SparseIntFV>(
                    bovw, 2, 2);
            
            return spatial.aggregate(pdsift.getByteKeypoints(0.015f), image.getBounds()).normaliseFV();
        }
    }
}

