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
        return phase.id().substring(0, 1).toUpperCase() + phase.id().substring(1).replace("-", "");
    }
}
