package metamaven;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.model.io.ModelReader;
import org.apache.maven.plugin.*;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MavenCompatibilityHelper;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;

/**
 * Generates the code for implementing a meta-plugin mojo, using Maven native <code>Plugin</code> configuration in
 * from POM. When executed as a plugin, the generated mojo executes multiple Maven plugins in order.
 * <p><p>
 * If the configured plugins bind to multiple Maven phases, the meta-plugin will generate a separate mojo for each phase.
 */
@Mojo(name = "meta-meta-plugin", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE)
@SuppressWarnings({"unused"})
public class MetaMetaPluginMojo extends AbstractMojo {
    private static final String DEFAULT_EXECUTION_ID = "default";

    @Inject
    private MojoExecution mojoExecution;

    @Inject
    private BuildPluginManager pluginManager;

    @Inject
    private ModelReader modelReader;

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
     * Documentation for the meta-plugin.
     * <ul>
     *     <li><code>overall</code> - Overall documentation for the overall meta-plugin.</li>
     *     <li><code>phases</code> - Key/value paris of additional documentation for maven build phases.
     *         Keys must be valid maven phase names in use by one of the embedded plugins.
     *         Values are documentation strings.</li>
     * </ul>
     * <p>
     * For rich text HTML, use <code>&lt;![CDATA[]]&gt;</code> escaping in the pom.
     */
    @org.apache.maven.plugins.annotations.Parameter
    @SuppressWarnings({"unused"})
    private Documentation documentation;

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
     * {@link Parameter}.
     * <ul>
     *     <li><code>name</code> - The name of the property. Must be a valid Java field name.</li>
     *     <li><code>description</code> - Documentation of the parameter's purpose. For rich text HTML, use
     *         <code>&lt;![CDATA[]]&gt;</code> escaping in the pom.</li>
     *     <li><code>alias</code> - An alias name of the parameter in the POM.</li>
     *     <li><code>property</code> - Property to use to retrieve a value. Can come from -D execution, setting
     *         properties or pom properties.</li>
     *     <li><code>defaultValue</code> - The default value of the parameter. This value can be an interpolated
     *         expression.</li>
     *     <li><code>required</code> - Whether the parameter is required.</li>
     *     <li><code>readonly</code> - Whether the parameter is readonly and should not be set by the user.
     *         Used to capture values from the Maven project context.</li>
     * </ul>
     * <p><p>
     * Parameter default values can be interpolated during Maven's execution.
     * <ul>
     *     <li>Expressions like <code>${}</code> are evaluated during meta-plugin code generation.</li>
     *     <li>Expressions like <code>#{}</code> are evaluated during meta-plugin execution.</li>
     * </ul>
     */
    @org.apache.maven.plugins.annotations.Parameter()
    @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
    private List<Parameter> parameters;

    /**
     * A <code>&lt;plugin&gt;</code> element for each plugin to be executed. The plugins follow the same format as build plugins.
     * <p><p>
     * Plugin phases (default and explicit) determine which meta-plugin goals are generated.
     */
    @org.apache.maven.plugins.annotations.Parameter(required = true)
    @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
    private List<Plugin> plugins;

    private static final MustacheFactory MUSTACHE_FACTORY = new DefaultMustacheFactory();

    /**
     * This method is the main entry point for the meta-plugin code generator.
     *
     * @throws MojoExecutionException if an error occurs where the user is not at fault
     * @throws MojoFailureException if an error occurs where the user is at fault
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        assertPluginNameSuffix();
        assertParameters();
        assertMavenPluginPackaging();
        assertMavenPluginPlugin();
        assertDocumentation();
        assertNoDuplicatePlugins();
        assertDependency("org.apache.maven", "maven-core", "provided");
        assertDependency("org.apache.maven", "maven-plugin-api", "provided");
        assertDependency("org.apache.maven.plugin-tools", "maven-plugin-annotations", "provided");
        assertDependency("org.twdata.maven", "mojo-executor", "compile");

        var metaPlugin = new MetaPlugin();
        metaPlugin.packageName = buildPackageName(packageName);
        metaPlugin.parameters = parameters;
        metaPlugin.pluginConfiguration = pluginsToXml(mojoExecution.getConfiguration().getChild("plugins")).split("\\n");
        metaPlugin.abstractClassName = executionIdToClassName(mojoExecution.getExecutionId()) + "AbstractMetaPluginMojo";

        generateFile("AbstractMetaPluginMojo.java.mustache", metaPlugin.packageName, metaPlugin.abstractClassName + ".java", metaPlugin);

        for (LifecyclePhase phase : phasesInUse()) {
            metaPlugin.javadoc = Documentation.getJavadoc(documentation, phase, pluginsToJavaDocXml(phase));
            metaPlugin.className = executionIdToClassName(mojoExecution.getExecutionId()) + LifecyclePhases.toClassName(phase)+ "Mojo";
            metaPlugin.goalName = (mojoExecution.getExecutionId().equals(DEFAULT_EXECUTION_ID) ? "" : mojoExecution.getExecutionId() + "-") + phase.id();
            metaPlugin.defaultPhase = "LifecyclePhase." + phase.name();
            metaPlugin.threadSafe = allPluginsThreadSafe(phase) ? "true" : "false";
            generateFile("PhaseMetaPluginMojo.java.mustache", metaPlugin.packageName, metaPlugin.className + ".java", metaPlugin);
        }
    }

    private String pluginsToXml(Xpp3Dom plugins) {
        var project = new Xpp3Dom("project");
        var build = new Xpp3Dom("build");
        project.addChild(build);
        build.addChild(plugins);
        var xml = project.toString();
        xml = xml.replace("@{", "${");
        xml = xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "");
        return xml;
    }

    private String pluginsToJavaDocXml(LifecyclePhase forPhase) throws MojoExecutionException {
        try {
            Model model = modelReader.read(new StringReader(pluginsToXml(mojoExecution.getConfiguration().getChild("plugins"))), null);

            // Find plugin executions that are not this one and remove them
            MojoExecutor.ExecutionEnvironment env = executionEnvironment(project, mavenSession, pluginManager);
            List<org.apache.maven.model.Plugin> toRemovePlugins = new ArrayList<>();
            for (var plugin : model.getBuild().getPlugins()) {
                PluginDescriptor pluginDescriptor = MavenCompatibilityHelper.loadPluginDescriptor(plugin, env, mavenSession);
                List<org.apache.maven.model.PluginExecution> toRemoveExecutions = new ArrayList<>();
                for (var execution : plugin.getExecutions()) {
                    for (String goal : execution.getGoals()) {
                        MojoDescriptor mojo = pluginDescriptor.getMojo(goal);
                        String goalPhase = execution.getPhase() == null ? mojo.getPhase() : execution.getPhase();
                        if (!goalPhase.equals(forPhase.id())) {
                            toRemoveExecutions.add(execution);
                        }
                    }
                }
                toRemoveExecutions.forEach(plugin::removeExecution);
                if (plugin.getExecutions().isEmpty()) {
                    toRemovePlugins.add(plugin);
                }
            }
            toRemovePlugins.forEach(plugin -> model.getBuild().removePlugin(plugin));

            // Write model to XML
            StringWriter stringWriter = new StringWriter();
            new DefaultModelWriter().write(stringWriter, null, model);
            return stringWriter.toString().replace("<?xml version=\"1.0\"?>\n", "");
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
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

    private void assertDependency(String groupId, String artifactId, String scope) throws MojoFailureException {
        boolean dependencyFound = project.getDependencies().stream()
                .anyMatch(dependency -> groupId.equals(dependency.getGroupId()) &&
                                                    artifactId.equals(dependency.getArtifactId()) &&
                                                    scope.equals(dependency.getScope()));
        if (!dependencyFound) {
            getLog().error("Missing required Maven dependency. Add this to your POM:");
            getLog().error("<dependency>");
            getLog().error("    <groupId>" + groupId + "</groupId>");
            getLog().error("    <artifactId>" + artifactId + "</artifactId>");
            getLog().error("    <version>{latest}</version>");
            getLog().error("    <scope>" + scope + "</scope>");
            getLog().error("</dependency>");
            throw new MojoFailureException("Missing required Maven dependency: " + groupId + ":" + artifactId);
        }
    }

    private void assertMavenPluginPlugin() throws MojoFailureException {
        boolean wellFormed;
        var mavenPluginPlugin = project.getBuildPlugins().stream()
                .filter(plugin -> "org.apache.maven.plugins".equals(plugin.getGroupId()) && "maven-plugin-plugin".equals(plugin.getArtifactId()))
                .findFirst();

        if (mavenPluginPlugin.isPresent()) {
            var plugin = mavenPluginPlugin.get();
            wellFormed = plugin.getExecutions().stream()
                    .anyMatch(execution ->
                            execution.getGoals().contains("helpmojo") &&
                                    execution.getGoals().contains("descriptor"));
        } else {
            wellFormed = false;
        }

        if (!wellFormed) {
            getLog().error("Missing required Maven build plugin. Add this to your POM:");
            getLog().error("<build>");
            getLog().error("    <plugins>");
            getLog().error("        <plugin>");
            getLog().error("            <groupId>org.apache.maven.plugins</groupId>");
            getLog().error("            <artifactId>maven-plugin-plugin</artifactId>");
            getLog().error("            <version>{latest}</version>");
            getLog().error("            <executions>");
            getLog().error("                <execution>");
            getLog().error("                    <goals>");
            getLog().error("                        <goal>helpmojo</goal>");
            getLog().error("                        <goal>descriptor</goal>");
            getLog().error("                    </goals>");
            getLog().error("                </execution>");
            getLog().error("            </executions>");
            getLog().error("        </plugin>");
            getLog().error("    </plugins>");
            getLog().error("</build>");
            throw new MojoFailureException("Missing required Maven build plugin: org.apache.maven.plugins:maven-plugin-plugin");
        }
    }

    private void assertMavenPluginPackaging() throws MojoFailureException {
        if (!project.getPackaging().equals("maven-plugin")) {
            getLog().error("The project must be packaged as a Maven plugin. Set the packaging to 'maven-plugin' in your POM.");
            getLog().error("<packaging>maven-plugin</packaging>");
            throw new MojoFailureException("The project must be packaged as a Maven plugin.");
        }
    }

    private void assertParameters() throws MojoFailureException {
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                if (!parameter.isNameValid()) {
                    getLog().error("Invalid plugin parameter name: '" + parameter.getName() + "'");
                    getLog().error("Parameter names must be valid Java identifiers and not reserved keywords.");
                    getLog().error("To set an alternative name, use the 'alias' property.");
                    throw new MojoFailureException("Invalid parameter name: " + parameter.getName());
                }
            }
        }
    }

    private void assertPluginNameSuffix() {
        if (!project.getArtifactId().endsWith("-maven-plugin")) {
            getLog().warn("The artifactId should end with '-maven-plugin' to follow Maven conventions.");
        }
    }

    private void assertDocumentation() throws MojoFailureException, MojoExecutionException {
        if (documentation != null) {
            if (!documentation.validatePhases(phasesInUse())) {
                getLog().error("Invalid documentation phases. Ensure all keys are valid lifecycle phase ids in use by one of the embedded plugins.");
                throw new MojoFailureException("All documentation phases must be valid lifecycle phase ids and used by one of the embedded plugins.");
            }
        }
    }

    private void assertNoDuplicatePlugins() throws MojoExecutionException {
        try {
            Model model = modelReader.read(new StringReader(pluginsToXml(mojoExecution.getConfiguration().getChild("plugins"))), null);
            Map<String, Integer> found = new HashMap<>();
            model.getBuild().getPlugins().stream().map(org.apache.maven.model.Plugin::getKey).forEach(k -> {
                found.put(k, found.getOrDefault(k, 0) + 1);
            });
            if (found.values().stream().allMatch(v -> v > 1)) {
                getLog().warn("Some problems were encountered while building the effective meta-plugin model");
                found.forEach((k, v) -> {
                    if (v > 1) {
                        getLog().warn("Embedded plugins must be unique but found duplicate declaration of plugin " + k);
                    }
                });
                getLog().warn("It is highly recommended to fix these problems because they threaten the stability of your build.");
            };
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private boolean allPluginsThreadSafe(LifecyclePhase phase) throws MojoExecutionException {
        try {
            var env = MojoExecutor.executionEnvironment(project, mavenSession, pluginManager);
            for (Plugin plugin : plugins) {
                var pluginDescriptor = MavenCompatibilityHelper.loadPluginDescriptor(plugin, env, mavenSession);
                for (org.apache.maven.model.PluginExecution execution : plugin.getExecutions()) {
                    for (String goal : execution.getGoals()) {
                        var mojo = pluginDescriptor.getMojo(goal);
                        if (mojo.getPhase().equals(phase.id()) && !mojo.isThreadSafe()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        } catch (PluginResolutionException | PluginDescriptorParsingException | InvalidPluginDescriptorException | PluginNotFoundException e) {
            throw new MojoExecutionException("Failed to determine thread safety of plugin", e);
        }
    }

    private Set<LifecyclePhase> phasesInUse() throws MojoExecutionException {
        try {
            var env = MojoExecutor.executionEnvironment(project, mavenSession, pluginManager);
            Set<LifecyclePhase> phases = new HashSet<>();

            for (Plugin plugin : plugins) {
                var pluginDescriptor = MavenCompatibilityHelper.loadPluginDescriptor(plugin, env, mavenSession);
                for (org.apache.maven.model.PluginExecution execution : plugin.getExecutions()) {
                    if (execution.getPhase() != null) {
                        phases.add(LifecyclePhases.fromString(execution.getPhase()));
                    }
                    for (String goal : execution.getGoals()) {
                        var mojo = pluginDescriptor.getMojo(goal);
                        phases.add(LifecyclePhases.fromString(mojo.getPhase()));
                    }
                }
            }
            return phases;
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to determine phases in use", e);
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

    /**
     * Converts a Maven plugin execution id (valid XML name) into a valid Java class name,
     * starting with an uppercase letter. Non-Java identifier characters are removed,
     * and underscores or dashes are treated as word boundaries.
     */
    static String executionIdToClassName(String executionId) {
        if (executionId == null || executionId.isEmpty() || executionId.equals(DEFAULT_EXECUTION_ID)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : executionId.toCharArray()) {
            if (c == '-' || c == '_' || !Character.isJavaIdentifierPart(c)) {
                capitalizeNext = true;
                continue;
            }
            if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(c);
            }
        }
        // Ensure the first character is a valid Java identifier start
        if (sb.length() == 0 || !Character.isJavaIdentifierStart(sb.charAt(0))) {
            sb.insert(0, '_');
        }
        return sb.toString();
    }
}
