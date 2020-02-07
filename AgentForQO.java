import genius.core.Bid;
import genius.core.issue.Issue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentForQO {

    private static int idCounter = 0;

    private int agent_Id;

    private HashMap<String, Double> issues;
    private HashMap<String, String> valueToIssue;
    private HashMap<String, Integer> values;
    private HashMap<String, Integer> maxValueForEachIssue;

    public AgentForQO() {
        issues = new HashMap<>();
        valueToIssue = new HashMap<>();
        values = new HashMap<>();
        maxValueForEachIssue = new HashMap<>();
        idCounter++;
        agent_Id = idCounter;
    }


    public int getAgent_Id() {
        return agent_Id;
    }

    public void addIssueValue(String issue, Double value) {
        issues.put(issue, value);
    }

    public void setValueToIssue(HashMap<String, String> valueToIssue) {
        this.valueToIssue = valueToIssue;
    }

    public void setMaxValueForEachIssue(HashMap<String, Integer> maxValueForEachIssue) {
        this.maxValueForEachIssue = maxValueForEachIssue;
    }

    public void setValues(HashMap<String, Integer> values) {
        this.values = values;
    }

    public double calculateUtility(Bid offer) {
        double utility = 0;

        for (Issue issue : offer.getIssues()) {
            String issueName = issue.toString();
            for (Map.Entry<String, String> entry : valueToIssue.entrySet()) {
                // Check if value matches with given value
                if (entry.getValue().equals(issueName)) {
                    if (entry.getKey().equals(offer.getValue(issue).toString()))
                        utility += issues.get(issueName) * values.get(entry.getKey()) / (double) maxValueForEachIssue.get(issueName);
                }
            }
        }
        return utility;
    }

    public void printAgent() {
        System.out.println("ISSUES MAP");
        for (Map.Entry<String, Double> entry : issues.entrySet()) {
            System.out.println("KEY:" +entry.getKey());
            System.out.println("KEY:" +entry.getValue());
        }
        System.out.println("---------------------");
        System.out.println("Value To Issue MAP");
        for (Map.Entry<String, String> entry : valueToIssue.entrySet()) {
            System.out.println("KEY:" +entry.getKey());
            System.out.println("KEY:" +entry.getValue());
        }
        System.out.println("---------------------");
        System.out.println("Values MAP");
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            System.out.println("KEY:" +entry.getKey());
            System.out.println("KEY:" +entry.getValue());
        }
        System.out.println("---------------------");
        for (Map.Entry<String, Integer> entry : maxValueForEachIssue.entrySet()) {
            System.out.println("KEY:" +entry.getKey());
            System.out.println("KEY:" +entry.getValue());
        }
        System.out.println("---------------------");
    }
}
