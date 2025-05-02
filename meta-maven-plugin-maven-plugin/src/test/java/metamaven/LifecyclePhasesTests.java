package metamaven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LifecyclePhasesTests {

    @Test
    public void testFromStringValidPhase() {
        assertEquals(LifecyclePhase.COMPILE, LifecyclePhases.fromString("compile"));
        assertEquals(LifecyclePhase.TEST, LifecyclePhases.fromString("test"));
    }

    @Test
    public void testFromStringInvalidPhase() {
        assertEquals(LifecyclePhase.NONE, LifecyclePhases.fromString("invalid-phase"));
    }

    @Test
    public void testFromStringCaseInsensitive() {
        assertEquals(LifecyclePhase.PACKAGE, LifecyclePhases.fromString("PACKAGE"));
        assertEquals(LifecyclePhase.INSTALL, LifecyclePhases.fromString("Install"));
    }

    @Test
    public void testToClassName() {
        assertEquals("Compile", LifecyclePhases.toClassName(LifecyclePhase.COMPILE));
        assertEquals("ProcessResources", LifecyclePhases.toClassName(LifecyclePhase.PROCESS_RESOURCES));
        assertEquals("Package", LifecyclePhases.toClassName(LifecyclePhase.PACKAGE));
    }
}
