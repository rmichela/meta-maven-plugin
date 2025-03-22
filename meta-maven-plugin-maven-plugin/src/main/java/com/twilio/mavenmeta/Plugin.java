package com.twilio.mavenmeta;

import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;

import java.io.StringReader;

public class Plugin extends org.apache.maven.model.Plugin {
    // Short circuit SISU/Plexus dependency injection for the Plugin class's configuration field. Setter injection
    // takes priority over field injection when resolving injection.
    public void setConfiguration(XmlPlexusConfiguration xml) throws Exception {
        StringReader reader = new StringReader(xml.toString());
        Xpp3Dom dom = Xpp3DomBuilder.build(reader);
        super.setConfiguration(dom);
    }
}
