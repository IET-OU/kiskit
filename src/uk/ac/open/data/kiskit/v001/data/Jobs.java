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

public class Jobs {
	private static Logger log = LoggerFactory.getLogger(Jobs.class);
	private final static URL dataFile = AccreditationBodies.class.getResource("./SOC_JOB_CATEGORIES.csv");
	private Map<String, String> jobCategories = new HashMap<String, String>();

	private Jobs() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(dataFile.getFile()));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				Pattern pattern = Pattern.compile("\"([0-9]{1,3})\".\"(.*)\"");
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					String code = matcher.group(1);
					String description = matcher.group(2);
					jobCategories.put(code, description);
					jobCategories.put(description.toLowerCase().replace(" ", ""), code); // eh
																							// eh...
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

	public String getCodeFromDescription(String description) {
		return jobCategories.get(description.toLowerCase().replace(" ", ""));
	}

	public String getDescription(String code) {
		if (jobCategories.containsKey(code)) {
			return jobCategories.get(code);
		}
		return "Unknown";
	}

	public boolean codeExists(String code) {
		return jobCategories.containsKey(code);
	}

	private static Jobs instance = null;

	public static Jobs getInstance() {
		if (instance == null) {
			instance = new Jobs();
		}
		return instance;
	}

}
