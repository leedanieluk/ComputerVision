package dl2g14;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;

public class App {
    public static void main( String[] args ) throws MalformedURLException, IOException {
    	
    	// Storing our image into MBFImage object
    	MBFImage image = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/sinaface.jpg"));
    	System.out.println(image.colourSpace);
    	
    	// Exercise 2.1.1 - DisplayUtilities
    	// We use the displayName with the same name in all of the images, to display only a single window
    	DisplayUtilities.displayName(image, "Final Image");
    	DisplayUtilities.displayName(image.getBand(0), "Final Image");
    	
    	// Copy image into our clone variable
    	MBFImage clone = image.clone();
    	
    	// Vertical axis
    	for(int y=0; y<image.getHeight(); y++) {
    		// Horizontal axis
    		// Set every pixel of the Blue and Green band to black
    		for(int x=0; x<image.getWidth(); x++) {
    			clone.getBand(1).pixels[y][x] = 0;
    			clone.getBand(2).pixels[y][x] = 0;
    		}
    	}
    	
    	/* Alternative method
    	clone.getBand(1).fill(0f);
		clone.getBand(2).fill(0f);
    	 */
    	
    	DisplayUtilities.displayName(clone, "Final Image");
    	
    	// Exercise 2.1.2 - Drawing
    	// Applying Canny edge detection on our initial image
    	image.processInplace(new CannyEdgeDetector());
    	DisplayUtilities.displayName(image, "Final Image");
    	
    	image.drawShapeFilled(new Ellipse(700f, 450f, 20f, 10f, 0f), RGBColour.WHITE);
    	image.drawShapeFilled(new Ellipse(650f, 425f, 25f, 12f, 0f), RGBColour.WHITE);
    	image.drawShapeFilled(new Ellipse(600f, 380f, 30f, 15f, 0f), RGBColour.WHITE);
    	image.drawShapeFilled(new Ellipse(500f, 300f, 100f, 70f, 0f), RGBColour.WHITE);
    	
    	// Thicker and colourful edge!
    	image.drawShape(new Ellipse(700f, 450f, 20f, 10f, 0f), 5, RGBColour.RED);
    	image.drawShape(new Ellipse(650f, 425f, 25f, 12f, 0f), 5, RGBColour.RED);
    	image.drawShape(new Ellipse(600f, 380f, 30f, 15f, 0f), 5, RGBColour.RED);
    	image.drawShape(new Ellipse(500f, 300f, 100f, 70f, 0f), 5, RGBColour.RED);
    	
    	image.drawText("OpenIMAJ is", 425, 300, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
    	image.drawText("Awesome", 425, 330, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
    	
    	// Single display, therefore only the last image content will appear
    	DisplayUtilities.displayName(image, "Final Image");
   
    }
}