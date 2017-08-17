package dl2g14;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.typography.hershey.HersheyFont;

/**
 * OpenIMAJ Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
    	// Create an image
        MBFImage image = new MBFImage(720,70, ColourSpace.RGB);

        // Fill the image with white
        image.fill(RGBColour.BLACK);
        		        
        // Exercise 1.2.1 - Playing with the sample application
        image.drawText("Computer Vision Works!", 10, 60, HersheyFont.FUTURA_LIGHT, 60, RGBColour.RED);

        // Apply a Gaussian blur
        image.processInplace(new FGaussianConvolve(1f));
        
        // Display the image
        DisplayUtilities.display(image);
    }
}
