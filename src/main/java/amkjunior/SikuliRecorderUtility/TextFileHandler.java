package amkjunior.SikuliRecorderUtility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Ajith MK
 * @version 1.0
 *
 */
class TextFileHandler {
	private static String logFileName;
	/**
	 * To set the folder path where the Chopper log needs to be saved.
	 * @param directory Path/Directory where the log needs to be saved.
	 */
	static void setLogFileDirectory(String directory)
	{
		logFileName = directory + "\\ChopperLog.txt";
        try {
        	File file = new File(logFileName);
        	if(!file.exists())
        		file.createNewFile();
        } catch (IOException e) {
        	e.printStackTrace();
        } 
	}
	/**
	 * To write contents to the Chopper Log file
	 * @param content Content to be written to the log file.
	 */
    static void WriteToFile(String content)
    {
    	File file = new File(logFileName);
    	try(FileWriter fw = new FileWriter(file, true);
    			BufferedWriter bw = new BufferedWriter(fw);
    			PrintWriter out = new PrintWriter(bw))
    	{
    		out.println(content);
    	} catch (IOException e) {
        	e.printStackTrace();
    	} 
    }
}
