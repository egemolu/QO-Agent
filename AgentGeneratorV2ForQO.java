import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.ValueDiscrete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class AgentGeneratorV2ForQO {


    private HashMap<String, String> valueToIssue = new HashMap<>();
    private HashMap<String, Integer> values = new HashMap<>();
    private HashMap<String, Integer> maxValueForEachIssue = new HashMap<>();

    private List<AgentForQO> agents = new ArrayList<>();
    private List<Issue> issues;

    private boolean debug_mode = false;


    public AgentGeneratorV2ForQO(int numberOfAgents, List<Issue> issues) {
        this.issues = issues;
        createValues();
        createAgents(numberOfAgents);
    }

    private void createValues() {
        for (int i = 0; i < issues.size(); i++) {
            String issueName = issues.get(i).toString();
            IssueDiscrete issueDiscrete = (IssueDiscrete) issues.get(i);
            Integer maxValueForIssue = -1;
            for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                Random rnd = new Random();
                String valueName = valueDiscrete.getValue();
                valueToIssue.put(valueName, issueName);
                int value = rnd.nextInt(20) + 1;
                values.put(valueName, value);
                if (value >= maxValueForIssue) {
                    maxValueForIssue = value;
                    maxValueForEachIssue.put(issueName, maxValueForIssue);
                }
            }
        }
    }

    public void createAgents(int agentCount) {
        for (int i = 0; i < agentCount; i++) {
            AgentForQO agent = new AgentForQO();
            agent.setValueToIssue(valueToIssue);
            agent.setValues(values);
            agent.setMaxValueForEachIssue(maxValueForEachIssue);
            double lower = 0.0;
            double upper = 1.0;

            for (int j = 0; j < issues.size() - 1; j++) {
                double rand = generetaRandomDouble(lower, upper);
                agent.addIssueValue(issues.get(j).toString(), rand);
                upper -= rand;
            }
            agent.addIssueValue(issues.get(issues.size() - 1).toString(), upper);
            agents.add(agent);

            if (debug_mode)
                agent.printAgent();


        }
    }

    public List<AgentForQO> getAgents() {
        return agents;
    }

    public static double generetaRandomDouble(double lower, double upper) {
        double rand = 0.0;
        double random = new Random().nextDouble();
        rand = lower + (random * (upper - lower));
        return rand;
    }
}
