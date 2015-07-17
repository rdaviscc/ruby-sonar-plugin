package com.godaddy.sonar.ruby.metricfu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class MetricfuComplexityYamlParserImpl implements
		MetricfuComplexityYamlParser {
	private static final Logger LOG = LoggerFactory
			.getLogger(MetricfuComplexityYamlParser.class);

	@SuppressWarnings("unchecked")
	public List<RubyFunction> parseFunctions(String fileNameFromModule,
			File resultsFile) {
		LOG.debug("Started parseFunctions method");
		List<RubyFunction> rubyFunctionsForFile = new ArrayList<RubyFunction>();

		String fileString = null;
		LOG.info("Reading from file " + resultsFile.getAbsolutePath());
		try {
			fileString = FileUtils.readFileToString(resultsFile, "UTF-8");
		} catch (IOException e) {
			LOG.error("Issue reading file : " + resultsFile.getAbsolutePath(),
					e);
		}

		int hotSpotIndex = fileString.indexOf(":hotspots:");
		if (hotSpotIndex >= 0) {
			String stringToRemove = fileString.substring(hotSpotIndex,
					fileString.length());
			fileString = StringUtils.remove(fileString, stringToRemove);
		}

		Yaml yaml = new Yaml();

		Map<String, Object> metricfuResult = (Map<String, Object>) yaml.loadAs(
				fileString, Map.class);

		LOG.debug("Started saikuro metricResult "
				+ metricfuResult.get(":saikuro"));
		if (metricfuResult.get(":saikuro") == null) {
			LOG.debug("Metricresult was null giving up on saikuru ");
			return new ArrayList<RubyFunction>();
		}

		ArrayList<Map<String, Object>> saikuroFilesResult = null;
		Map<String, Object> saikuroResult = (Map<String, Object>) metricfuResult
				.get(":saikuro");
		saikuroFilesResult = (ArrayList<Map<String, Object>>) saikuroResult
				.get(":files");

		Map<String, Object> fileInfoToWorkWith = new HashMap<String, Object>();
		for (Map<String, Object> fileInfo : saikuroFilesResult) {
			String fileNameFromResults = (String) fileInfo.get(":filename");

			if (fileNameFromResults.contains(fileNameFromModule)) {
				fileInfoToWorkWith = fileInfo;
				break;
			}
		}

		if (fileInfoToWorkWith.size() == 0) {
			return new ArrayList<RubyFunction>();
		}

		ArrayList<Map<String, Object>> classesInfo = (ArrayList<Map<String, Object>>) fileInfoToWorkWith
				.get(":classes");

		for (Map<String, Object> classInfo : classesInfo) {
			ArrayList<Map<String, Object>> methods = (ArrayList<Map<String, Object>>) classInfo
					.get(":methods");

			for (Map<String, Object> method : methods) {
				RubyFunction rubyFunction = new RubyFunction();
				rubyFunction.setName((String) method.get(":name"));
				rubyFunction.setComplexity((Integer) method.get(":complexity"));
				rubyFunction.setLine((Integer) method.get(":lines"));

				rubyFunctionsForFile.add(rubyFunction);
			}
		}

		LOG.debug("Returning ruby functions for file " + rubyFunctionsForFile);
		return rubyFunctionsForFile;
	}
}
