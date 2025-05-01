package metamaven;

import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.twdata.maven.mojoexecutor.PlexusConfigurationUtils;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Plugin extends org.apache.maven.model.Plugin implements java.io.Serializable {
    private static final long serialVersionUID = 1337L;

    // Short circuit SISU/Plexus dependency injection for the Plugin class's configuration field. Setter injection
    // takes priority over field injection when resolving injection.
    public void setConfiguration(PlexusConfiguration xml) throws Exception {
        super.setConfiguration(PlexusConfigurationUtils.toXpp3Dom(xml));
    }

    public void setExecutions(ArrayList<PluginExecution> executions) {
        super.setExecutions(executions.stream().map(org.apache.maven.model.PluginExecution.class::cast).collect(Collectors.toList()));
    }
}
