package dl2g14;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.connectedcomponent.GreyscaleConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.PixelProcessor;
import org.openimaj.image.segmentation.SegmentationUtilities;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;


/**
 * OpenIMAJ Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws MalformedURLException, IOException {
    	
    	// Get the image and assign it to input
    	MBFImage input = ImageUtilities.readMBF(new URL("http://cdn.images.express.co.uk/img/dynamic/128/590x/secondary/CUTE-ANIMAL-528797.jpg"));
    	DisplayUtilities.displayName(input, "Original Image");
    	
    	// Colour-space transformation to LAB
    	input = ColourSpace.convert(input, ColourSpace.CIE_Lab);
    	FloatKMeans cluster = FloatKMeans.createExact(2);
    	float[][] imageData = input.getPixelVectorNative(new float[input.getWidth() * input.getHeight()][3]);
    	final FloatCentroidsResult result = cluster.cluster(imageData);
  
    	final float[][] centroids = result.centroids;
    	for (float[] fs : centroids) {
    	    System.out.println(Arrays.toString(fs));
    	}
    	
    	/*
    	HardAssigner<float[],?,?> assigner = result.defaultHardAssigner();
    	
    	
    	for (int y=0; y<input.getHeight(); y++) {
    	    for (int x=0; x<input.getWidth(); x++) {
    	    	
    	    	// Get pixel from the original image
    	        float[] pixel = input.getPixelNative(x, y);
 
    	        // Fill the centroid variable with the index of the corresponding class (e.g. 1 or 2)
    	        int centroid = assigner.assign(pixel);
    	        
    	        // Set the pixel to the centroid of the class
    	        input.setPixelNative(x, y, centroids[centroid]);
    	    }
    	}*/
    	
    	// Exercise 3.1.1 - Implementing a PixelProcessor to replace the nested for loops
    	input.processInplace(new PixelProcessor<Float[]>() {
    		
    		HardAssigner<float[],?,?> assigner = result.defaultHardAssigner();
    		
    	    public Float[] processPixel(Float[] pixel) {
    	    	
        		// We need to change the pixel type from Float to float to process it using the assigner
        		float[] pixel_float = new float[pixel.length];

        		for(int i=0; i < pixel_float.length; i++) {
        			pixel_float[i] = pixel[i].floatValue();
        		}
        		
        		// We create the pixel that will contain its respective centroid index
    	    	int centroid = assigner.assign(pixel_float);
    	    	float[] centroid_float = centroids[centroid];
    	    	Float[] final_pixel = new Float[pixel.length];
    	    	
    	    	// Fill in the pixel
    	    	for(int i=0; i < final_pixel.length; i++) {
        	    	final_pixel[i] = centroid_float[i];
    	    	}
    	    	
    	    	return final_pixel;
    	    }
    	});
    	
    	/* Comments on advantages and disadvantages
    	 * Advantages: This function can be easily reduced.
    	 * We avoid using 2 nested for loops every time we want to manipulate the pixels.
    	 * Disadvantages: We have to move from the float type to the Float type
    	 * in different steps of the process.
    	 */
    	
    	// Convert image back to the RGB Colourspace
    	input = ColourSpace.convert(input, ColourSpace.RGB);
    	DisplayUtilities.displayName(input, "After classification");
    	
    	// Find the connected components and store them in the components list
    	GreyscaleConnectedComponentLabeler labeler = new GreyscaleConnectedComponentLabeler();
    	List<ConnectedComponent> components = labeler.findComponents(input.flatten());
    	
    	int i = 0;
    	for (ConnectedComponent comp : components) {
    	    if (comp.calculateArea() < (input.getHeight()*input.getWidth())/4) 
    	        continue;
    	    input.drawText("Comp:" + (i++), comp.calculateCentroidPixel(), HersheyFont.TIMES_MEDIUM, 20);
    	}
    	DisplayUtilities.displayName(input, "Connected components");
    	
    	// Exercise 3.1.2 - A real segmentation algorithm
    	SegmentationUtilities.renderSegments(input, components);
    	DisplayUtilities.displayName(input, "Using FelzenszwalbHuttenlocherSegmenter");
    }
}
