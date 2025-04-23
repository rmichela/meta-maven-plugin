package metamaven;

public class Parameter {
    private String name;
    private String alias;
    private String property;
    private String defaultValue;
    private boolean required;
    private boolean readonly;

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public String getProperty() {
        return property;
    }

    public String getDefaultValue() {
        return defaultValue.replace("@{", "${");
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isReadonly() {
        return readonly;
    }
}
