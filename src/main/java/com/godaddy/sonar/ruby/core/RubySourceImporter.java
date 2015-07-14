package com.godaddy.sonar.ruby.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.api.utils.SonarException;

@Phase(name = Phase.Name.PRE)
//public class RubySourceImporter extends AbstractSourceImporter
public class RubySourceImporter
{
    private static final Logger LOG = LoggerFactory.getLogger(RubySourceImporter.class);
    private Project project;
    private ModuleFileSystem moduleFileSystem;

    public RubySourceImporter(Project project, ModuleFileSystem moduleFileSystem)
    {
//        super(Ruby.INSTANCE);
        this.project = project;
        this.moduleFileSystem = moduleFileSystem;
    }

//    @Override
//    public void analyse(Project project, SensorContext context)
//    {
//        try
//        {
//            LOG.info("Importing files from project " + project.getName());
//            doAnalyse(project, context);
//        } catch (IOException e)
//        {
//            throw new SonarException("Parsing source files ended abnormally", e);
//        }
//    }

//    @Override
//    protected RubyFile createResource(File file, List<File> sourceDirs, boolean unitTest)
//    {
//        return file != null ? new RubyFile(file, sourceDirs) : null;
//    }

    protected void doAnalyse(Project project, SensorContext context) throws IOException
    {
        // Importing source files
        Charset sourceCharset = moduleFileSystem.sourceCharset();

        List<File> sourceDirs = moduleFileSystem.sourceDirs();
        LOG.info("Got {} source dirs", sourceDirs.size());
        List<File> sourceFiles = moduleFileSystem.files(FileQuery.onSource());
        LOG.info("Got {} source files", sourceFiles.size());
//        parseDirs(context, sourceFiles, sourceDirs, false, sourceCharset);
        for (File directory : sourceDirs)
        {
            LOG.info(directory.getName());
        }

        // Importing tests files
        List<File> testDirs = moduleFileSystem.testDirs();
        LOG.info("Got {} test dirs", testDirs.size());
        List<File> testFiles = moduleFileSystem.files(FileQuery.onTest());
        LOG.info("Got {} test files", testFiles.size());
//        parseDirs(context, testFiles, testDirs, true, sourceCharset);
        // Display source dirs and tests directories if info level is enabled.
        for (File directory : testDirs)
        {
            LOG.info(directory.getName());
        }
    }

//    @Override
//    protected void parseDirs(SensorContext context, List<File> files, List<File> sourceDirs, boolean unitTest, Charset sourcesEncoding)
//    {
//        for (File file : files)
//        {
//            RubyFile resource = createResource(file, sourceDirs, unitTest);
//            if (resource != null)
//            {
//                try
//                {
//                    LOG.debug("Indexing resource: " + resource.getName());
//                    context.index(resource);
//                    if (isEnabled(project))
//                    {
//                        String source = FileUtils.readFileToString(file, sourcesEncoding.name());
//                        LOG.debug("Saving source from: " + file.getPath());
//                        context.saveSource(resource, source);
//                    }
//                } catch (Exception e)
//                {
//                    throw new SonarException("Unable to read and import the source file : '" + file.getAbsolutePath() + "' with the charset : '" + sourcesEncoding.name() + "'.", e);
//                }
//            }
//        }
//    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
//        builder.append("getLanguage()", getLanguage());
        builder.append("getClass()", getClass());
        return builder.toString();
    }
}
