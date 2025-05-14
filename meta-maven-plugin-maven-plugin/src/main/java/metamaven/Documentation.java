package metamaven;

import org.apache.maven.plugins.annotations.LifecyclePhase;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class Documentation {
    public String overall;
    public Map<String, String> phases;

    /**
     * Ensure that every configuration goal key is a valid lifecycle phase id.
     * @return true if all keys are valid, false otherwise.
     */
    public boolean validatePhases(Set<LifecyclePhase> phasesInUse) {
        if (phases != null) {
            for (String phaseKey : this.phases.keySet()) {
                LifecyclePhase phase = LifecyclePhases.fromString(phaseKey);
                if (!Arrays.asList(LifecyclePhase.values()).contains(phase)) {
                    return false;
                }
                if (!phasesInUse.contains(phase)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String getJavadoc(Documentation documentation, LifecyclePhase phase) {
        StringBuilder sb = new StringBuilder();
        sb.append("Executes the <code>");
        sb.append(phase.id());
        sb.append("</code> lifecycle phase for each plugin in the meta-plugin's configuration.");

        if (documentation != null) {
            if (documentation.overall != null && !documentation.overall.isEmpty()) {
                sb.append("\n<br/><br/>\n");
                sb.append(documentation.overall);
            }
            if (documentation.phases != null && documentation.phases.containsKey(phase.id())) {
                String goal = documentation.phases.get(phase.id());
                if (goal != null && !goal.isEmpty()) {
                    sb.append("\n<br/><br/>\n");
                    sb.append(goal);
                }
            }
        }
        return asJavadoc(sb.toString());
    }

    public static String asJavadoc(String text) {
        if (text == null) {
            return "";
        } else {
            String[] lines = text.split("\n");
            StringBuilder sb = new StringBuilder();
            sb.append("/**\n");
            for (String line : lines) {
                sb.append(" * ").append(line).append("\n");
            }
            sb.append(" */");
            return sb.toString();
        }
    }
}
