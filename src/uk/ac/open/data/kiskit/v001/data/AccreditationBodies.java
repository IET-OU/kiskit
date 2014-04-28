package uk.ac.open.data.kiskit.v001.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccreditationBodies {

	private static Logger log = LoggerFactory.getLogger(AccreditationBodies.class);
	private final static URL dataFile = AccreditationBodies.class.getResource("./ACCBODYIDs.csv");
	private Map<String, String> accreditationBodies = new HashMap<String, String>();

	/**
	 * singleton
	 */
	private AccreditationBodies() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(dataFile.getFile()));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				Pattern pattern = Pattern.compile("\"([0-9]{3})\".\"(.*)\"");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					String code = matcher.group(1);
					String description = matcher.group(2);
					accreditationBodies.put(code, description);
				} else {
					log.error("SKIPPED The line cannot be parsed: \n{}\n", line);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			log.error("FATAL", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			log.error("FATAL", e);
			throw new RuntimeException(e);
		}
	}

	public String getDescription(String code) {
		if (accreditationBodies.containsKey(code)) {
			return accreditationBodies.get(code);
		}
		return "Unknown";
	}

	public boolean codeExists(String code) {
		return accreditationBodies.containsKey(code);
	}

	private static AccreditationBodies instance = null;

	public static AccreditationBodies getInstance() {
		if (instance == null) {
			instance = new AccreditationBodies();
		}
		return instance;
	}

}
