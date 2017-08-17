package dl2g14;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.time.Timer;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.partition.RangePartitioner;

public class App {
    public static void main( String[] args ) throws IOException {
    	
    	// Tests parallel processing with a simple eample
    	Parallel.forIndex(0, 10, 1, new Operation<Integer>() {
    		public void perform(Integer i) {
    		    //System.out.println(i);
    		}
    	});
    	VFSGroupDataset<MBFImage> allImages = Caltech101.getImages(ImageUtilities.MBFIMAGE_READER);
    	GroupedDataset<String, ListDataset<MBFImage>, MBFImage> images = GroupSampler.sample(allImages, 8, false);
    	
    	final List<MBFImage> output = new ArrayList<MBFImage>();
    	final ResizeProcessor resize = new ResizeProcessor(200);
    	
    	// I initialize the timer before the outer loop
    	Timer t1 = Timer.timer();
    	
    	/* This chunk of code uses parallalel processing for inner for loop
    	 * The running time after parallelising was 20405ms.
    	 * Using partivioned parallelisation, the time was further reduced to 14626ms.
    	 */
    	
    	/*for (ListDataset<MBFImage> clzImages : images.values()) {
    	    final MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);
    	    
    	    // Parallelising the inner loop
    	    /*
    	    Parallel.forEach(clzImages, new Operation<MBFImage>() {
    	        public void perform(MBFImage i) {
    	            final MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
    	            tmp.fill(RGBColour.WHITE);

    	            final MBFImage small = i.process(resize).normalise();
    	            final int x = (200 - small.getWidth()) / 2;
    	            final int y = (200 - small.getHeight()) / 2;
    	            tmp.drawImage(small, x, y);

    	            synchronized (current) {
    	                current.addInplace(tmp);
    	            }
    	        }
    	    });
    	*/
    	    
    	  /*  // Testing RangePartitioner
    	    Parallel.forEachPartitioned(new RangePartitioner<MBFImage>(clzImages), new Operation<Iterator<MBFImage>>() {
    	    	public void perform(Iterator<MBFImage> it) {
    	    	    MBFImage tmpAccum = new MBFImage(200, 200, 3);
    	    	    MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);

    	    	    while (it.hasNext()) {
    	    	        final MBFImage i = it.next();
    	    	        tmp.fill(RGBColour.WHITE);

    	    	        final MBFImage small = i.process(resize).normalise();
    	    	        final int x = (200 - small.getWidth()) / 2;
    	    	        final int y = (200 - small.getHeight()) / 2;
    	    	        tmp.drawImage(small, x, y);
    	    	        tmpAccum.addInplace(tmp);
    	    	    }
    	    	    synchronized (current) {
    	    	        current.addInplace(tmpAccum);
    	    	    }
    	    	}
    	    });
    	    
    	    current.divideInplace((float) clzImages.size());
    	    output.add(current);
    	}*/
    	
    	// Exercise 14.1.1 - Parallelise the outer loop
    	/*
    	 * To achieve this, we iterate over each ListDataset<MBFImage>
    	 * contained in the images variable.
    	 * This resulted in a running time of 17238ms. Which was a bit slower than
    	 * the inner loop partitiooned but faster than the inner loop normal parallalisation.
    	 * 
    	 * The advantages of foor loops is that it can achieve a after run time when the outer loop
    	 * iterations are larger than the inner loops iterations or when there is a large variance in
    	 * the run time of the inner loop tasks.
    	 * The disadvatages are when they are not efficient to use when the tasks in the inner loop are larger
    	 * than the iterations of the outer loops, as it would not serve much purpose to reduce the run time.
    	 */
    	
    	Parallel.forEach(images.values(), new Operation<ListDataset<MBFImage>>() {
    		public void perform(ListDataset<MBFImage> clzImages) {
	    	    MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);
	    	    
	    	    for (MBFImage i : clzImages) {
	    	        MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
	    	        tmp.fill(RGBColour.WHITE);

	    	        MBFImage small = i.process(resize).normalise();
	    	        int x = (200 - small.getWidth()) / 2;
	    	        int y = (200 - small.getHeight()) / 2;
	    	        tmp.drawImage(small, x, y);

	    	        current.addInplace(tmp);
	    	    }
	    	    current.divideInplace((float) clzImages.size());
	    	    output.add(current);
    		}
    	});
    	
    	
    	DisplayUtilities.display("Images", output);
    	System.out.println("Time: " + t1.duration() + "ms");
    	
    }
}
