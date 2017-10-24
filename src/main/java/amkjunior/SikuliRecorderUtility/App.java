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
    public static void main( String[] args ) 
    {
        String ssFile = args[0];
        int x = Integer.valueOf(args[1]);
        int y = Integer.valueOf(args[2]);
        String tpFile = null;
        Pattern tpPattern = null;
        Pattern ssPattern = new Pattern(ssFile).similar((float)0.8);
        Region CropRegion = new Region(x-3, y-3, 6, 6);
        Region bounds = new Region(Integer.valueOf(PropertyFileHandler.getProperty("BoundX")),
        		Integer.valueOf(PropertyFileHandler.getProperty("BoundY")),
        		Integer.valueOf(PropertyFileHandler.getProperty("BoundW")),
        		Integer.valueOf(PropertyFileHandler.getProperty("BoundH")));
        do
        {
        	tpFile = String.valueOf(System.currentTimeMillis());
            tpPattern = new Pattern(tpFile).similar((float)0.8);
            CropRegion = DetermineCropRegion(x, y, bounds, CropRegion);
        	CropImage(ssPattern, tpPattern, CropRegion);
            tpPattern = new Pattern(tpFile).similar((float)0.8);
        	System.out.println(tpPattern.getFilename());
        }
        while(NoOfMatches(tpPattern, ssPattern) > 1);
        System.out.println(tpPattern.getFilename());        
        new File(tpPattern.getFilename()).renameTo(
        		new File(PropertyFileHandler.getProperty("OutFile")));
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
    	
    	if(((cropRegion.x - 5) > bounds.x) && ((cropRegion.x + cropRegion.w + 5) < (bounds.x + bounds.w)))
    	{
    		newCropRegion.x = cropRegion.x - 5;
    		newCropRegion.w = cropRegion.w + 10;
    		newCropRegion.y = cropRegion.y;
    		newCropRegion.h = cropRegion.h;
		}
    	else if(((cropRegion.y - 5 > bounds.y) && ((cropRegion.y + cropRegion.h + 5) < (bounds.y + bounds.h))))
    	{
    		newCropRegion.x = x - 5;
    		newCropRegion.w = 10;
    		newCropRegion.y = cropRegion.y - 5;
    		newCropRegion.h = cropRegion.h + 10;
    	}
		return newCropRegion;
	}
}
