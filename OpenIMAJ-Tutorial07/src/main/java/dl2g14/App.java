package dl2g14;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.processing.edges.SUSANEdgeDetector;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.xuggle.XuggleVideo;

public class App {
    public static void main( String[] args ) throws MalformedURLException, IOException {
    	
    	// Create our video object
    	Video<MBFImage> video;
    	
    	// Get video and store it in video
    	video = new XuggleVideo(new URL("http://static.openimaj.org/media/tutorial/keyboardcat.flv"));
    	
    	// Local camera
    	// video = new VideoCapture(320, 240);
    	
    	// Display video
    	VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video);
    	
    	// A simple way of adding Canny Edge detector to each frame of the video
    	/*for (MBFImage mbfImage : video) {
    	    DisplayUtilities.displayName(mbfImage.process(new CannyEdgeDetector()), "videoFrames");
    	}*/
    	
    	/* Exercise 7.1.1 - Applying different types of image processing to the video
    	 * In this exercise, I implemented the SUSAN edge detector to display the thicker edges, by combining
    	 * edges that are close together.
    	 */
    	for (MBFImage mbfImage : video) {
    	    DisplayUtilities.displayName(mbfImage.process(new SUSANEdgeDetector()), "videoFrames");
    	}
    	
    	display.addVideoListener(
    	  new VideoDisplayListener<MBFImage>() {
    		  public void beforeUpdate(MBFImage frame) {
    			  frame.processInplace(new CannyEdgeDetector());
    	    }

    	    public void afterUpdate(VideoDisplay<MBFImage> display) {
    	    }
    	});
    	
    }
}