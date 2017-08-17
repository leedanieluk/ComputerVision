package dl2g14;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.model.EigenImages;

public class App {
    public static void main( String[] args ) throws FileSystemException {
    	
    	// Create Group datasset of FImages
    	VFSGroupDataset<FImage> dataset = 
    		    new VFSGroupDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);
    	
    	// Use half of the data to train and the other to test
    	int nTraining = 5;
    	int nTesting = 5;
    	
    	/* Exercise 13.1.2 - Explore the effect of training a set size
    	 * When the training set size was reduced to 1, the accuracy dropped to around 67 percent, as expected,
    	 * due to the fact, that the PCA basis is only modelled from a small fraction of the dataset.
    	 */
    	
    	//int nTraining = 1;
    	//int nTesting = 9;
    	
    	GroupedRandomSplitter<String, FImage> splits = 
    	    new GroupedRandomSplitter<String, FImage>(dataset, nTraining, 0, nTesting);
    	GroupedDataset<String, ListDataset<FImage>, FImage> training = splits.getTrainingDataset();
    	GroupedDataset<String, ListDataset<FImage>, FImage> testing = splits.getTestDataset();
    	
    	// We train our training images
    	List<FImage> basisImages = DatasetAdaptors.asList(training);
    	int nEigenvectors = 100;
    	EigenImages eigen = new EigenImages(nEigenvectors);
    	eigen.train(basisImages);
    	
    	List<FImage> eigenFaces = new ArrayList<FImage>();
    	for (int i = 0; i < 12; i++) {
    	    eigenFaces.add(eigen.visualisePC(i));
    	}
    	DisplayUtilities.display("EigenFaces", eigenFaces);
    	
    	Map<String, DoubleFV[]> features = new HashMap<String, DoubleFV[]>();
    	for (final String person : training.getGroups()) {
    	    final DoubleFV[] fvs = new DoubleFV[nTraining];

    	    for (int i = 0; i < nTraining; i++) {
    	        final FImage face = training.get(person).get(i);
    	        fvs[i] = eigen.extractFeature(face);
    	    }
    	    
    	    features.put(person, fvs);
    	    
    	    // Exercise 13.1.1 - Reconstructing Faces
    	    // We create an FImage array to store the reconstructed images
    	    FImage[] recons = new FImage[fvs.length];
    	    for (int i = 0; i < fvs.length; i++) {
    	    	recons[i] = eigen.reconstruct(fvs[i]);
    	    	recons[i].normalise();
    	    	DisplayUtilities.displayName(recons[i], "Face: " + i);
    	    }
    	}
    	
    	// Accuracy calculation
    	double correct = 0, incorrect = 0;
    	for (String truePerson : testing.getGroups()) {
    	    for (FImage face : testing.get(truePerson)) {
    	        DoubleFV testFeature = eigen.extractFeature(face);

    	        String bestPerson = null;
    	        double minDistance = Double.MAX_VALUE;
    	        for (final String person : features.keySet()) {
    	            for (final DoubleFV fv : features.get(person)) {
    	                double distance = fv.compare(testFeature, DoubleFVComparison.EUCLIDEAN);
    	                System.out.println(distance);
    	                // Exercise 13.1.3 - Apply a threshold
    	                /* 
    	                 * Most of the distances range from 15 to 25, so a threshold of 30
    	                 * would be a reasonable value to use.
    	                 */
    	                if (distance < minDistance) {
    	                    minDistance = distance;
    	                    if(minDistance > 30) {
    	                    	bestPerson = "Unknown";
    	                    } else {
    	                    	bestPerson = person;
    	                    }
    	                }
    	            }
    	        }

    	        System.out.println("Actual: " + truePerson + "\tguess: " + bestPerson);

    	        if (truePerson.equals(bestPerson))
    	            correct++;
    	        else
    	            incorrect++;
    	    }
    	}

    	System.out.println("Accuracy: " + (correct / (correct + incorrect)));
    }
}
