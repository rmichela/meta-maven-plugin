package com.twilio.mavenmeta;

import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.twdata.maven.mojoexecutor.PlexusConfigurationUtils;

public class Plugin extends org.apache.maven.model.Plugin {
    // Short circuit SISU/Plexus dependency injection for the Plugin class's configuration field. Setter injection
    // takes priority over field injection when resolving injection.
    public void setConfiguration(PlexusConfiguration xml) throws Exception {
        super.setConfiguration(PlexusConfigurationUtils.toXpp3Dom(xml));
    }
}
