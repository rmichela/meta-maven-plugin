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
import org.apache.maven.project.MavenProject;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "meta-meta-plugin", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
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
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            var metaPlugin = new MetaPlugin();
            metaPlugin.packageName = buildPackageName(packageName);
            metaPlugin.className = "MetaInitializeMojo";
            metaPlugin.goalName = "meta-initialize";
            metaPlugin.defaultPhase = "org.apache.maven.plugins.annotations.LifecyclePhase.INITIALIZE";
            metaPlugin.threadSafe = "true";

            // Get the target/generated-sources directory
            File generatedSourcesDir = new File(project.getBuild().getDirectory(), "generated-sources/meta-maven-plugin/" + metaPlugin.packageName.replace('.', '/'));

            // Ensure the directory exists
            if (!generatedSourcesDir.exists() && !generatedSourcesDir.mkdirs()) {
                throw new MojoExecutionException("Failed to create directory: " + generatedSourcesDir.getAbsolutePath());
            }

            File outputFile = new File(generatedSourcesDir, metaPlugin.className + ".java");

            try (FileWriter writer = new FileWriter(outputFile)) {
                Mustache template = MUSTACHE_FACTORY.compile("MetaPluginMojo.java.mustache");
                template.execute(writer, metaPlugin).flush();
            }

            project.addCompileSourceRoot(generatedSourcesDir.getAbsolutePath());
        } catch (IOException e) {
            throw new MojoExecutionException("Error writing generated file", e);
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
}
