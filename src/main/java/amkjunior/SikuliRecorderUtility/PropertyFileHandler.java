package amkjunior.SikuliRecorderUtility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class PropertyFileHandler {
	static String configFile = "config.properties";
	
	public static void setProperty(String key, String value){
		ArrayList<String> lines = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(configFile));
		    String line;
		    boolean isKeyPresent = false;
		    while ((line = br.readLine()) != null) {
		       if(line.contains(key + "=")){
		    	   lines.add(key + "=" + value);
		    	   isKeyPresent = true;
		       }
		       else
		    	   lines.add(line);
		    }
		    if(!isKeyPresent){
		    	lines.add(key + "=" + value);
		    }
		    br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			FileWriter writer = new FileWriter(configFile);
			for(String line: lines) {
				writer.write(line + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static String getProperty(String key){
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(configFile);
			prop.load(input);
			return prop.getProperty(key);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
