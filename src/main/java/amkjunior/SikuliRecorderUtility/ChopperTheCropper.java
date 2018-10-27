/**
 * 
 */
package amkjunior.SikuliRecorderUtility;

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
 * @param Screenshot Absolute Path of the screenshot from which the image needs to be cropped
 * @param targetX x-coordinate of the point at which the image needs to be centered.
 * @param targetY y-coordinate of the point at which the image needs to be centered.
 * @param OutputFile Output file name post image crop.
 */
public class ChopperTheCropper {
	public static void main( String[] args ) 
    {
    	//Check to see if all parameters are passed correctly and if config file is present
    	if((args.length != 4) || !(new File("config.properties").exists()))
    		System.exit(1);
    	
    	//Assigning required parameters
    	String Screenshot = args[0];
    	int x = Integer.valueOf(args[1]);
    	int y = Integer.valueOf(args[2]);
    	String OutputFile = args[3];
    	
    	//Fetching all the required properties
        float similarity = Float.valueOf(PropertyFileHandler.getProperty("similarity"));
        Region bounds = new Region(
        		Integer.valueOf(PropertyFileHandler.getProperty("BoundX")),
        		Integer.valueOf(PropertyFileHandler.getProperty("BoundY")),
        		Integer.valueOf(PropertyFileHandler.getProperty("BoundW")),
        		Integer.valueOf(PropertyFileHandler.getProperty("BoundH")));
        int MinWidth = Integer.valueOf(PropertyFileHandler.getProperty("MinWidth"));
        int MinHeight = Integer.valueOf(PropertyFileHandler.getProperty("MinHeight"));
        
        //Checking if MinWidth & MinHeight even or not
        if(MinWidth%2 != 0)
        	MinWidth += 1;
        if(MinHeight%2 != 0)
        	MinHeight += 1;
        
        //Declaring additional parameters required
        Pattern tpPattern = null;
        Pattern ssPattern = null;
        Region CropRegion = null;
        String OutputFileName;
        String OutputFoldername;
        
        ssPattern = new Pattern(Screenshot).similar(similarity);
        OutputFileName = OutputFile;
        OutputFileName = OutputFileName.substring(OutputFileName.lastIndexOf("\\")).replace("\\", "");
        OutputFileName = OutputFileName.substring(0, OutputFileName.indexOf("."));
        OutputFoldername = OutputFile;
        OutputFoldername = OutputFoldername.substring(0, OutputFoldername.lastIndexOf("\\"));
        TextFileHandler.setLogFileDirectory(OutputFoldername);
        
        //Core Logic
        BufferedImage BICropImage = null;
        BufferedImage SSImage     = ssPattern.getBImage();
        do
        {
        	if(CropRegion == bounds)
        		break;
            CropRegion = getCropRegion(x, y, MinWidth, MinHeight, bounds, CropRegion);
            if(CropRegion == null)
            {
        		int newX = ((bounds.x + bounds.w)/2);
        		int newY = ((bounds.y + bounds.h)/2);
        		String content = "[warning] Failed to crop " + OutputFileName + ".png. Attempting to crop with new targetOffset (" + (newX - x) + ", " + (newY - y) + ")";
            	TextFileHandler.WriteToFile(content);
            	main(new String[]{Screenshot, Integer.toString(newX), Integer.toString(newY), OutputFile});
            }
        	BICropImage = CropImage(SSImage, CropRegion);
            tpPattern = new Pattern(BICropImage).similar(similarity);
        }
        while(!isPatternUnique(tpPattern, SSImage));
        
        File f = new File(OutputFile);
    	try {ImageIO.write(BICropImage, "PNG", f);} catch (Exception e) {System.exit(2);}
    	
        System.exit(0);
    }

    private static Region getCropRegion(int x, int y, int MinWidth, int MinHeight, Region bounds, Region oldCropRegion) {
        Integer Incrementer = Integer.valueOf(PropertyFileHandler.getProperty("MaxAttempts"));
        Incrementer = (int) Math.sqrt(Incrementer);
        if (Incrementer <= 1) Incrementer = 2;
        
        int marginX = (x - bounds.x) < (bounds.x + bounds.w - x)
        		    ? (x - bounds.x)
        		    : (bounds.x + bounds.w - x);
        	        
        int marginY = (y - bounds.y) < (bounds.y + bounds.h - y)
        		    ? (y - bounds.y)
        		    : (bounds.y + bounds.h - y);
        			
    	int incrementalX = (int) marginX / Incrementer;
    	int incrementalY = (int) marginY / Incrementer;
    	
    	if(incrementalX < 1) incrementalX = 1;
    	if(incrementalY < 1) incrementalY = 1; 
        		    
		Region newCropRegion = new Region(0, 0, 0, 0); 
    	if(oldCropRegion == null)
    	{
    		oldCropRegion = new Region(x, y, 1, 1);
			oldCropRegion.x -= MinWidth/2;
			oldCropRegion.w += MinWidth;
			oldCropRegion.y -= MinHeight/2;
			oldCropRegion.h += MinHeight;
    		
    		oldCropRegion.x += incrementalX;
    		oldCropRegion.w -= (2 * incrementalX);
    	}
		
    	if(((oldCropRegion.x - incrementalX) > bounds.x) && ((oldCropRegion.x + oldCropRegion.w + incrementalX) < (bounds.x + bounds.w)))
    	{
    		newCropRegion.x = oldCropRegion.x - incrementalX;
    		newCropRegion.w = oldCropRegion.w + 2 * incrementalX;
    		newCropRegion.y = oldCropRegion.y;
    		newCropRegion.h = oldCropRegion.h;
		}
    	else if(((oldCropRegion.y - incrementalY > bounds.y) && ((oldCropRegion.y + oldCropRegion.h + incrementalY) < (bounds.y + bounds.h))))
    	{
    		newCropRegion.x = x - MinWidth/2;
    		newCropRegion.w = MinWidth + 1;
    		newCropRegion.y = oldCropRegion.y - incrementalY;
    		newCropRegion.h = oldCropRegion.h + 2 * incrementalY;
    	}
    	else
    	{
    		//output to log file
    		int newX = ((bounds.x + bounds.w)/2);
    		int newY = ((bounds.y + bounds.h)/2);
    		if((newX - x) != 0 && (newY - y) != 0)
    		{
	    		return null;
    		}
    		else
    		{
    			return new Region(Integer.valueOf(PropertyFileHandler.getProperty("BoundX")),
    	        		Integer.valueOf(PropertyFileHandler.getProperty("BoundY")),
    	        		Integer.valueOf(PropertyFileHandler.getProperty("BoundW")),
    	        		Integer.valueOf(PropertyFileHandler.getProperty("BoundH")));
    		}
    	}
		return newCropRegion;
	}

	private static BufferedImage CropImage(BufferedImage sSImage, Region region)
    {
		BufferedImage croppedImage = null;
		try{
    	croppedImage = sSImage.getSubimage(region.x, region.y, region.w, region.h);
		} catch (Exception e) {
			System.exit(1);
		}
    	return croppedImage;
    }
	private static boolean isPatternUnique(Pattern searchImage, BufferedImage sSImage)
    {
        int count = 0;
		Finder f = new Finder(sSImage);
		f.findAll(searchImage);
		while(f.hasNext())
		{
			count++;
			f.next();
		}
		return (count == 1);
    }
}
