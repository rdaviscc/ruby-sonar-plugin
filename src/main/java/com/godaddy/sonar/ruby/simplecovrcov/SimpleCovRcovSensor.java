package com.godaddy.sonar.ruby.simplecovrcov;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import com.godaddy.sonar.ruby.core.Ruby;
import com.godaddy.sonar.ruby.core.RubyFile;

public class SimpleCovRcovSensor implements Sensor 
{
    private static final Logger LOG = LoggerFactory.getLogger(SimpleCovRcovSensor.class);

    private SimpleCovRcovJsonParser simpleCovRcovJsonParser;
    private ModuleFileSystem moduleFileSystem;

    /**
     * Use of IoC to get Settings
     */
    public SimpleCovRcovSensor(ModuleFileSystem moduleFileSystem, SimpleCovRcovJsonParser simpleCovRcovJsonParser) 
    {
        this.moduleFileSystem = moduleFileSystem;
        this.simpleCovRcovJsonParser = simpleCovRcovJsonParser;
    }

    public boolean shouldExecuteOnProject(Project project) 
    {
        return Ruby.KEY.equals(project.getLanguageKey());
    }

    public void analyse(Project project, SensorContext context) 
    {
    	LOG.info("Calling analyse");

    	String fileName = "coverage/.resultset.json";
        File jsonFile = new File(fileName);
        File jsonFile2 = new File(fileName);
        
       
        FileInputStream fis;
		try {
			fis = new FileInputStream(jsonFile2);
		
    	//Construct BufferedReader from InputStreamReader
    	BufferedReader br = new BufferedReader(new InputStreamReader(fis));
     
    	String line = null;
    	while ((line = br.readLine()) != null) {
    		System.out.println(line);
    	}
     
    	br.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        List<File> sourceDirs = moduleFileSystem.sourceDirs();

        try 
        {
        	LOG.info("Calling Calculate Metrics");
            calculateMetrics(sourceDirs, jsonFile, context);
        } 
        catch (IOException e) 
        {

            LOG.error("unable to calculate Metrics:", e);
            
        }
    }

    private void calculateMetrics(List<File> sourceDirs, File jsonFile, final SensorContext context) throws IOException 
    {
    	LOG.debug(jsonFile.toString());
        Map<String, CoverageMeasuresBuilder> jsonResults = simpleCovRcovJsonParser.parse(jsonFile);

        LOG.debug("jsonResults: " + jsonResults);
        File sourceFile = null;
        for (Entry<String, CoverageMeasuresBuilder> entry : jsonResults.entrySet()) 
        {
            try 
            {
                String fileName = entry.getKey();
                sourceFile = new File(fileName);
                RubyFile rubyFile = new RubyFile(sourceFile, sourceDirs);
                
//                CoreMetrics.LINES_TO_COVER, CoreMetrics.UNCOVERED_LINES, CoreMetrics.COVERAGE_LINE_HITS_DATA,
//                CoreMetrics.CONDITIONS_TO_COVER, CoreMetrics.UNCOVERED_CONDITIONS, CoreMetrics.CONDITIONS_BY_LINE,
//                CoreMetrics.COVERED_CONDITIONS_BY_LINE);
//                
                CoverageMeasuresBuilder fileCoverage = entry.getValue();
                if (fileCoverage.getLinesToCover() > 0) {
                	context.saveMeasure(rubyFile, CoreMetrics.LINES_TO_COVER, (double) fileCoverage.getLinesToCover());
                	context.saveMeasure(rubyFile, CoreMetrics.UNCOVERED_LINES, (double) (fileCoverage.getLinesToCover() - fileCoverage.getCoveredLines()));
//                	context.saveMeasure(rubyFile, CoreMetrics.COVERAGE_LINE_HITS_DATA, (KeyValueFormat.format(fileCoverage.getHitsByLine())).setPersistenceMode(PersistenceMode.DATABASE));
                }
                if (fileCoverage.getConditions() > 0) {
                	context.saveMeasure(rubyFile, CoreMetrics.CONDITIONS_TO_COVER, (double) fileCoverage.getConditions());
                	context.saveMeasure(rubyFile, CoreMetrics.UNCOVERED_CONDITIONS,(double) (fileCoverage.getConditions() - fileCoverage.getCoveredConditions()));
//                	context.saveMeasure(rubyFile, CoreMetrics.CONDITIONS_BY_LINE, hitsByLine.size());
//                	context.saveMeasure(rubyFile, CoreMetrics.LINES_TO_COVER, hitsByLine.size());
                }
                
                
            } 
            catch (Exception e) 
            {
                if (sourceFile != null) 
                {
                    LOG.error("Unable to save metrics for file: " + sourceFile.getName(), e);
                } 
                else
                {
                    LOG.error("Unable to save metrics.", e);
                }
            }
        }
    }
}
