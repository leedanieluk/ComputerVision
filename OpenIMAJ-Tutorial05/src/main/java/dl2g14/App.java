package dl2g14;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.feature.local.matcher.BasicTwoWayMatcher;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.geometry.transforms.estimation.RobustHomographyEstimator;
import org.openimaj.math.model.fit.RANSAC;

public class App {
    public static void main( String[] args ) throws MalformedURLException, IOException {
    	
    	// We import our query and target images
    	MBFImage query = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/query.jpg"));
    	MBFImage target = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/target.jpg"));
    	
    	// We create and engine to find the keypoints from each image
    	DoGSIFTEngine engine = new DoGSIFTEngine();	
    	LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
    	LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());
        
        // We find the matches
    	// Exercise 5.1.1 - Different matchers
    	/*
    	 * Here we chage our BasicMatcher to a BasicTwoWayMatcher and analyse its performance.
    	 * The BasicTwoWayMatcher compares the eucledian distances between the query and target keypoints.
    	 */
    	
    	//LocalFeatureMatcher<Keypoint> matcher = new BasicMatcher<Keypoint>(80);
    	
    	LocalFeatureMatcher<Keypoint> matcher = new BasicTwoWayMatcher<Keypoint>();
    	
    	matcher.setModelFeatures(queryKeypoints);
    	matcher.findMatches(targetKeypoints);
        
        // We draw and display the matches
    	MBFImage basicMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), RGBColour.RED);
    	DisplayUtilities.display(basicMatches);
        
        /* We apply RANSAC to get the most consistent matches.
         * The RANSAC algorithm grabs only a subset of the data and generates and model,
         * this model is then used to find how many inliers the model produces from the remaining data.
         * After itarating this process several times, the best model is selected and used for the entire
         * dataset. In this particular, example we are looking a 50% of the data as our inlier data. 
         */
    	
    	 
    	/* Exercise 5.1.2 - Different Models
    	 * In this exercise, we compare the matches using a different model, the HomographyModel.
    	 * Simulations of both RANSAC and LMeds algoriths were made.
    	 */
    	
    	// RobustAffineTransformEstimator modelFitter = new RobustAffineTransformEstimator(5.0, 1500, 
    	//		new RANSAC.PercentageInliersStoppingCondition(0.5));
   	
   		// LMeds algorithms with no refinement for a HomographyModel.
    	//HomographyRefinement refinement = HomographyRefinement.NONE;
    	//RobustHomographyEstimator modelFitter = new RobustHomographyEstimator(0.5, refinement);
    	
    	
    	/* RANSAC algorithms for a HomographyModel with no refinement. 
    	 * This model gives a more accurate border on our target image.
    	 */
    	
    	RobustHomographyEstimator modelFitter = new RobustHomographyEstimator(5.0, 1500, 
    			new RANSAC.PercentageInliersStoppingCondition(0.5), HomographyRefinement.NONE);
    
    	matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
    			new FastBasicKeypointMatcher<Keypoint>(8), modelFitter); 	
    	
    	matcher.setModelFeatures(queryKeypoints);
    	matcher.findMatches(targetKeypoints);

    	MBFImage consistentMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), 
    			RGBColour.RED);

    	DisplayUtilities.display(consistentMatches);
    			
    	target.drawShape(
    			query.getBounds().transform(modelFitter.getModel().getTransform().inverse()), 3,RGBColour.BLUE);
    	
    	DisplayUtilities.display(target); 
        		
    }	
}
