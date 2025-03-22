package com.twilio.mavenmeta;

import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.*;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Mojo(name = "generate-meta-plugin", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class MetaMetaPluginMojo extends AbstractMojo {

    @Inject
    private MojoExecution mojoExecution;

    @Parameter(name = "plugins", required = true)
    private List<Plugin> plugins;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            var plugin = mojoExecution.getPlugin();
            Xpp3Dom configurationXml = (Xpp3Dom) plugin.getConfiguration();
            if (configurationXml != null) {
                getLog().info("Received XML configuration:");
//                printXml(configurationXml);
                plugins.forEach(p -> {
                    getLog().info(p.getGroupId() + ":" + p.getArtifactId() + ":" + p.getVersion());
                    getLog().info(p.getConfiguration().toString());
                });
            } else {
                getLog().warn("No XML configuration provided.");
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void printXml(Xpp3Dom dom) {
        StringWriter writer = new StringWriter();
        XMLWriter xmlWriter = new PrettyPrintXMLWriter(writer, "  ");
        Xpp3DomWriter.write(xmlWriter, dom, false);
        getLog().info("\n" + writer);
    }

    private PluginContainer getPluginContainer(Xpp3Dom pluginContainerXml) throws Exception {
        Method privateMethod = MavenXpp3Reader.class.getDeclaredMethod("parsePluginContainer", XmlPullParser.class, boolean.class);
        privateMethod.setAccessible(true);

        StringWriter writer = new StringWriter();
        XMLWriter xmlWriter = new CompactXMLWriter(writer);
        Xpp3DomWriter.write(xmlWriter, pluginContainerXml, false);

        XmlPullParser parser = new MXParser();
        parser.setInput(new XmlStreamReader(new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8))));
        parser.nextTag(); // Discard the opening configuration tag
        return (PluginContainer) privateMethod.invoke(new MavenXpp3Reader(),parser, true);
    }
}
