package amk.sikuli.recorder.utility;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.sikuli.script.Finder;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;

/**
 * Version 2.0 of Chopper is built to crop the file such that even if 1st attempt fails with targetOffset(0,0);
 * it would try to find another unique image within the specified bounds and write the new targetOffset value
 * in the ChopperLog file.<br>
 * <b>Note:-</b><br>
 * 1. The targetOffsets would have to be manually replaced within the script as of now in case of failure to crop at (0,0).<br>
 * 2. System exit code "1" equates to not all parameters being passed correctly or config file not present.<br>
 * 3. System exit code "2" equates to failure encountered while determining crop Region.<br>
 * @author Ajith MK
 * @version 2.0
 * @param screenshot Absolute Path of the screenshot from which the image needs to be cropped.
 * @param targetX x-coordinate of the point at which the image needs to be centered.
 * @param targetY y-coordinate of the point at which the image needs to be centered.
 * @param outputFile Output file name post image crop.
 * @param similarity at which unique match needs to be found.
 * @param boundX
 * @param boundY
 * @param boundW
 * @param boundH
 * @param minimum Width of the image to be cropped.
 * @param minimum Height of the image to be cropped.
 * @param maxAttempts
 */
public class ChopperTheCropper {
	private static int maxAttempts;

	public static void main( String[] args ) {
    	//Check to see if all parameters are passed correctly and if config file is present
    	if(!((args.length == 12) || (args.length == 4)))
    		System.exit(1);

    	int minWidth;
    	int minHeight;
    	float similarity;
    	Region bounds;
    	
    	//Assigning required parameters
    	String screenshot = args[0];
    	int x             = Integer.parseInt(args[1]);
    	int y             = Integer.parseInt(args[2]);
    	String outputFile = args[3];  
    	
    	if(args.length == 12) {
            similarity  = Float.parseFloat(args[4]);
            bounds = new Region(
            		Integer.parseInt(args[5]),
            		Integer.parseInt(args[6]),
            		Integer.parseInt(args[7]),
            		Integer.parseInt(args[8]));
            minWidth      = Integer.parseInt(args[9]);
            minHeight     = Integer.parseInt(args[10]);
            maxAttempts   = Integer.parseInt(args[11]);
    	} else {
    		similarity = Float.parseFloat(PropertyFileHandler.getProperty("similarity"));
            bounds = new Region(
            		Integer.parseInt(PropertyFileHandler.getProperty("BoundX")),
            		Integer.parseInt(PropertyFileHandler.getProperty("BoundY")),
            		Integer.parseInt(PropertyFileHandler.getProperty("BoundW")),
            		Integer.parseInt(PropertyFileHandler.getProperty("BoundH")));
            minWidth      = Integer.parseInt(PropertyFileHandler.getProperty("MinWidth"));
            minHeight     = Integer.parseInt(PropertyFileHandler.getProperty("MinHeight"));
            maxAttempts   = Integer.parseInt(PropertyFileHandler.getProperty("MaxAttempts"));
    	}
        
        //Checking if MinWidth & MinHeight even or not
        if(minWidth%2 != 0)
        	minWidth += 1;
        if(minHeight%2 != 0)
        	minHeight += 1;
        
        //Declaring additional parameters required
        Pattern tpPattern = null;
        Pattern ssPattern = null;
        Region cropRegion = null;
        String outputFileName;
        String outputFoldername;
        
        ssPattern = new Pattern(screenshot).similar(similarity);
        outputFileName    = outputFile;
        outputFileName    = outputFileName.substring(outputFileName.lastIndexOf('\\')).replace("\\", "");
        outputFileName    = outputFileName.substring(0, outputFileName.indexOf('.'));
        outputFoldername  = outputFile;
        outputFoldername  = outputFoldername.substring(0, outputFoldername.lastIndexOf('\\'));
        TextFileHandler.setLogFileDirectory(outputFoldername);
        
        //Core Logic
        BufferedImage biCropImage = null;
        BufferedImage ssImage     = ssPattern.getBImage();
        do {
        	if(cropRegion == bounds)
        		break;
            cropRegion    = getCropRegion(x, y, minWidth, minHeight, bounds, cropRegion);
            if(cropRegion.x == 0 && cropRegion.y == 0 && cropRegion.w == 0 && cropRegion.h == 0) {
        		int newX  = ((bounds.x + bounds.w)/2);
        		int newY  = ((bounds.y + bounds.h)/2);
        		String content = "[warning] Failed to crop " + outputFileName + ".png. Attempting to crop with new targetOffset (" + (newX - x) + ", " + (newY - y) + ")";
            	TextFileHandler.writeToFile(content);
            	main(new String[]{screenshot, Integer.toString(newX), Integer.toString(newY), outputFile});
            }
        	biCropImage   = cropImage(ssImage, cropRegion);
            tpPattern     = new Pattern(biCropImage).similar(similarity);
        }
        while(!isPatternUnique(tpPattern, ssImage));
        
        File f = new File(outputFile);
    	try {ImageIO.write(biCropImage, "PNG", f);} catch (Exception e) {System.exit(2);}
    	
        System.exit(0);
    }

    private static Region getCropRegion(int x, int y, int minWidth, int minHeight, Region bounds, Region oldCropRegion) 
    {
        Integer incrementer = maxAttempts;
        incrementer         = (int) Math.sqrt(incrementer);
        if (incrementer <= 1) incrementer = 2;
        
        int marginX = (x - bounds.x) < (bounds.x + bounds.w - x)
        		    ? (x - bounds.x)
        		    : (bounds.x + bounds.w - x);
        	        
        int marginY = (y - bounds.y) < (bounds.y + bounds.h - y)
        		    ? (y - bounds.y)
        		    : (bounds.y + bounds.h - y);
        			
    	int incrementalX = marginX / incrementer;
    	int incrementalY = marginY / incrementer;
    	
    	if(incrementalX < 1) incrementalX = 1;
    	if(incrementalY < 1) incrementalY = 1; 
        		    
		Region newCropRegion = new Region(0, 0, 0, 0); 
    	if(oldCropRegion == null) {
    		oldCropRegion = new Region(x, y, 1, 1);
			oldCropRegion.x -= minWidth/2;
			oldCropRegion.w += minWidth;
			oldCropRegion.y -= minHeight/2;
			oldCropRegion.h += minHeight;
    		
    		oldCropRegion.x += incrementalX;
    		oldCropRegion.w -= (2 * incrementalX);
    	}
		
    	if(((oldCropRegion.x - incrementalX) > bounds.x) 
    	&& ((oldCropRegion.x + oldCropRegion.w + incrementalX) < (bounds.x + bounds.w))) {
    		newCropRegion.x = oldCropRegion.x - incrementalX;
    		newCropRegion.w = oldCropRegion.w + 2 * incrementalX;
    		newCropRegion.y = oldCropRegion.y;
    		newCropRegion.h = oldCropRegion.h;
		}
    	else if(((oldCropRegion.y - incrementalY > bounds.y) 
    		&& ((oldCropRegion.y + oldCropRegion.h + incrementalY) < (bounds.y + bounds.h)))) {
    		newCropRegion.x = x - minWidth/2;
    		newCropRegion.w = minWidth + 1;
    		newCropRegion.y = oldCropRegion.y - incrementalY;
    		newCropRegion.h = oldCropRegion.h + 2 * incrementalY;
    	}
    	else {
    		//output to log file
    		int newX = ((bounds.x + bounds.w)/2);
    		int newY = ((bounds.y + bounds.h)/2);
    		if((newX - x) != 0 && (newY - y) != 0) {
	    		return new Region(0, 0, 0, 0);
    		}
    		else {
    			return bounds;
    		}
    	}
		return newCropRegion;
	}

	private static BufferedImage cropImage(BufferedImage ssImage, Region region) {
		return ssImage.getSubimage(region.x, region.y, region.w, region.h);
    }
	
	private static boolean isPatternUnique(Pattern searchImage, BufferedImage ssImage) {
        int count = 0;
		Finder f  = new Finder(ssImage);
		f.findAll(searchImage);
		while(f.hasNext()) {
			count++;
			f.next();
		}
		return (count == 1);
    }
}
