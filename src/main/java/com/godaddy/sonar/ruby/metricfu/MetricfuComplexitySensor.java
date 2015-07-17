package com.godaddy.sonar.ruby.metricfu;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.api.measures.RangeDistributionBuilder;

import com.godaddy.sonar.ruby.core.Ruby;
import com.godaddy.sonar.ruby.core.RubyFile;

public class MetricfuComplexitySensor implements Sensor
{
    private static final Logger LOG = LoggerFactory.getLogger(MetricfuComplexitySensor.class);

    private MetricfuComplexityYamlParser metricfuComplexityYamlParser;
    private ModuleFileSystem moduleFileSystem;
    private static final Number[] FILES_DISTRIB_BOTTOM_LIMITS = { 0, 5, 10, 20, 30, 60, 90 };
    private static final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = { 1, 2, 4, 6, 8, 10, 12, 20, 30 };

    public MetricfuComplexitySensor(ModuleFileSystem moduleFileSystem, MetricfuComplexityYamlParser metricfuComplexityYamlParser)
    {
        this.moduleFileSystem = moduleFileSystem;
        this.metricfuComplexityYamlParser = metricfuComplexityYamlParser;
    }

    public boolean shouldExecuteOnProject(Project project)
    {
        return Ruby.KEY.equals(project.getLanguageKey());
    }

    public void analyse(Project project, SensorContext context)
    {
        File resultsFile = new File(moduleFileSystem.baseDir(), "tmp/metric_fu/report.yml");
        List<File> sourceDirs = moduleFileSystem.sourceDirs();
        List<File> rubyFilesInProject = moduleFileSystem.files(FileQuery.onSource().onLanguage(project.getLanguageKey()));

        for (File file : rubyFilesInProject)
        {
            LOG.debug("analyzing functions for classes in the file: " + file.getName());
            try
            {            	
                analyzeFile(file, sourceDirs, context, resultsFile);
            } catch (IOException e)
            {
                LOG.error("Can not analyze the file " + file.getAbsolutePath() + " for complexity", e);                
            }
        }
    }

    private void analyzeFile(File file, List<File> sourceDirs, SensorContext sensorContext, File resultsFile) throws IOException
    {		
    	
        RubyFile resource = new RubyFile(file, sourceDirs);
        LOG.info("functions are set");
        List<RubyFunction> functions = metricfuComplexityYamlParser.parseFunctions(resource.getName(), resultsFile);

        // if function list is empty, then return, do not compute any complexity
        // on that file
        if (functions.isEmpty() || functions.size() == 0 || functions == null)
        {
            return;
        }
        
        

        // COMPLEXITY
        LOG.info("COMPLEXITY are set" + functions.toString());
        int fileComplexity = 0;
        for (RubyFunction function : functions)
        {
        	
            fileComplexity += function.getComplexity();
            LOG.info("Complexity " + fileComplexity);
        }
        
        LOG.info("Saving Measure ");
        sensorContext.saveMeasure(resource, CoreMetrics.COMPLEXITY, Double.valueOf(fileComplexity));

        // FILE_COMPLEXITY_DISTRIBUTION
        LOG.info("FILE_COMPLEXITY_DISTRIBUTION are set");

        RangeDistributionBuilder fileDistribution = new RangeDistributionBuilder(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION, FILES_DISTRIB_BOTTOM_LIMITS);
        fileDistribution.add(Double.valueOf(fileComplexity));
        sensorContext.saveMeasure(resource, fileDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));

        // FUNCTION_COMPLEXITY
        LOG.info("FUNCTION_COMPLEXITY are set");

        sensorContext.saveMeasure(resource, CoreMetrics.FUNCTION_COMPLEXITY, Double.valueOf(fileComplexity) / functions.size());

        // FUNCTION_COMPLEXITY_DISTRIBUTION
        LOG.info("FUNCTION_COMPLEXITY_DISTRIBUTION are set");

        RangeDistributionBuilder functionDistribution = new RangeDistributionBuilder(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, FUNCTIONS_DISTRIB_BOTTOM_LIMITS);
        for (RubyFunction function : functions)
        {
            functionDistribution.add(Double.valueOf(function.getComplexity()));
        }
        sensorContext.saveMeasure(resource, functionDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
    }
}
