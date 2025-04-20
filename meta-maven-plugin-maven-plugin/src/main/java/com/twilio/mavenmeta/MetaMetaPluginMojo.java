package com.twilio.mavenmeta;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "meta-meta-plugin", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class MetaMetaPluginMojo extends AbstractMojo {
    @Inject
    private MojoExecution mojoExecution;

    @Inject
    private BuildPluginManager pluginManager;

    /**
     * The current Maven session.
     */
    @org.apache.maven.plugins.annotations.Parameter(defaultValue = "${session}", readonly = true, required = true)
    @SuppressWarnings({"unused"})
    private MavenSession mavenSession;

    @org.apache.maven.plugins.annotations.Parameter(defaultValue = "${project}", readonly = true, required = true)
    @SuppressWarnings({"unused"})
    private MavenProject project;

    /**
     * The name of the package for the generated meta plugin.
     * <p>
     * By default, the package name will be calculated as <code>groupId + "." + artifactId</code> with additional
     * <ul>
     * <li><code>-</code> (dashes) will be replaced by <code>_</code> (underscores)</li>
     * <li><code>_</code> (underscore) will be added before each number or Java keyword at the beginning of name</li>
     * </ul>
     */
    @org.apache.maven.plugins.annotations.Parameter
    @SuppressWarnings({"unused"})
    private String packageName;

    /**
     * A list of parameters for use by the generated meta plugin. Has the same properties as
     * {@link com.twilio.mavenmeta.Parameter}. All parameters are of type String and are interpolated into
     * the meta plugin configuration. Meta plugin interpolated parameters are prefixed with #{} instead of ${}.
     */
    @org.apache.maven.plugins.annotations.Parameter()
    @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
    private List<Parameter> parameters;

    /**
     * A &lt;plugin&gt; element for each plugin to be executed. The plugins follow the same format as build plugins.
     * <p>
     * Plugin phases (default and explicit) determine which meta-plugin goals are generated.
     */
    @org.apache.maven.plugins.annotations.Parameter(required = true)
    @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
    private List<Plugin> plugins;

    private static final MustacheFactory MUSTACHE_FACTORY = new DefaultMustacheFactory();

    @Override
    public void execute() throws MojoExecutionException {
//        var plugin = mojoExecution.getPlugin();
//        Xpp3Dom configurationXml = (Xpp3Dom) plugin.getConfiguration();

        var metaPlugin = new MetaPlugin();
        metaPlugin.packageName = buildPackageName(packageName);
        metaPlugin.className = "MetaInitializeMojo";
        metaPlugin.goalName = "meta-initialize";
        metaPlugin.defaultPhase = "org.apache.maven.plugins.annotations.LifecyclePhase.INITIALIZE";
        metaPlugin.threadSafe = "true";
        metaPlugin.parameters = parameters;
        metaPlugin.encodedPlugins = new ArrayList<>();

        try {
            for (Plugin plugin : plugins) {
                metaPlugin.encodedPlugins.add(serializeToBase64(plugin));
            }

            generateFile("MetaPluginMojo.java.mustache", metaPlugin.packageName, metaPlugin.className + ".java", metaPlugin);
            // Utility classes needed for serialized Plugin rehydration
            generateFile("Plugin.java.mustache", "com.twilio.mavenmeta", "Plugin.java", metaPlugin);
            generateFile("PluginExecution.java.mustache", "com.twilio.mavenmeta", "PluginExecution.java", metaPlugin);
        } catch (Exception e) {
            throw new MojoExecutionException("Error writing generated file", e);
        }
    }

    private void generateFile(String templateName, String packageName, String fileName, Object context) throws MojoExecutionException {
        File sourceRoot = new File(project.getBuild().getDirectory(), "generated-sources/meta-maven-plugin/");
        project.addCompileSourceRoot(sourceRoot.getAbsolutePath());

        File packageDirectory = new File(sourceRoot, packageName.replace('.', '/'));

        // Ensure the directory exists
        if (!packageDirectory.exists() && !packageDirectory.mkdirs()) {
            throw new MojoExecutionException("Failed to create output directory: " + packageDirectory.getAbsolutePath());
        }

        File outputFile = new File(packageDirectory, fileName);
        try (FileWriter writer = new FileWriter(outputFile)) {
            Mustache template = MUSTACHE_FACTORY.compile(templateName);
            template.execute(writer, context).flush();
        } catch (Exception e) {
            throw new MojoExecutionException("Error writing generated file " + fileName, e);
        }
    }

    String buildPackageName(String metaPackageName) {
        String packageName = null;
        if (metaPackageName != null && !metaPackageName.isBlank()) {
            packageName = metaPackageName;
        }

        if (packageName == null) {
            packageName = project.getGroupId() + "." + project.getArtifactId();
            packageName = packageName.replace("-", "_");

            String[] packageItems = packageName.split("\\.");
            packageName =
                    Arrays.stream(packageItems).map(this::prefixSpecialCase).collect(Collectors.joining("."));
        }

        return packageName;
    }

    private String prefixSpecialCase(String name) {
        if (SourceVersion.isKeyword(name) || !Character.isJavaIdentifierStart(name.charAt(0))) {
            name = "_" + name;
        }
        return name;
    }

    // Serialize an object to a Base64 string
    private static String serializeToBase64(Serializable obj) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(obj);
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        }
    }
}
