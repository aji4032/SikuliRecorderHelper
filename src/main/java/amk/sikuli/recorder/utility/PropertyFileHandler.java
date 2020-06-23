package amk.sikuli.recorder.utility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Ajith MK
 * @version 1.0
 */
class PropertyFileHandler {
	static final String CONFIG_FILE = "config.properties";
	
	private PropertyFileHandler() {}
	/**
	 * To reset a value in "config.properties" file
	 * @param key The key whose value that needs to be reset.
	 * @param value The new value for the key.
	 */
	static void setProperty(String key, String value){
		ArrayList<String> lines = new ArrayList<>();		
		
		try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
		    String line;
		    boolean isKeyPresent = false;
		    while ((line = br.readLine()) != null) {
		       if(line.contains(key + "=")) {
		    	   lines.add(key + "=" + value);
		    	   isKeyPresent = true;
		       }
		       else
		    	   lines.add(line);
		    }
		    if(!isKeyPresent){
		    	lines.add(key + "=" + value);
		    }
		} catch (IOException e) {
			Logger.println(e.getMessage());
		}

		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			for(String line: lines) {
				writer.write(line + "\n");
			}
		} catch (IOException e) {
			Logger.println(e.getMessage());
		}
	}
	
	/**
	 * To retrieve a String value saved against a "key" in the "config.properties" file.
	 * @param key The name of the Key against which the value needs to be retrieved
	 * @return A string value of the key saved in "config.properties" file.
	 */
	static String getProperty(String key){
		Properties prop = new Properties();
		try (InputStream input = new FileInputStream(CONFIG_FILE)) {
			prop.load(input);
			return prop.getProperty(key);
		} catch (IOException e) {
			Logger.println(e.getMessage());
		}
		return "";
	}
}
