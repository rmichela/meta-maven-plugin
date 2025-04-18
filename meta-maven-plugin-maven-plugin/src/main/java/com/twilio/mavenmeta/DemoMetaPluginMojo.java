package com.twilio.mavenmeta;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.*;
import org.twdata.maven.mojoexecutor.MavenCompatibilityHelper;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mojo(name = "demo-meta-plugin", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true)
public class DemoMetaPluginMojo extends AbstractMojo {

    @Inject
    private MojoExecution mojoExecution;

    @Inject
    private BuildPluginManager pluginManager;

    /**
     * The current Maven session.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    @SuppressWarnings({"unused"})
    private MavenSession mavenSession;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
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
    @Parameter
    private String packageName;

    /**
     * A list of parameters for use by the generated meta plugin. Has the same properties as
     * {@link com.twilio.mavenmeta.Parameter}. All parameters are of type String and are interpolated into
     * the meta plugin configuration. Meta plugin interpolated parameters are prefixed with #{} instead of ${}.
     */
    @Parameter(name = "parameters")
    @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
    private List<com.twilio.mavenmeta.Parameter> parameters;

    /**
     * A &lt;plugin&gt; element for each plugin to be executed. The plugins follow the same format as build plugins.
     * <p>
     * Plugin phases (default and explicit) determine which meta-plugin goals are generated.
     */
    @Parameter(name = "plugins", required = true)
    @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
    private List<Plugin> plugins;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            if (parameters != null) {
                parameters.forEach(parameter -> {
                    getLog().info("Parameter: " + parameter.getName() + " Default: " + parameter.getDefaultValue());
                });
            }

            var activePhase = mojoExecution.getLifecyclePhase();
            var env = MojoExecutor.executionEnvironment(project, mavenSession, pluginManager);

            if (plugins == null || plugins.isEmpty()) {
                getLog().error("No plugins specified. Must configure at least one plugin.");
                throw new MojoExecutionException("No plugins specified. Must configure at least one plugin.");
            }

            for (Plugin plugin : plugins) {
                getLog().info(plugin.getGroupId() + ":" + plugin.getArtifactId() + ":" + plugin.getVersion());
                var pluginDescriptor = MavenCompatibilityHelper.loadPluginDescriptor(plugin, env, mavenSession);

                var pluginConfiguration = Optional.ofNullable(plugin.getConfiguration()).map(Xpp3Dom.class::cast).orElse(new Xpp3Dom("configuration"));

                for (PluginExecution execution : plugin.getExecutions()) {
                    var executionConfiguration = Optional.ofNullable(execution.getConfiguration()).map(Xpp3Dom.class::cast).orElse(new Xpp3Dom("configuration"));

                    for (String goal : execution.getGoals()) {
                        try {
                            var mojo = pluginDescriptor.getMojo(goal);
                            var executionPhase = execution.getPhase() == null ? mojo.getPhase() + "(default)" : execution.getPhase();

                            getLog().info("Doing " + executionPhase + " in " + activePhase);


                            MojoExecutor.executeMojo(
                                    plugin,
                                    goal,
                                    Xpp3DomUtils.mergeXpp3Dom(executionConfiguration, pluginConfiguration),
                                    env);
                        } catch (MojoExecutionException e) {
                            // TODO: Do better
                            getLog().error("Failed to execute meta goal " + plugin.getGroupId() + ":" + plugin.getArtifactId() + ":" + goal + " : " + e.getCause().getMessage());
                            throw new MojoExecutionException("Failed to execute plugin", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
