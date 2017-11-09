package amkjunior.SikuliRecorderUtility;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.sikuli.script.Finder;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;

public class App 
{
	static String ssFile;
	static int x, y, MinWidth, MinHeight;
	static String OutputFileName;
	
    public static void main( String[] args ) 
    {
        ssFile = args[0];
        x = Integer.valueOf(args[1]);
        y = Integer.valueOf(args[2]);
        OutputFileName = args[3];
        float similarity = Float.valueOf(PropertyFileHandler.getProperty("similarity"));
        
        String tpFile = null;
        Pattern tpPattern = null;
        Pattern ssPattern = new Pattern(ssFile).similar(similarity);
        
        String Filename = OutputFileName;
        Filename = Filename.substring(Filename.lastIndexOf("\\")).replace("\\", "");
        Filename = Filename.substring(0, Filename.indexOf("."));
        
        String Foldername = OutputFileName;
        Foldername = Foldername.substring(0, Foldername.lastIndexOf("\\"));
                
        MinWidth = Integer.valueOf(PropertyFileHandler.getProperty("MinWidth"));
        MinHeight = Integer.valueOf(PropertyFileHandler.getProperty("MinHeight"));
        
        if(MinWidth%2 != 0)
        	MinWidth += 1;
        if(MinHeight%2 != 0)
        	MinHeight += 1;
        
        Region CropRegion = new Region(x - MinWidth/2, y - MinHeight/2, MinWidth, MinHeight);
        Region bounds = new Region(Integer.valueOf(PropertyFileHandler.getProperty("BoundX")),
        		Integer.valueOf(PropertyFileHandler.getProperty("BoundY")),
        		Integer.valueOf(PropertyFileHandler.getProperty("BoundW")),
        		Integer.valueOf(PropertyFileHandler.getProperty("BoundH")));
        boolean firstAttempt = true;
        do
        {
        	if(CropRegion == bounds)
        		break;
        	tpFile = Foldername + "\\" + Filename + "_" + String.valueOf(System.currentTimeMillis());
            tpPattern = new Pattern(tpFile).similar(similarity);
            if (!firstAttempt)
            	CropRegion = DetermineCropRegion(x, y, bounds, CropRegion);
        	firstAttempt = false;
        	CropImage(ssPattern, tpPattern, CropRegion);
            tpPattern = new Pattern(tpFile).similar(similarity);
        }
        while(NoOfMatches(tpPattern, ssPattern) > 1);
        
        new File(tpPattern.getFilename()).renameTo(new File(OutputFileName));
    }

	public static int NoOfMatches(Pattern searchImage, Pattern Screenshot)
    {
        int count = 0;
    	try {
				Finder f = new Finder(Screenshot.getBImage());
				f.findAll(searchImage);
				while(f.hasNext())
				{
					count++;
					f.next();
				}
			} 
    	catch (Exception e1) {
			e1.printStackTrace();
		}
    	if(count > 1)
    		new File(searchImage.getFilename()).delete();
    	return count;
    }

    public static void CropImage(Pattern Image, Pattern Screenshot, Region region)
    {
    	BufferedImage croppedImage = Image.getBImage().getSubimage(region.x, region.y, region.w, region.h);
    	File f = new File(Screenshot.getFilename());
    	try {
			ImageIO.write(croppedImage, "PNG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private static Region DetermineCropRegion(int x, int y, Region bounds, Region cropRegion) {
		Region newCropRegion = new Region(0, 0, 0, 0);
    	int incrementalX = (int) (0.05 * bounds.w) > 1 ? (int) (0.05 * bounds.w) : 1;
    	int incrementalY = (int) (0.05 * bounds.h) > 1 ? (int) (0.05 * bounds.h) : 1;
		
    	if(((cropRegion.x - incrementalX) > bounds.x) && ((cropRegion.x + cropRegion.w + incrementalX) < (bounds.x + bounds.w)))
    	{
    		newCropRegion.x = cropRegion.x - incrementalX;
    		newCropRegion.w = cropRegion.w + 2 * incrementalX;
    		newCropRegion.y = cropRegion.y;
    		newCropRegion.h = cropRegion.h;
		}
    	else if(((cropRegion.y - incrementalY > bounds.y) && ((cropRegion.y + cropRegion.h + incrementalY) < (bounds.y + bounds.h))))
    	{
    		newCropRegion.x = x - MinWidth/2;
    		newCropRegion.w = MinWidth;
    		newCropRegion.y = cropRegion.y - incrementalY;
    		newCropRegion.h = cropRegion.h + 2 * incrementalY;
    	}
    	else
    	{
    		//output to log file
    		int newX = ((bounds.x + bounds.w)/2);
    		int newY = ((bounds.y + bounds.h)/2);
    		if((newX - x) != 0 && (newY - y) != 0)
    		{
	    		System.out.println("[warning] Failed to capture image with (0,0) targetOffset.");
	    		System.out.println("[warning] Attempting to capture screen image with below targetOffset:");
	    		System.out.println((newX - x) + "," + (newY - y));
	    		//call main
	    		main(new String[]{ssFile,Integer.toString(newX),Integer.toString(newY),OutputFileName});
        		//Exit app
        		System.exit(5);
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
}
