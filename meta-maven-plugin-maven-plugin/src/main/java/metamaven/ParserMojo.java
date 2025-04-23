package metamaven;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;
import org.apache.maven.model.interpolation.ModelInterpolator;
import org.apache.maven.model.io.ModelReader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import javax.inject.Inject;
import java.io.File;
import java.io.StringReader;

@Mojo(name = "demo-parser", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class ParserMojo extends AbstractMojo {
    private static final String MODEL = """
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>com.github.ekryd.echo-maven-plugin</groupId>
        <artifactId>echo-maven-plugin</artifactId>
        <version>2.0.0</version>
        <executions>
          <execution>
            <goals>
              <goal>echo</goal>
            </goals>
            <phase>compile</phase>
          </execution>
        </executions>
        <configuration>
          <message>Value</message>
        </configuration>
      </plugin>
      <plugin>
        <groupId>com.github.ekryd.echo-maven-plugin</groupId>
        <artifactId>echo-maven-plugin</artifactId>
        <version>2.0.0</version>
        <executions>
          <execution>
            <goals>
              <goal>echo</goal>
            </goals>
            <configuration>
              <message>${bananas}</message>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
    """;

    @Inject
    private ModelReader modelReader;

    @Inject
    private ModelInterpolator interpolator;

    @org.apache.maven.plugins.annotations.Parameter(defaultValue = "${project}", readonly = true, required = true)
    @SuppressWarnings({"unused"})
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Model model = modelReader.read(new StringReader(MODEL), null);

            ModelBuildingRequest request = new DefaultModelBuildingRequest()
                    .setProcessPlugins(true)
                    .setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_STRICT)
                    .setSystemProperties(System.getProperties())
                    .setUserProperties(project.getProperties());

            ModelProblemCollector collector = new ModelProblemCollector() {
                @Override
                public void add(ModelProblemCollectorRequest req) {
                    getLog().error(req.getMessage(), req.getException());
                }
            };
            model = interpolator.interpolateModel(model, null, request, collector);

            for (var plugin : model.getBuild().getPlugins()) {
                getLog().info("Plugin: " + plugin.getGroupId() + ":" + plugin.getArtifactId());
                for (var execution : plugin.getExecutions()) {
                    getLog().info("  Execution: " + execution.getId());
                    if (execution.getConfiguration() != null) {
                        getLog().info("    Configuration: " + execution.getConfiguration().toString());
                    }
                }
            }
        } catch (Throwable e) {
            throw new MojoExecutionException("Error parsing configuration", e);
        }
    }
}
