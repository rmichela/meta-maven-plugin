package com.twilio.mavenmeta;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.*;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.*;
import org.twdata.maven.mojoexecutor.MavenCompatibilityHelper;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

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
    @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
    private List<Plugin> plugins;

    @Override
    public void execute() throws MojoExecutionException {
        try {
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
