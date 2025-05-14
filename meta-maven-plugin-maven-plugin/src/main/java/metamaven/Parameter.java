package metamaven;

import org.codehaus.plexus.configuration.PlexusConfiguration;

import javax.lang.model.SourceVersion;

public class Parameter {
    public String name;
    public String description;
    public String alias;
    public String property;
    public String defaultValue;
    public boolean required;
    public boolean readonly;

    // Getters for Mustache template specific pre-processing
    public String getName() {
        return name;
    }

    public boolean isNameValid() {
        return isValidJavaFieldName(name);
    }

    public String getAlias() {
        return alias == null ? "" : alias;
    }

    public String getProperty() {
        return property == null ? "" : property;
    }

    public String getDefaultValue() {
        return defaultValue == null ? "" : defaultValue.replace("@{", "${");
    }

    public String getJavadoc() {
        return Documentation.asJavadoc(description);
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isReadonly() {
        return readonly;
    }

    private static boolean isValidJavaFieldName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        // Check if the name is a valid identifier and not a reserved keyword
        return SourceVersion.isIdentifier(name) && !SourceVersion.isKeyword(name);
    }
}
