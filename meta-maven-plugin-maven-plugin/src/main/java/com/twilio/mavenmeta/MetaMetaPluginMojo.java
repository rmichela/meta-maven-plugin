package com.twilio.mavenmeta;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.*;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.*;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Mojo(name = "generate-meta-plugin", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true)
public class MetaMetaPluginMojo extends AbstractMojo {

    @Inject
    private MojoExecution mojoExecution;

    @Inject
    private BuildPluginManager pluginManager;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The current Maven session.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession mavenSession;


    @Parameter(name = "plugins", required = true)
    private List<Plugin> plugins;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            var plugin = mojoExecution.getPlugin();
            Xpp3Dom configurationXml = (Xpp3Dom) plugin.getConfiguration();
            if (configurationXml != null) {
                getLog().info("Received XML configuration:");
                for (Plugin p : plugins) {
                    getLog().info(p.getGroupId() + ":" + p.getArtifactId() + ":" + p.getVersion());

                    for (PluginExecution ex : p.getExecutions()) {
                        for (String goal : ex.getGoals()) {
                            MojoExecutor.executeMojo(
                                    p,
                                    goal,
                                    (Xpp3Dom) p.getConfiguration(),
                                    MojoExecutor.executionEnvironment(project, mavenSession, pluginManager));
                        }
                    }
                }
            } else {
                getLog().warn("No XML configuration provided.");
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

//    private void printXml(Xpp3Dom dom) {
//        StringWriter writer = new StringWriter();
//        XMLWriter xmlWriter = new PrettyPrintXMLWriter(writer, "  ");
//        Xpp3DomWriter.write(xmlWriter, dom, false);
//        getLog().info("\n" + writer);
//    }
//
//    private PluginContainer getPluginContainer(Xpp3Dom pluginContainerXml) throws Exception {
//        Method privateMethod = MavenXpp3Reader.class.getDeclaredMethod("parsePluginContainer", XmlPullParser.class, boolean.class);
//        privateMethod.setAccessible(true);
//
//        StringWriter writer = new StringWriter();
//        XMLWriter xmlWriter = new CompactXMLWriter(writer);
//        Xpp3DomWriter.write(xmlWriter, pluginContainerXml, false);
//
//        XmlPullParser parser = new MXParser();
//        parser.setInput(new XmlStreamReader(new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8))));
//        parser.nextTag(); // Discard the opening configuration tag
//        return (PluginContainer) privateMethod.invoke(new MavenXpp3Reader(),parser, true);
//    }
}
