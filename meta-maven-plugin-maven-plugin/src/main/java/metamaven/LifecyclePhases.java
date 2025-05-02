package metamaven;

import org.apache.maven.plugins.annotations.LifecyclePhase;

public class LifecyclePhases {
    public static LifecyclePhase fromString(String phase) {
        for (LifecyclePhase lifecyclePhase : LifecyclePhase.values()) {
            if (lifecyclePhase.id().equalsIgnoreCase(phase)) {
                return lifecyclePhase;
            }
        }
        return LifecyclePhase.NONE;
    }

    public static String toClassName(LifecyclePhase phase) {
        String[] parts = phase.id().split("-");
        StringBuilder className = new StringBuilder();
        for (String part : parts) {
            className.append(part.substring(0, 1).toUpperCase())
                     .append(part.substring(1).replace("-", ""));
        }
        return className.toString();
    }
}
