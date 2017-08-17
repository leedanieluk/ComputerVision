package dl2g14;

import java.util.Map.Entry;

import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.dataset.BingImageDataset;
import org.openimaj.image.dataset.FlickrImageDataset;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.BingAPIToken;
import org.openimaj.util.api.auth.common.FlickrAPIToken;


public class App {
    public static void main( String[] args ) throws Exception   {
    	
    	// Import Images and store them in the list dataset
    	VFSListDataset<FImage> images = new VFSListDataset<FImage>("/Users/daniellee/Desktop/images", 
    			ImageUtilities.FIMAGE_READER);
    	System.out.println(images.size());
    	
    	// Display random image
    	DisplayUtilities.display(images.getRandomInstance(), "A random image from the dataset");
    	
    	// Display all images in a single window
    	DisplayUtilities.display("My images", images);
    	
    	// Display all faces from the zip file in a single window
    	VFSListDataset<FImage> faces = 
    			new VFSListDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);
    	DisplayUtilities.display("ATT faces", faces);
    	
    	// Import faces into  our Group Dataset
    	VFSGroupDataset<FImage> groupedFaces = 
    			new VFSGroupDataset<FImage>( "zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);
    	
    	// Display faces in groups
    	// Exercise 6.1.1 - Exploring Grouped Datasets
    	for (final Entry<String, VFSListDataset<FImage>> entry : groupedFaces.entrySet()) {
    		
    		// We display a random face from each group
    		//DisplayUtilities.display(entry.getKey(), entry.getValue());
    		VFSListDataset<FImage> faces_key = entry.getValue();
    		//DisplayUtilities.display(faces_key.getRandomInstance(), "Random image of key " + entry.getKey());
    	}
    	
    	/* Exercise 6.1.2 - Find out more about VFS datasets
    	 * The sources that Common VFS can manipulate include Local Disk, HTTP server, Zip Archive,
    	 * BZIP2, FTP, Jar, Ram, RES and several more.
    	 */
    	
    	FlickrAPIToken flickrToken = DefaultTokenFactory.get(FlickrAPIToken.class);
    	
    	// Flickr Search Images of cats
    	FlickrImageDataset<FImage> cats = 
    			FlickrImageDataset.create(ImageUtilities.FIMAGE_READER, flickrToken, "cat", 10);
    	DisplayUtilities.display("Cats", cats);
    	
    	// Exercise 6.1.3 - Try the BingImageDataset dataset
    	// Bing Seach Images of dogs
    	BingAPIToken bingToken = new BingAPIToken("ca3144a68e764cf2ac1fecc21acac83b");
    	
    	BingImageDataset<FImage> messi = 
    			BingImageDataset.create(ImageUtilities.FIMAGE_READER, bingToken, "messi", 10);
    	
    	BingImageDataset<FImage> obama = 
    			BingImageDataset.create(ImageUtilities.FIMAGE_READER, bingToken, "obama", 10);
    
    	BingImageDataset<FImage> dicaprio = 
    			BingImageDataset.create(ImageUtilities.FIMAGE_READER, bingToken, "leonardo dicaprio", 10);
    	
    	// Exercise 6.1.4 - Using MapBackedDataset
    	// First we create our MapBackedDataset
    	MapBackedDataset<String, Dataset<FImage>, FImage> mapped = new MapBackedDataset<String, Dataset<FImage>, FImage>();
    	
    	// Then we add our datasets
    	mapped.add("messi", messi);
    	mapped.add("obama", obama);
    	mapped.add("dicaprio", dicaprio);
    	
    	// Lastly, we print a random image for each dataset
    	Dataset<FImage> messiData = mapped.getInstances("messi");
    	Dataset<FImage> obamaData = mapped.getInstances("obama");
    	Dataset<FImage> dicaprioData = mapped.getInstances("dicaprio");
    	
    	DisplayUtilities.displayName(messiData.getRandomInstance(), "Messi");
    	DisplayUtilities.displayName(obamaData.getRandomInstance(), "Obama");
    	DisplayUtilities.displayName(dicaprioData.getRandomInstance(), "Dicaprio");
    	
    }   
}