package dl2g14;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;

public class App {
    public static void main( String[] args ) throws IOException {
    	
    	// Store images' URL in array
    	URL[] imageURLs = new URL[] {
    			   new URL( "http://users.ecs.soton.ac.uk/dpd/projects/openimaj/tutorial/hist1.jpg" ),
    			   new URL( "http://users.ecs.soton.ac.uk/dpd/projects/openimaj/tutorial/hist2.jpg" ), 
    			   new  URL( "http://users.ecs.soton.ac.uk/dpd/projects/openimaj/tutorial/hist3.jpg" ) 
    			};
    	
    	// Store images
    	MBFImage image1 = ImageUtilities.readMBF(imageURLs[0]);
    	MBFImage image2 = ImageUtilities.readMBF(imageURLs[1]);
    	MBFImage image3 = ImageUtilities.readMBF(imageURLs[2]);
    	
    	MBFImage[] images = {image1, image2, image3};
    	
		// Generate a histogram model
		List<MultidimensionalHistogram> histograms = new ArrayList<MultidimensionalHistogram>();
		HistogramModel model = new HistogramModel(4, 4, 4);

		// Push the estimates model into our list of histrograms
		for( URL u : imageURLs ) {
		    model.estimateModel(ImageUtilities.readMBF(u));
		    histograms.add( model.histogram.clone() );
		}
		
		// Exercise 4.1.1 - Finding and displaying similar images
		double shortest_distance = 1;
		int hist_a = 0, hist_b = 0;
		
		// Compare each histogram with each other by computing the Euclidean distances between them
		for( int i = 0; i < histograms.size(); i++ ) {
		    for( int j = 0; j < histograms.size(); j++ ) {
		    	
		    	// Irrelevant
		    	if(i == j) {continue;}
		    	
		    	/* Exercise 4.1.2 - Exploring comparison measures
		    	 * When using the INTERSECTION comparison, the intersection between the histograms is compared
		    	 * with the formula s(H1,H2) = sumI( min(H1(I), H2(I) ).
		    	 * In this case, the two images with the closest histogram intersections values are images 1 and 2.
		    	 */
		    	
		        //double distance = histograms.get(i).compare( histograms.get(j), DoubleFVComparison.EUCLIDEAN );
		        double distance = histograms.get(i).compare( histograms.get(j), DoubleFVComparison.INTERSECTION );
		        
		        // Store shortest distance otherwise carry on
		        if (shortest_distance > distance) {
		        	shortest_distance = distance;
		        	hist_a = i;
		        	hist_b = j;
		        }
		        
		        int k = i + 1; 
		        int l = j + 1; 
		        
		        // For debugging
		        System.out.println("Comparing histograms " + k + " and " + l + ". Distance: " + distance);
		    }
		}

		DisplayUtilities.displayName(images[hist_a], "Image A");
		DisplayUtilities.displayName(images[hist_b], "Image B");
    			
    }
}
