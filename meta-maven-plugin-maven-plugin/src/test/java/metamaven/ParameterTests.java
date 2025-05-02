package metamaven;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParameterTests {

    @Test
    public void testIsNameValid_ValidNames() {
        Parameter parameter = new Parameter();

        // Valid Java field names
        parameter.name = "validName";
        assertTrue(parameter.isNameValid());

        parameter.name = "_underscore";
        assertTrue(parameter.isNameValid());

        parameter.name = "$dollarSign";
        assertTrue(parameter.isNameValid());
    }

    @Test
    public void testIsNameValid_InvalidNames() {
        Parameter parameter = new Parameter();

        // Invalid Java field names
        parameter.name = "123invalid";
        assertFalse(parameter.isNameValid());

        parameter.name = "class"; // Reserved keyword
        assertFalse(parameter.isNameValid());

        parameter.name = null;
        assertFalse(parameter.isNameValid());

        parameter.name = "";
        assertFalse(parameter.isNameValid());
    }

    @Test
    public void testGetDefaultValue() {
        Parameter parameter = new Parameter();

        // Default value is null
        parameter.defaultValue = null;
        assertEquals("", parameter.getDefaultValue());

        // Default value without placeholders
        parameter.defaultValue = "defaultValue";
        assertEquals("defaultValue", parameter.getDefaultValue());

        // Default value with Maven placeholder
        parameter.defaultValue = "@{placeholder}";
        assertEquals("${placeholder}", parameter.getDefaultValue());
    }
}
