package com.twilio.mavenmeta;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;

import java.io.StringWriter;

@Mojo(name = "generate-meta-plugin", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class MetaMetaPluginMojo extends AbstractMojo {

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojoExecution;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Plugin plugin = mojoExecution.getPlugin();
        Xpp3Dom root = (Xpp3Dom) plugin.getConfiguration();
        if (root != null) {
            getLog().info("Received XML configuration:");
            printXml(root);
        } else {
            getLog().warn("No XML configuration provided.");
        }
    }

    private void printXml(Xpp3Dom dom) {
        StringWriter writer = new StringWriter();
        XMLWriter xmlWriter = new PrettyPrintXMLWriter(writer, "  ");
        Xpp3DomWriter.write(xmlWriter, dom, false);
        getLog().info("\n" + writer.toString());
    }
}
